# Motion and standards

**Status: binding.** This document supersedes the numbers and rules listed in **Part 9**
and nothing else. `design-v3.md` remains the authority on everything visual and
interactive that Part 9 does not name. Where this document and `design-v3.md` disagree on
a value named in Part 9, this document wins and Part 9 quotes the sentence it replaces and
states why. Where they disagree on anything Part 9 does not name, `design-v3.md` wins and
this document is wrong. `design-v3.md` is not edited by this document.

Written against `0.11.1`, commit `0e19a37`. Every count below is reproducible with the
command beside it. Every duration is a measured step response, not an estimate. Every
spring was simulated against Compose's own `SpringSimulation` model at unit mass.

---

## The system, in one page

Everything in this document is **one construction applied four times.**

**One ladder**, root 2 on the two numbers `design-v3.md` 6 already states, 20 and 28, halved and doubled:
`2, 5, 7, 10, 14, 20, 28, 40, 56`. It is the **spacing** scale, the **radius** scale and the **travel** scale,
so the app has one set of distances rather than three that have to be kept in agreement. Because each step is
half the step two above it, **insetting a container by half its radius makes the inner radius exactly half the
outer**, and corner nesting stops being a judgment.

**One type scale**, ratio 5:4, quantized onto the platform's own font-scaling nodes: `10, 12, 15, 18, 24, 30`
plus `64` for the timer. Seven sizes, eleven roles, four registered sans weights. Every line box is a multiple
of 4dp, so type and space share a 2dp lattice, and spacing grows on **the curve body text actually grows on**
rather than on a straight line.

**One header component**, `PageHeader`, in two modes: `Named` when the line is the screen's name, `Led` when the
line is a sentence the engine wrote. Eight treatments become one.

**Eight motion jobs**: Orient, Relate, Confirm, Track, Reveal, Replace, Depict, Notice. The job is a fact about
what an animation is for, not a judgment about how it should feel, **so choosing a spring is a lookup rather
than a taste question.** That is the whole of the standardization.

**Seven springs**, named for jobs: `spatialConfirm` 0.82/900, `spatialTravel` 0.86/380, `spatialSettle`
0.90/240, `spatialRelease` 1.00/900, `effectsInstant` 1.00/3800, `effectsChange` 1.00/1600, `effectsArrive`
1.00/800. Plus three delays, `30 / 60 / 120`, one hold, `900`, and one **documented absence**: Depict has no
spring, deliberately. **Eleven motion numbers, replacing six curves, twelve durations and five staggers.**

**Reduce motion is a constraint on displacement, not on duration.** Travel goes to zero. Not one spring
changes, the stagger is untouched because the stagger is the reading order, and every Depict keeps running.
`ReducedMotion` is deleted because there is nothing left for it to do.

**Eleven build gates** make all of it a build failure rather than a convention, shipped behind a shrinking
allowlist so the build is green from the first commit.

**And the cost, stated up front: the app does not get slower to use.** The press gets **159ms faster on every
tap in the app**, the Report gains 400ms of headroom, and the only thing that gets slower is the first arrival
of the eight surfaces that today have no arrival at all, at 506ms each, once per app session.

---

## 0. Why this exists

The app was built to specification and it reads as approximate. Three counts explain it,
and they are the same count three times.

**0.1 There is no space scale.** `ClaritySpacing` declares twelve dimensions and not one
of them is a step on anything. Outside `ui/theme/`, in `src/main`, there are **634 `.dp`
literals across 62 distinct values**, 23 of which are `0.dp`. The values run
`0.5, 1, 1.5, 2, 2.5, 3, 3.5, 4, 5, 6, 7, 8, 9, 9.5, 10, 11, 12, 13, 14, 15, 16, 17, 18,
19, 20, 22, 23, 24, 26, 28, 30, 32, ...` Some are one apart. Some are half apart. There is
no ratio anywhere in the list.

Six of the twelve declared tokens are off the 4dp grid `design-v3.md` 6 claims the app sits
on: `cardPaddingHorizontal` 18, `cardPaddingVertical` 17, `cardGap` 11, `areaDot` 7,
`tabBarHeight` 61, `tabBarInset` 17. **The top visual authority states a grid its own numbers
do not sit on.** That contradiction is settled in 1.1.

**0.2 The type scale is wide, arithmetic, and spent on the wrong screens.** Fourteen sizes
from 10.5sp to 64sp. The sans ladder 13, 15, 17, 19, 21 is a constant *difference* of 2sp,
so its *ratio* decays as it climbs: 1.154, 1.133, 1.118, 1.105. Perceived hierarchy is a
ratio. The top of the ladder is the flattest part of it, which is backwards.

On Areas the loudest element is the word `Areas` at 30sp, **1.43 times** the active item at
21sp. The active item is the one string the app exists to render, and the screen's own name
outranks it.

Worse, the platform's own font scaling makes this collapse exactly where the audience needs
it not to. Android 14 converts sp to dp through a piecewise-linear curve, not a multiplier.
Computed from the AOSP table at the phone's 1.3 setting:

| role | sp | renders at 1.3 | growth |
|---|---|---|---|
| `caption` | 12 | 15.60dp | 1.300x |
| `label` | 13 | 16.40dp | 1.262x |
| `body` | 15 | 18.30dp | 1.220x |
| `itemTitle` | 21 | 23.85dp | 1.136x |
| `readSerif` | 26 | 27.60dp | 1.062x |
| `displayTitle` | 30 | 30.00dp | **1.000x** |

**At 130 percent, the screen title does not grow at all while the caption under it grows 30
percent.** The `itemTitle` to `label` ratio falls from 1.615 to 1.454. `displayTitle` to
`readSerif` falls from 1.154 to 1.087, which is two roles that are no longer two roles.

And `spacingScaleFor` is `fontScale.coerceIn(1f, 2f)`, a straight line. So gaps grow 1.30x
while the body they separate grows 1.220x, and the ratio of air to text drifts from 1.867 to
1.989. **The whitespace overtakes the type it is there to separate.** This is a shipping
defect, invisible on a phone at its default setting, and it is most of why a screen does not
feel composed at any accessibility text size.

**0.3 Motion is the same disease in the time dimension.** Six curve tokens, and beneath them
**twelve distinct duration integers across 25 call sites outside `ui/theme/`**:
`30, 35, 45, 150, 180, 200, 220, 240, 250, 400, 450, 600`. Four of those are staggers doing
one job. `150` is redeclared as a private constant in six files because `ReducedMotion`
already owns that number and no token exposes it. `SETTLE_MILLIS` exists twice with two
different values.

Three of thirteen surfaces have any entrance at all. `Modifier.clarityEntrance` has six call
sites and they are all in `AreasScreen.kt`. **`PredictiveBackHandler` appears zero times
against eight `BackHandler` call sites**, so `design-v3.md` 10.15's "predictive back is
supported everywhere" is currently false at every one of them.

`springStandard` (0.8 / 380) is what a card press runs on. It reaches 98 percent of its
travel in **193ms**, and `Modifier.clickable` delays the press interaction by the platform's
100ms tap timeout inside a scrollable parent. A press that arrives roughly 290ms after the
finger lands is a soft press, and it is on the exact elements the owner named.

**Reading: the app is not under-designed, it is un-systematized.** Good individual decisions,
no ratio between any two of them. Everything below is one construction applied four times.

---

# PART 1. THE STANDARDS

## 1.1 One ladder. It is the space scale, the radius scale, and the travel scale.

**Base: 20 and 28, the two numbers `design-v3.md` 6 already states. Progression: root 2,
realized as halving and doubling both anchors.**

```
28 / 4 =  7      20 / 4 =  5
28 / 2 = 14      20 / 2 = 10
28     = 28      20     = 20
28 * 2 = 56      20 * 2 = 40
```

Interleaved, plus one sub-step for strokes and optical nudges:

| # | dp | name | job | derivation | step ratio |
|---|---|---|---|---|---|
| 1 | **2** | `hair` | not a gap. An optical nudge, the air inside a mark. Never separates two things. | sub-ladder | |
| 2 | **5** | `pair` | two marks that are one thing: an area dot and its label, a glyph and its count | 20 / 4 | 2.50 |
| 3 | **7** | `tight` | two lines of one block: a title and the caption belonging to it | 28 / 4 | 1.40 |
| 4 | **10** | `near` | siblings in a list, a control's edge to its own label | 20 / 2 | 1.43 |
| 5 | **14** | `block` | one grouped block to the next: cards in a column, rows in a group | 28 / 2 | 1.40 |
| 6 | **20** | `page` | the screen inset, and any container's own inset | anchor | 1.43 |
| 7 | **28** | `section` | one section of a scroll to the next | anchor | 1.40 |
| 8 | **40** | `chapter` | parts of a page barely related, and the drop to a screen's first content | 20 * 2 | 1.43 |
| 9 | **56** | `part` | declared, currently unused, so the ladder has somewhere to go | 28 * 2 | 1.40 |

### The obvious answer, refused with numbers

The statistically common answer, and Material 3's own, is a linear 4dp ladder:
`4, 8, 12, 16, 20, 24, 28, 32, 40, 48`. I scored seven candidate ladders against the app's
real space literals, weighted by occurrence:

| ladder | steps | ratio spread | weighted displacement | sites moving 2dp or more |
|---|---|---|---|---|
| linear 4dp (the obvious answer) | 10 | 1.14 to 2.00 | 383dp | **161 (50%)** |
| M3 tokens 4,8,12,16,24,32,48 | 7 | 1.33 to 2.00 | 523dp | 192 (59%) |
| 3:2 from 4 | 8 | 1.33 to 2.00 | 435dp | 150 (46%) |
| **root 2 on 20 and 28** | **8** | **1.40 to 1.43** | **380dp** | **110 (34%)** |

The linear ladder loses on both axes at once. Its top steps are 14 to 17 percent apart, which
is below the threshold at which an author can tell two of them apart, and it moves half the
app's spacing anyway. Refused, and refused on measurement rather than on taste.
`design-v3.md` 15 is satisfied.

### The 4dp grid survives where it does work

`page`, `section`, `chapter` and `part` are all multiples of 4. `near` and `block` are exactly
half of `page` and `section`. `pair` and `tight` are exactly a quarter. `hair` is a stroke
measure. So the two off-grid steps are principled halvings of stated numbers rather than free
choices, and everything from `near` up resolves onto a 2dp lattice. **That is the honest
resolution of 0.1's contradiction: `design-v3.md` 6's "4dp base grid" becomes "root 2 on 20
and 28, resolving onto a 2dp lattice", which is what its own numbers were already doing.**

### The same ladder is the radius scale, and the concentric rule is exact

Fourteen shapes are declared today across seven radii: 8, 11, 12, 14, 16, 18, 28. They become
four steps of the ladder, plus `pill`.

| radius | ladder step | what takes it |
|---|---|---|
| **7** | `tight` | a mark, a badge, a swatch, a mood pill, a settings badge, the widget inner |
| **10** | `near` | a row, a button, a snackbar, a Momentum tile, an appearance tile |
| **20** | `page` | a card, the weekly banner |
| **28** | `section` | a sheet |
| `pill` | 50 percent | anything whose height is its identity |

The reason this works and is not a coincidence: **because each step is exactly half of the
step two above it, insetting a container by half its own radius makes the inner radius exactly
half the outer, at every level of the ladder.**

```
28 - 14 = 14      20 - 10 = 10      14 - 7 = 7      10 - 5 = 5
```

A row at radius 10 inside a card at radius 20, inset by `near` 10, is exactly concentric. A
card at radius 20 inside a sheet at radius 28 inset by `block` 14 is not, and that tells you
the sheet wants a `section` 28 inset around a `block`-radius container, which is the correct
answer anyway. **A nesting question that used to be a judgment is now arithmetic.** No report
proposed this; it falls out of the root-2-by-halving construction for free, and it is the
single strongest argument for that construction over a linear one.

### The same ladder is the travel scale

There is no `d1 / d2 / d3`. Motion distances are ladder steps, so the app has one set of
distances rather than two that must be kept in agreement.

| travel | step | used by |
|---|---|---|
| **2dp** | `hair` | nothing yet. Reserved. |
| **5dp** | `pair` | the maximum a Confirm may move. See 2.5. |
| **7dp** | `tight` | an element leaving or being replaced |
| **10dp** | `near` | an element arriving |

**Why 10dp for an arrival, derived rather than chosen.** `body` is 15sp on a 24sp line, so a
body line box is 24dp. A rise of more than half a line box reads as a line arriving from
somewhere else; less than half reads as a line settling onto its own baseline. 10dp is under
half. At 200 percent the line box is larger and the same 10dp reads as even more settled,
which is the correct direction. The current 16dp of `design-v3.md` 8.2 item 4 is over half a
line and nothing in any layout sits 16dp above where it lands, **which is why the entrance
currently reads as an effect rather than as typography.**

### The axis vocabulary

Adopted from Nathan Curtis, because it removes the second guess after "which step":

- **inset** padding inside a container, four sides equal
- **squish inset** an inset with the stack axis one step tighter than the inline axis
- **stack** the vertical gap between siblings
- **inline** the horizontal gap between siblings

A card's padding is a **squish inset at `page`**: 20 inline, 14 stack. That replaces today's
18 horizontal and 17 vertical, which is 1dp of squish, which is nothing.

### Every existing literal, mapped

| step | absorbs | occurrences |
|---|---|---|
| `hair` 2 | 1, 1.5, 2, 3 | 20 |
| `pair` 5 | 4, 5, 6 | 47 |
| `tight` 7 | 6, 7, 8 | 54 |
| `near` 10 | 9, 10, 11, 12 | 92 |
| `block` 14 | 13, 14, 15, 16 | 45 |
| `page` 20 | 17, 18, 20, 22 | 55 |
| `section` 28 | 24, 26, 28, 30, 32 | 26 |
| `chapter` 40 | 34, 36, 38, 40, 48, 50 | 15 |

**110 sites (34 percent) move by 2dp or more. 16 sites (5 percent) move by 4dp or more.**
Everything at 60dp and above is not a space at all; it is a component measure and goes to
`ClaritySizes.kt` unchanged.

The named tokens, with their deltas:

| token | today | becomes | delta |
|---|---|---|---|
| `screenPadding` | 20 | `page` 20 | 0 |
| `sectionGap` | 28 | `section` 28 | 0 |
| `cardPaddingHorizontal` | 18 | `page` 20 | +2 |
| `cardPaddingVertical` | 17 | `block` 14 | -3 |
| `cardGap` | 11 | `near` 10 | -1 |
| `sheetContentTop` | 18 | `page` 20 | +2 |
| `tabBarInset` | 17 | `page` 20 | +3 |
| `areaDot` 7, `sheetHandle` 34x4, `tabBarHeight` 61, `fabSize` 48, `minTouchTarget` 48, `swipeActionWidth` 66 | | sizes, unchanged | 0 |

## 1.2 The three kinds of dimension

This taxonomy is what makes the enforcement in Part 6 principled rather than a list of files
somebody argued for.

1. **Space.** A gap or an inset. Lives in `ClaritySpacing.kt`. Always on the ladder. Grows
   with text by 1.4's law.
2. **Size.** A component's own measure: a ring diameter, a dot, a handle, a bar height, a
   touch floor. Lives in `ClaritySizes.kt`. Off the ladder, fixed in dp, and **every entry
   carries a KDoc line citing the section of `design-v3.md` that fixes it.** The 159 file-local
   `private val`s move here.
3. **Stroke.** A drawn line width or a shadow offset. Lives in `ClarityStrokes.kt`. Five
   values: `hairline` 1, `rule` 1.5, `ring` 2, `heavy` 4, `focusRing` 6, each cited.

## 1.3 The type scale

**Ratio 5:4, the major third, quantized onto the platform's own `FontScaleConverter`
interpolation nodes wherever one is within reach.** Fourteen declared sizes become seven, plus
one off-ladder.

| step | sp | renders at 1.3 | growth | line box | line ratio |
|---|---|---|---|---|---|
| 1 | **10** | 13.00dp | 1.300x | 12 | 1.200 |
| 2 | **12** | 15.60dp | 1.300x | 16 | 1.333 |
| 3 | **15** | 18.30dp | 1.220x | 24 | 1.600 |
| 4 | **18** | 21.60dp | 1.200x | 24 | 1.333 |
| 5 | **24** | 26.40dp | 1.100x | 32 | 1.333 |
| 6 | **30** | 30.00dp | 1.000x | 36 | 1.200 |
| | **64** | 64.00dp | 1.000x | 68 | 1.063 |

Four of the seven sit exactly on an AOSP node (10, 12, 18, 24, 30), which means their growth
is read straight off Google's measured curve rather than interpolated between two of its
samples. **Every line box is a multiple of 4dp**, which is Relation 1 in 1.4.

Two sizes leave for cause. **8sp**, the Report's day initials, is 2.5sp below the app's own
stated floor and becomes 10. **10.5sp** becomes 10, which is a node, and gains 1.300x growth
instead of the 1.262x it has today.

### The roles: eleven, on seven sizes

Two roles at one size differing in weight, family and tracking is how hierarchy works when the
size range is short. Three roles pretending to be three sizes inside 1.6sp is not, and that is
what `label` 13, `sidehead` 13 and `caption` 12 currently are.

**Newsreader, the voice. Three roles.**

| role | sp | `opsz` | `wght` | tracking | line height | job |
|---|---|---|---|---|---|---|
| `voice` | 30 | 48 | 400 | -0.008em | 36sp | a sentence the engine wrote, and the loudest line on its page: the Report headline, the Momentum headline, the Pulse observation, an empty state |
| `title` | 24 | 38 | 400 | 0, `opsz` carries it | 32sp | a name, not a sentence: a page header, the Report's closing line, the Momentum stat figures with `tnum` |
| `read` | 18 | 18 | 400 light, 370 dark | 0, `opsz` carries it | 28sp | paragraphs: the Report sections, the About text |

**Hanken Grotesk, the interface. Eight roles on five sizes.**

| role | sp | `wght` | tracking | line height | job |
|---|---|---|---|---|---|
| `timer` | 64 | 250 | -0.030em | 68sp | the focus countdown, tabular, capped at 1.3x |
| `item` | 24 | 700 | -0.030em | 32sp | **the one active item on an area card. The dominant sans line in the app.** |
| `strong` | 18 | 600 | -0.010em | 24sp | sheet titles, Trail day headers, button labels, emphasis inside body. Collapses today's `title` 19 and `bodyStrong` 17 |
| `body` | 15 | 400 | +0.004em | 24sp | running prose, list rows, field text, snackbar message |
| `label` | 12 | 600 | +0.022em | 16sp | area labels, tab labels, chips |
| `sidehead` | 12 | 700 | +0.030em | 16sp | section labels, sentence case, above `label` at the same size for the reason 5.3 already argues |
| `caption` | 12 | 400 | +0.022em | 16sp | timestamps, footers, helper text |
| `micro` | 10 | 700 | +0.036em | 12sp | swipe labels, the Report's day initials |

Registered Hanken weight instances fall from six to **four**: 250, 400, 600, 700. Weight 500
and 650 leave, and with them the three `FontWeight(500)` overrides at `AreaCard.kt:159`,
`AreaSheets.kt:406` and `ColorPicker.kt:175` that `ClarityType.kt`'s own KDoc records as having
silently rendered the wrong weight for two phases.

### The tracking law

The app already has a scale here and does not know it. Fitting the nine sans tracking values in
`design-v3.md` 5.3:

> **t(size) = clamp( 0.032 - 0.054 * log2(size / 10.5), -0.030, +0.036 ) em**

| size | 5.3 states | law gives | residual |
|---|---|---|---|
| 10.5 | +0.0320 | +0.0320 | 0.0000 |
| 12 | +0.0220 | +0.0216 | -0.0004 |
| 13 | +0.0160 | +0.0154 | -0.0006 |
| 15 | +0.0040 | +0.0042 | +0.0002 |
| 17 | -0.0060 | -0.0055 | +0.0005 |
| 19 | -0.0140 | -0.0142 | -0.0002 |
| 21 | -0.0220 | -0.0220 | 0.0000 |
| 64 | -0.0300 | -0.0300 (clamped) | 0.0000 |

Worst residual 0.0006em, which is 0.01sp at body size. The clamp at -0.030 reproduces the timer
numeral exactly without the timer being an exception. **Nine hand-picked numbers are one line of
code, and once it is a line of code, adding a role computes its tracking instead of guessing it.**
Test T2 asserts this.

### What the variable axes buy

**`opsz` on Newsreader.** Optical size changes letterform shape, not metrics: small values open
apertures and thicken hairlines, large values refine details and close spacing. Setting `opsz`
*above* the size makes the face more delicate than the size asks for, which is the display look
this brand wants. The app does that today by accident, at five arbitrary ratios: 1.00x, 1.31x,
1.42x, 1.60x, 1.70x. Two rules replace them:

- **Read in paragraphs: `opsz` = size.** `read` at 18sp gets `opsz` 18.
- **Read once: `opsz` = min(72, size * 1.6).** `title` 24sp gets 38, `voice` 30sp gets 48.

And one correction the app is missing entirely: **`opsz` must follow the rendered size, not the
nominal one.** At 200 percent, `voice` renders near 39dp and still carries `opsz` 48, drawn for
a size it is no longer set at. Resolving typography through a composable that reads
`LocalDensity.fontScale` and recomputes `opsz` is a dozen lines, and it is the difference between
owning a variable font and shipping five static cuts of one.

**`wght` on Newsreader, dark-ground compensation.** Light text on a dark ground optically blooms.
The whole Report and the whole Pulse are Newsreader on `deepBlack`. The axis reaches 200, so the
correction is free: **`read` at 370 on Contemplative grounds, 400 on Daylight.** It changes no
contrast ratio, so the audit is undisturbed. It costs one extra registered Newsreader instance,
and it ships after the ladder, not with it.

**`wght` on Hanken.** The headroom above 700 stays unused on purpose. `item` at 24sp/700 against
`body` at 15sp/400 is a 1.6x size ratio and a 1.75x weight ratio at once. Going to 800 would buy
weight the design does not need and would push the sans past the serif in voice.

## 1.4 The relation between type and space, as two things that are exactly true

**Relation 1, the lattice.** Every type role's line box is a multiple of 4dp. Every space step
at or above `near` is a multiple of 4dp or exactly half of one. So text and space resolve onto
one 2dp lattice above `pair`, and a column of prose and the gaps between its blocks share a
rhythm instead of each having their own.

**Relation 2, the growth law.** This is the fix for 0.2, and it is four lines:

```kotlin
// The ratio the platform actually applies to body text at this density, which on API 34
// and up is a curve and below it is a straight line. Read from the density rather than
// assumed, so the two eras need no branch.
@Composable
fun spacingScale(): Float = with(LocalDensity.current) {
    val body = ClarityTypeScale.body.fontSize
    (body.toDp().value / body.value).coerceAtLeast(1f)
}
```

Spacing then grows on exactly the curve body text grows on:

| | 1.0 | 1.3 |
|---|---|---|
| `body` renders at | 15.0dp | 18.30dp |
| `section` gap, today (linear) | 28.0dp | 36.40dp |
| `section` gap, proposed | 28.0dp | 34.16dp |
| gap / body, today | 1.867 | **1.989** |
| gap / body, proposed | 1.867 | **1.867** |

Constant at every setting instead of drifting. Titles pull slightly closer to their own gaps as
text grows, which is correct: a headline at 200 percent does not need twice the air, and the
platform's own curve already says so.

**This deliberately breaks `TextSizeScaleTest`'s assertion that `spacingScaleFor(fontScale) ==
fontScale.coerceIn(1f, 2f)`.** That rule was written before anyone measured the platform's curve.
It is replaced by T5.

### What the type change buys, measured

| | today | proposed |
|---|---|---|
| declared sizes | 14 | 7 plus one off-ladder |
| declared roles | 14 | 11 |
| registered Hanken weights | 6 | 4 |
| Areas dominant / quietest, at 1.0 | 21 / 13 = **1.615** | 24 / 12 = **2.000** |
| same, at the phone's 1.3 setting | **1.454** | **1.692** |
| page name vs active item | title wins, 1.43x | **item wins**, on weight and family at equal size |
| smallest type in the app | 8sp | 10sp |

## 1.5 Headers and subheaders

There are **eight** header treatments in the app today, not five: Areas, Trail, Settings, pushed
screens, the Report, Momentum, sheets, and the Trail day header. And `Sidehead()` is bypassed
four times with `style = type.sidehead` set by hand.

**The clarifying observation: Momentum and the Report have no page header because their first
element is not a header, it is content.** The Report's 40sp hero and Momentum's 26sp opener are
sentences the corpus wrote about the person's week. Treating a loud content line as a screen
title is what produced two of the eight treatments, and it is why those two tabs do not announce
themselves the way the other two do.

### Role 1: the page header. One component, two modes.

```kotlin
@Composable
fun PageHeader(
    line: String,                               // always present
    mode: PageHeaderMode,                       // Named or Led
    eyebrow: String? = null,                    // caption 12sp, inkSecondary
    onBack: (() -> Unit)? = null,               // 48dp target, 22dp glyph, on the text edge
    actions: List<HeaderAction> = emptyList(),  // 0 to 3, 22dp glyphs at inkSecondary
)
```

- **`Named`**: `line` is the screen's name, set in `title`, serif 24sp. Areas, Trail, Settings,
  About, Archive, every pushed screen.
- **`Led`**: `line` is the engine's sentence, set in `voice`, serif 30sp, with `eyebrow` carrying
  the screen's identity. Momentum (`eyebrow = "Momentum"`), the Report
  (`eyebrow = "Clarity Report, week of August 21"`, the string that already exists).

Every page therefore opens with the same two-slot structure: an optional quiet line, then one
serif line. **One component instead of eight layouts.**

Fixed geometry, all from the ladder:

- Leading aligned, never centered. Centered is the platform default and the obvious answer; it is
  refused because a centered title with trailing glyphs has no true optical center, and a leading
  title puts the header's left edge on the same `page` inset as everything under it.
- Top of screen to the header line: `chapter` 40, or `page` 20 below a back chevron.
- Eyebrow to line: `tight` 7.
- Header to first content: `section` 28.
- Back glyph 22dp inside a 48dp target, offset by `-(48 - 22) / 2` so the glyph lands on the text
  edge. `PushedScreen.kt` already does this correctly and becomes the one implementation.

### Role 2: the section header, `Sidehead`

Sentence case at `sidehead` 12sp / 700 / +0.030em, a `pair` 5 gap, then a hairline to the trailing
edge, vertically centered on the label. Gold in the Report, `inkSecondary` in Daylight. `section`
28 above, `near` 10 below. **This becomes the only way to reach `type.sidehead`**, which absorbs
the Trail day header and the four hand-rolled sites.

### Role 3: the group label

`label` 12sp / 600 / +0.022em, no rule, `tight` 7 below. Field labels, a short run of rows inside a
sheet. The thing a sidehead would be too loud for.

### Role 4: the eyebrow

`caption` 12sp / 400 / +0.022em at `inkSecondary`, one line, only ever directly above a `voice` line,
only inside `PageHeader`. It scopes a headline; it is never a standalone label. **This forecloses
`design-v3.md` 15.1's "badge above a centered headline" tell by making the eyebrow structurally
unable to appear alone**, which is better than a reviewer noticing.

### Screen by screen

| screen | role | eyebrow | actions |
|---|---|---|---|
| Areas | `Named` "Areas" | none | archive, settings |
| Momentum | `Led`, engine headline | "Momentum" | none |
| Report | `Led`, engine headline | `Clarity Report, week of ...` | history, regenerate, copy |
| Trail | `Named` "Trail" | none | none |
| Settings, About, Archive, all pushed | `Named` | none | back |
| Focus session, Pulse, Focus complete | **none. Declared exceptions.** | | |
| sheets | not a page. `strong` 18 under the handle. | | |

The three Contemplative surfaces are declared exceptions rather than tolerated ones, and test H1
requires each to carry the sentence of `design-v3.md` 11 that permits it.


---

# PART 2. THE MOTION SYSTEM

## 2.1 The eight jobs

A framing that refuses nothing is decoration with a vocabulary. **The test of this set is that
it refuses three animations that are currently specified or built.**

| job | the question it answers | may travel | may stagger | may loop |
|---|---|---|---|---|
| **Orient** | where did this come from, where does it go back to | yes, a real path | no | no |
| **Relate** | is this thing that thing | yes, between two measured rects | no | no |
| **Confirm** | did my touch land, and on what | `pair` 5dp max | no | no |
| **Track** | am I still holding it, what happens if I let go | 1:1 with the finger | n/a | no |
| **Reveal** | in what order should I read this | `near` 10dp max | **yes, only this one** | no |
| **Replace** | what is here now, what was here before | **no. The slot is fixed.** | no | no |
| **Depict** | what is the value, right now | it *is* the value | no | conditionally |
| **Notice** | something changed that I did not do | no | no | no |

### Depict and Notice are the most important line in this document

`design-v3.md` 16.6 already knows this distinction and has to re-derive it by hand twenty-eight
times. It keeps the focus ring depleting in calm mode and writes "information, not decoration".
It keeps the snackbar's depleting line and writes the same phrase. It keeps the Live Update
track. Meanwhile the shimmer holds and the glow freezes. **That is one correct judgment made five
separate times with no name for the thing being judged.**

Named, the rule is mechanical:

> **Depict means the animation *is* the value, and suppressing it destroys information. A Depict
> is never turned off. It may become discrete, 1Hz instead of continuous, but never absent.**
>
> **Notice means something changed and the app is pointing at it once. Losing a Notice costs a
> signal, not a fact, so a Notice may be shortened or dropped.**

Every row of 16.6 falls out in one step. Ring, snackbar line, Live Update track, session-extended
arc: Depict, kept. Shimmer, transition mark, promotion wash: Notice, shortenable.

### Confirm and Track split on physics, not on semantics

A Confirm is a spring from A to B after the fact. A Track is bound to a pointer for as long as
the pointer is down and **has no spring at all while the finger is on the glass**. Its only spring
is the release. Conflating them is exactly how apps ship laggy drag. `SwipeableRow` already gets
this right; naming it stops the next person getting it wrong.

### Orient carries a test, and the test retires an exception

> **An Orient must have a nameable inverse, and the inverse must be played on the way back. If you
> cannot say what the reverse motion is, there is no spatial relationship and you may not use
> spatial motion.**

The sheet's inverse is falling to the bottom edge. The Focus world's inverse is the room lighting
again. The tab pill's inverse is traveling back.

`design-v3.md` 8.2 item 24 refuses a slide between tabs because "these are four views of the same
data". Correct, and currently a hand-argued exception attached to one animation. Under the test it
is a consequence: there is no "back" from Report to Areas, so there is no inverse, so there is no
Orient, so there is no travel. **The tab change is a Replace, and Replace's own rule produces the
no-slide answer without anyone arguing it again.**

### Replace is the job that was missing, and it is the one this app most needs

Look at what the app is: one active item per area, everything else out of sight. The single hardest
thing it has to communicate is *the thing you just finished is gone and a different thing is now
here*, in the same place on the same card. `design-v3.md` calls the queue promotion "the hero" and
says "if only one thing is polished, it is this", then has to describe it as a bundle of five
behaviors because there is no job it belongs to.

It is not Confirm, the tap was already confirmed. Not Reveal, nothing is being read into being for
the first time. Not Notice, I caused it. It is a **substitution**, and for an audience whose working
memory is the thing under load, making a substitution legible is the highest-value animation in the
product.

Three rules, all testable:

1. **The slot does not move.**
2. **The two states overlap in time**, so the eye tracks one thing changing rather than watching two
   events.
3. **They are never both at full opacity.** 2.4 makes this automatic.

### What the taxonomy refuses

1. **The focus glow breathing, 8.2 item 8.** An 8-second opacity loop inside a 25-minute session
   encodes nothing; 0.85 to 1.0 is not a value. **It survives only bound to a real binary:** it runs
   while the session runs and stops the instant the session stops, at which point it is a Depict of
   "this is live". If it runs regardless of session state it is decoration, and `design-v3.md` 15.3
   already refuses it by name.
2. **The tutorial ring pulse, 8.2 item 19.** An infinite 2-second pulse in the peripheral field while
   a person reads instructional text. Motion captures the eye involuntarily, so a loop beside prose is
   a tax collected without consent. It is a Notice, **and a Notice fires once.** Item 27, the transition
   mark, is the correctly designed version of the same job: brighten once and hold, no color change, no
   repeat. Item 19 becomes item 27.
3. **The expanding circle in the completion bloom, 8.2 item 9.** The ring collapsing is a Depict
   reaching zero. The check appearing is a Replace in the ring's slot. The soft circle expanding from
   center has no job, and `design-v3.md` 14 forbids "celebration of any kind". The bloom becomes: the
   arc runs to zero, then the ring is replaced by the check. Shorter, quieter, and it stops being the
   one place the app celebrates.

### What the taxonomy adds, and why it costs nothing

Here is the resolution of the tension between the owner's "a lot of motion, throughout" and 8.4's "an
entrance fires once per session".

**8.4 is right about entrances, and the app has no transitions at all.** A Reveal announces "this is
new" and should fire once, because on the twentieth open it is a toll paid by the reader least able to
afford it. But an Orient or a Relate says "you moved", and you moved *every time*. **The reason the app
is dead on the second open of a tab is not the missing entrance. It is that every navigation in the app
is a hard cut.**

Relate is entirely unused, and every navigation here has a real relationship to express: the area card
contains the area sheet; the item title exists on both the card and the focus screen; the Report becomes
a row in the Report history; Momentum's area tile *is* the area.

**And a Relate costs zero net latency.** It is caused by the tap and it overlaps a navigation that was
going to happen anyway. An entrance is pure added delay. That is the argument that gives the owner
motion everywhere without breaking 8.4's reasoning.

## 2.2 The seven springs

**Named for the job, never for the speed.** `springSnappy` invites "is this snappier than that?",
which is a taste question with no correct answer, and taste questions at call sites are how the app
got twelve durations. `spatialConfirm` invites "is this a Confirm?", which has one answer.
**That rename is the standardization.**

### Ground truth

Material's docs describe the schemes and publish no numbers. Extracted with `javap` from
`material3-runtime.jar` in this machine's Gradle cache, classes
`MotionScheme$ExpressiveMotionSchemeImpl` and `$StandardMotionSchemeImpl`:

| token | Expressive | Standard |
|---|---|---|
| `defaultSpatialSpec` | **0.8 / 380** | 0.9 / 700 |
| `fastSpatialSpec` | **0.6 / 800** | 0.9 / 1400 |
| `slowSpatialSpec` | **0.8 / 200** | 0.9 / 300 |
| `defaultEffectsSpec` | 1.0 / 1600 | 1.0 / 1600 |
| `fastEffectsSpec` | 1.0 / 3800 | 1.0 / 3800 |
| `slowEffectsSpec` | 1.0 / 800 | 1.0 / 800 |

**The two schemes share all three effects springs and differ only in the three spatial ones.**
"Expressive" in Material means exactly one thing: spatial motion is looser and slower. Opacity and
color are identical in both. That is load-bearing for Part 5: Material's own model of "calmer" changes
the spatial axis and leaves the effects axis alone, and the spatial axis is also where the vestibular
and attention costs live. The reduced form has a structure to inherit rather than one to invent.

### The set

**Spatial. Position, size, shape.**

```kotlin
/** Confirm and Notice. My finger, right now. */
fun <T> spatialConfirm(): FiniteAnimationSpec<T> = spring(dampingRatio = 0.82f, stiffness = 900f)

/** Orient and Relate. A thing moving along a real path. */
fun <T> spatialTravel(): FiniteAnimationSpec<T> = spring(dampingRatio = 0.86f, stiffness = 380f)

/** A surface larger than a third of the viewport coming to rest. */
fun <T> spatialSettle(): FiniteAnimationSpec<T> = spring(dampingRatio = 0.90f, stiffness = 240f)

/** A Track's release. The only spring in the app that is ever handed an initialVelocity. */
fun <T> spatialRelease(): FiniteAnimationSpec<T> = spring(dampingRatio = 1.00f, stiffness = 900f)
```

**Effects. Opacity, color, elevation. Anything whose overshoot would be a bug.** All critically
damped, all three keeping Material's exact values, because a spring that cannot bounce has no
character to differentiate, and differing would only guarantee that a Material bottom sheet and a
Clarity sheet disagree forever.

```kotlin
/** Confirm's ink half, and the outgoing half of a Replace. = M3 fastEffects */
fun <T> effectsInstant(): FiniteAnimationSpec<T> = spring(1f, 3800f)

/** Every alpha on content, everywhere. Reveal, Replace's incoming half, Notice. = M3 defaultEffects */
fun <T> effectsChange(): FiniteAnimationSpec<T> = spring(1f, 1600f)

/** A ground, a scrim, a wash. Never content. = M3 slowEffects, currently absent */
fun <T> effectsArrive(): FiniteAnimationSpec<T> = spring(1f, 800f)
```

**And an eighth entry that is a documented absence, in the same file:**

```kotlin
// Depict has no spring, deliberately, and this comment is the token.
//
// A spring settles toward a target. A depiction has no target, it has a current value, so a
// spring on the focus ring would make the ring lag the clock it is drawing. Depiction is a
// ticker, never an animation spec. If you are reaching for a spring here, the thing you are
// animating is information and you are about to make it wrong.
```

`design-v3.md` already treats a recorded absence as worth more than a tell. This is that move applied
to motion, and it is what lets "there are seven springs and every animation uses one of them" survive
contact with the one job that must not use one.

### Measured behavior

Simulated against Compose's `SpringSimulation` model at unit mass, `naturalFreq = sqrt(stiffness)`,
Dp visibility threshold 0.1dp:

| token | time constant | **98% (perceived)** | 99.5% | settle, 16dp | overshoot | overshoot at 300dp |
|---|---|---|---|---|---|---|
| `spatialConfirm` | 40.7ms | **131ms** | 142ms | 228ms | 1.11% | 3.33dp |
| `spatialTravel` | 59.6ms | **220ms** | 246ms | 243ms | 0.50% | 1.51dp |
| `spatialSettle` | 71.7ms | **303ms** | 353ms | 347ms | 0.15% | 0.46dp |
| `spatialRelease` | 33.3ms | **195ms** | 231ms | 216ms | 0 | 0 |
| `effectsInstant` | 16.2ms | **95ms** | 121ms | 117ms | 0 | 0 |
| `effectsChange` | 25.0ms | **146ms** | 186ms | 180ms | 0 | 0 |
| `effectsArrive` | 35.4ms | **206ms** | 263ms | 254ms | 0 | 0 |

Seven perceived durations: **95, 131, 146, 195, 206, 220, 303ms.** An ordered ladder, every rung inside
the 100 to 400ms band the literature supports, and the closest pair is across families.

### Why these values and not Material's

- **`spatialConfirm` is faster and calmer than M3 Expressive's `fastSpatial` at the same time.** M3E
  `fastSpatial` (0.6 / 800) overshoots **9.48 percent** on every press. "A bounce on every hover or
  press, rather than overshoot reserved for weight" is on `design-v3.md` 15.1's own tell list. Raising
  stiffness to 900 and damping to 0.82 makes the press *quicker* (131ms perceived against 211ms) while
  making it *quieter* (1.11 percent, which on a 5dp travel is 0.055dp and physically unobservable).
  **Expressive systems buy character with bounce. This one buys it with speed.** That is the inversion
  this audience needs.
- **`spatialTravel` keeps Material's 380 exactly**, so a stock Material component and a hand-written
  container transform arrive together, and raises damping from 0.8 to 0.86 so a full surface rebounds
  1.51dp instead of 4.55dp. Material's timing with Material's rebound removed, and only on large things.
- **`spatialSettle` is 240, not `springGentle`'s 200.** At 0.9 / 200 a 120dp travel does not settle for
  over 800ms, which is past every threshold in the literature and past the point NN/g calls a drag. 240
  pulls the perceived time to 303ms.
- **`spatialRelease` shares `spatialConfirm`'s stiffness because it is the same event, a finger leaving,
  and is critically damped because it is the only spring the app ever hands a velocity to.** Simulated
  return to rest from 40dp carrying a 2000 dp/s fling: `spatialConfirm` rebounds **7.29dp**,
  `spatialTravel` rebounds **17.68dp**, `spatialRelease` rebounds **2.19dp** and is at rest in 245ms.
  2.19dp is under the `pair` 5dp Confirm cap, so a hard flick never throws a row visibly past its home.

### Why velocity handoff is a call-site argument and not four more tokens

`initialVelocity` is a parameter of `animateTo`, not of the spec. So "the swipe carries its release
velocity" needs no `gestureSettle` and no `gestureCommit` token. It needs one rule:

> **Any animation started from a pointer release passes the release velocity, and any spring handed a
> velocity is `spatialRelease`.**

That is one token instead of four, and it is greppable: `initialVelocity` on any spec other than
`spatialRelease` is a review failure.

## 2.3 The delay ladder, and the one hold

Springs handle everything with a target. Three things remain that are genuinely durations.

| token | ms | job |
|---|---|---|
| **`markStep`** | **30** | the sweep across a row of marks: the 14 Pulse dots, the 14 Momentum dots, the 7 ribbon days, the 7 focus-pattern squares |
| **`rowStep`** | **60** | rows and cards a person reads. Capped at 4 steps |
| **`bandStep`** | **120** | the gap between one band of a screen and the next. See 3.1 |
| **`hold`** | **900** | ceremony only. One call site: the onboarding closing line |

Ratio 2 between the three delay steps. `bandStep` 120 is deliberately not a fourth free number: it is
`rowStep` doubled twice, and it is also the point where the delay ladder and the spring ladder meet,
because 120ms is close to `spatialConfirm`'s perceived 131ms, so a band gap and the app's quickest
movement are the same length and there is one seam rather than two scales floating free.

**Retirements: 35, 45, 50, 90, 150, 180, 200, 220, 240, 250, 400, 450, 600, 780, 1000 all go.** Twelve
duration integers and five staggers become four numbers, and `REDUCED_MILLIS` disappears from six files
because Part 5 removes the concept.

**Total motion numbers in the app: seven springs, three delays, one hold. Eleven, down from six curves,
twelve durations and five staggers.**

## 2.4 The rule: which token does a given animation take

The job chooses the spring. The job is a fact about what the animation is for, not a judgment about how
it should feel.

| job | property | token |
|---|---|---|
| Orient, Relate | position, size, bounds | `spatialTravel`, or `spatialSettle` if the moving element exceeds one third of the viewport |
| Confirm | scale, position | `spatialConfirm` |
| Confirm | ink, fill, tint | `effectsInstant` |
| Track | anything, while a pointer is down | **no spring. 1:1, no filter, no smoothing.** |
| Track | release | `spatialRelease` with `initialVelocity` |
| Reveal | alpha | `effectsChange` |
| Reveal | the `near` 10dp rise | `spatialTravel` |
| Reveal | order | `rowStep` 60, capped at 4, from a band delay |
| Replace | outgoing alpha | `effectsInstant` |
| Replace | incoming alpha | `effectsChange` |
| Notice | brightness, color | `effectsChange` |
| Depict | the value | **no spring. A ticker.** |
| ground, scrim, wash | alpha, color | `effectsArrive` |
| a drawn stroke caused by a touch | stroke progress | `spatialConfirm` |
| a drawn stroke that is part of an arrival | stroke progress | `spatialTravel` |

### The fade-through falls out of the table for free

Every crossfade in this app is a Replace, and Replace's outgoing half is `effectsInstant` while its
incoming half is `effectsChange`. That asymmetry, and nothing else, produces a Material-style fade
*through* rather than a 50/50 double exposure, with **no crossover constant, no delay, and no sequencing
code.** Both animations start at t = 0. Simulated:

| t | outgoing alpha | incoming alpha | combined |
|---|---|---|---|
| 0ms | 1.000 | 0.000 | 1.000 |
| 20ms | 0.651 | 0.191 | 0.842 |
| **40ms** | 0.294 | 0.475 | **0.769** |
| 60ms | 0.116 | 0.692 | 0.808 |
| 100ms | 0.015 | 0.908 | 0.923 |
| 146ms | 0.001 | 0.980 | 0.981 |

The combined opacity **dips to 0.769**, which is a fade through the ground. Running both halves on
`effectsChange` instead gives a combined opacity of exactly 1.000 at every instant, which is the
double exposure the app currently ships on its tab bar: at the midpoint the screen is 50 percent
Areas and 50 percent Report.

**Two springs already in the set replace a hand-written crossover constant, a 180ms tween, and the
argument about what percentage to sequence at.** That is what "efficient" means in this document.

## 2.5 Distance does not choose the spring. Area does.

> **The job chooses the spring, and the spring handles the distance.**

That is not a simplification, it is what the physics does. `spatialTravel` settles an 8dp travel in
about 480ms and a 400dp travel in about 530ms. Fifty times the distance, twelve percent more time,
because a spring's envelope decays exponentially and the time to fall from amplitude A to threshold
theta is `ln(A / theta) / (zeta * omega_n)`, which is logarithmic in distance. Each tenfold increase
in travel costs one extra `2.303 * tau`:

| token | cost of 10x the distance |
|---|---|
| `spatialConfirm` | +94ms |
| `spatialTravel` | +137ms |
| `spatialSettle` | +165ms |

**That logarithmic curve is exactly what the duration-based design systems spent years hand-building.**
Carbon ships a Motion Generator to compute a per-element duration from its distance. A spring computes
that curve exactly, for free, at every call site, which is the real reason systems stop writing
distance-to-duration tables when they move to springs. **A distance rule requires a judgment at every
call site, which is the mechanism that produced twelve durations. A job rule requires a lookup.**

### The exception, and it is area rather than distance

The vestibular literature is consistent that the cost of motion scales with the fraction of the visual
field that moves, not with how far it moves. Both web.dev and A List Apart put "large areas of motion"
first, ahead of speed and ahead of distance.

> - A moving element under **one third** of the viewport takes `spatialTravel`.
> - **One third to two thirds** takes `spatialSettle`.
> - **Over two thirds may not translate at all.** It may scale within 3 percent, or crossfade, and
>   nothing else.

That clause forbids full-screen slides, which are simultaneously the most common premium-app move and
the most expensive one for this audience. It also generalizes, correctly and retroactively, the
reasoning behind 8.2 item 24 and behind the Focus world's scale-from-0.97 rather than a slide.

**One declared exception: the onboarding iris**, which is a radial mask rather than a translation, fires
once in a person's entire use of the app, and is covered in 4.12.

### Travel is capped by job

| job | maximum travel | why that number |
|---|---|---|
| Confirm | **`pair` 5dp** | must stay inside a 48dp target so the hit rect never moves under a finger already on it |
| Notice | **0dp** | it is a brightness change and it must not pull the eye |
| Reveal | **`near` 10dp** | under half a body line box. See 1.1 |
| Replace | **0dp** | the slot is fixed. That is the definition |
| Track | the finger | 1:1 |
| Orient, Relate | the real distance between two real positions | **never an invented one** |

### Judge a spring by 98 percent, not by settle time

`springStandard` covers 98 percent of its travel in 193ms and does not satisfy Compose's equilibrium
test until 484ms. The literature's "under 400ms, 500ms is a drag" is about *perceived* duration.
Compose's Dp visibility threshold is 0.1dp, which on a 300dp travel is 0.03 percent, three time
constants tighter than perception needs. If a settle time ever matters for chaining, pass
`visibilityThreshold = 1.dp` and the reported duration collapses toward the perceived one. Otherwise
ignore `getDurationNanos`; it will only scare you.

## 2.6 What must never move

Eight refusals, in the register of `design-v3.md` 15.3, each with a cost attached rather than a
preference.

1. **Text that is being read.** Once a line of prose is legible at full opacity it does not move again.
   No reflow, no settle, no shift under a scroll. For a reader with attention or working-memory load,
   losing your place in a paragraph costs the paragraph. This forbids staggering a block of prose line
   by line, animating text size, and any layout change that displaces text already on screen.
2. **Anything in the peripheral field while something central is being read.** No animation in the outer
   20 percent while the center holds prose. The eye is pulled to movement involuntarily, so peripheral
   motion during reading is a tax collected without consent. Forbids an animating tab bar while the
   Report is open, a shimmer in a list behind a sheet, and the tutorial ring pulse.
3. **Anything that loops.** No infinite animation anywhere, with the single conditional exception of the
   focus glow, and only while bound to a running session. **The placeholder shimmer gets a threshold
   rather than a ban: if the wait has a known ceiling under 400ms, hold the placeholder still and start
   nothing.** This is a local Room database. A shimmer that appears for 80ms is worse than no shimmer.
4. **The active item title on the area card.** It may crossfade during a Replace and travel during a
   Relate. It may never scale, jitter, pulse, or join a Reveal stagger after its first arrival. It is the
   one string the app exists to show.
5. **Anything a person has to hit.** No target moves while it remains tappable. A control that is
   animating is either non-interactive for the duration or moves in a way that does not change its hit
   rect. This is the actual reason a FAB press is a scale and not a lift, and why Confirm's travel is
   capped at 5dp.
6. **The ground.** Canvases, washes and gradients never animate on their own. The Contemplative radials
   and the Pulse's day shift move on a timescale of hours, which is not motion. Forbids animated
   gradients, animated noise, and moving specks.
7. **Anything that would collide at 200 percent font scale.** If a Reveal's 10dp rise would cause overlap
   at the largest supported size, the rise is dropped for that element. **Motion yields to legibility,
   always, and it yields silently rather than by changing the layout.**
8. **Numbers being compared.** The three Momentum stats may roll on arrival and never re-roll while on
   screen. **A figure that changes while a person is looking at it changes by replacement, never by
   counting**, because counting through wrong intermediate values is a lie for as long as it lasts. This
   forbids the single most common "premium" data animation in existence, which is reason enough under
   `design-v3.md` 15 and correct on the merits besides.


---

# PART 3. THE ENTRANCE GRAMMAR

## 3.1 Three bands. A band is a rank, not a position.

| band | delay on a screen | delay in a sheet | what belongs in it |
|---|---|---|---|
| **ANCHOR** | **0** | **0** | what the screen *is*. The header line, and the single element the screen exists to show |
| **CONTENT** | **120** | **60** | what a person reads. Staggered internally at `rowStep` 60, capped at 4 steps |
| **OFFER** | **240** | **120** | what a person may act on. The FAB, the tab bar on cold start |

**A sheet's bands are the screen's bands one step down the delay ladder**, for the same reason a
sheet's travel is one step down the space ladder: a sheet is one band of a screen, and its motion is
proportional to it. No new number.

## 3.2 Two rules that do the work

**Only content travels. Chrome fades.** Chips, glyph buttons, the back chevron, filter rows, the tab
bar and the FAB arrive by opacity alone, on `effectsChange`, at their band's delay. Anything a person
*reads* arrives by opacity on `effectsChange` **plus** a `near` 10dp rise on `spatialTravel`.

This is how the eye gets led rather than dumped on: at any instant during an entrance, **exactly one
class of element is in motion, and it is the class you are supposed to be reading, in the order you
are supposed to read it.** It also solves pop-in, because chrome that arrived last in time would
materialize in the middle of a settled screen and read as a bug. Chrome arrives early and is simply
not moving.

**An element inside a container that is itself traveling does not travel. It fades.** A sheet's
contents fade while the sheet rises. A pushed screen's contents fade while the screen scales. Travel
resumes only when the container is at rest. One binary at every call site: *is my container moving?*
Yes, `travel = 0.dp`. No, `travel = Space.near` on a screen and `Space.tight` in a sheet.

## 3.3 Three guarantees, each testable

**1. Nothing is ever composed late.** An entrance is a `graphicsLayer` on already-composed,
already-measured, already-laid-out content, which is how `Modifier.clarityEntrance` already works and
does not change. No `AnimatedVisibility` gating a screen's content, no `delay()` in front of a
`LaunchedEffect` that emits state, no element that does not exist until an animation says so.
**TalkBack reads the full screen at frame one regardless of alpha**, and so does the accessibility
tree, and so does a screenshot test.

**2. Opacity finishes first, everywhere, always.** Every alpha on content in this app runs on
`effectsChange`, which is 98 percent complete in **146ms** from its own start. Only position, size,
color and stroke take longer. **Nothing in this app is ever hard to read for longer than 146ms after
its band begins.** That is the whole answer to "motion that delays comprehension is a defect however
good it looks", and it is greppable: an alpha on any spec other than `effectsChange` outside a ground,
a scrim or a wash is a review failure. Test M2.

**3. The budget is measured to the fold: 506ms legible, 580ms at rest.**

```
ANCHOR       delay   0  + effectsChange 146              = legible at 146ms
CONTENT[0]   delay 120  + 146                            = legible at 266ms
CONTENT[4]   delay 120 + 4x60 = 360, + 146               = legible at 506ms
                                     + spatialTravel 220 = at rest at 580ms
OFFER        delay 240  + 146                            = legible at 386ms
```

Content **below the fold** may continue the ladder past the cap, because a scroll gesture takes longer
than 200ms to even begin, so nothing below the fold can be looked at before it has arrived. That is
what lets Momentum and the Report run past 580ms without breaking the rule.

**And the rule that closes the last hole: the first scroll cancels the entrance.** Any element whose
delay has not yet elapsed when the user starts scrolling has its delay collapsed to zero and fades on
`effectsChange`. Scrolling means reading, and reading beats arriving. One boolean of state, and it
eliminates the entire class of scroll-triggered-animation defects, pop-in included.

## 3.4 The mark sweep

A row of marks is **one element with an internal sweep**, not N staggered children. Its arrival is one
entry in a timeline; the sweep across it is a property of that one element.

> **The sweep takes `markStep` 30ms per mark, capped at 210ms for the whole row. A row of more than 8
> marks divides 210ms by its own length.**

So the 14-day Pulse dot row and the 14-day Momentum dot row sweep at 16.2ms per dot, and the 7-day
ribbon and the 7 focus-pattern squares sweep at 30ms per mark. **Every mark row in the app finishes its
sweep in 210ms or less**, which is why they are the same gesture even though they are different lengths.
This is also a frame-budget rule: see Part 7.

## 3.5 When an entrance fires, and when it does not

**It fires only when all five are true:**

1. This is the first time this surface has been shown **in this app session**, meaning process lifetime,
   per 8.4. A rotation, a theme change and a text-size change are not new sessions.
2. The surface is **arriving**, not returning from something drawn over it.
3. **The content is loaded.** An entrance never plays over a shimmer. The shimmer to content swap is a
   Replace and it *spends* the entrance. This is a real defect class on cold start today: the Areas
   entrance is armed by composition, so a slow projection would animate an empty screen in and then have
   cards appear afterward. **The entrance must be armed by the first non-loading state, not by
   composition.**
4. It is not the Areas screen sitting **behind the onboarding iris.** Beat 3 composes the real Areas
   screen live behind black, so its entrance would fire unseen, or worse, fire *as* the iris opens and
   double the motion at the single most important moment in the app's life. **The reveal marks the Areas
   tab's entrance flag spent.** Currently unhandled.
5. Calm mode no longer suppresses it. See Part 5, and Part 8 item 12.

**It fires again, once, in exactly one case:** the Report, when the report being shown changes. 8.4's
single content exception, unchanged.

**It never fires on:** a tab return, a rotation or theme change, a sheet closing back onto the screen, a
pushed screen popping back onto the screen, a scroll, a data update while the screen is open, or a
paged-in batch of Trail rows.

## 3.6 The four movements, and nothing else

A tab change and a push can now never be confused, **because one of them has a z-axis and the other has
none.**

### LATERAL. Tab to tab. A Replace.

| | |
|---|---|
| outgoing | alpha 1 to 0 on `effectsInstant` |
| incoming | alpha 0 to 1 on `effectsChange` |
| travel | none |
| scale | none |
| perceived | 146ms, combined opacity dipping to 0.769 at 40ms |

**I refuse Material's 92 to 100 percent incoming scale.** 8.2 item 24 rules out a slide because "a slide
implies a spatial relationship between tabs and there is none". A scale implies the same relationship on
a different axis: it says one of these four is behind the other. It is also a full-screen plane shift,
named on the vestibular trigger list. Sequenced opacity delivers the entire perceptual benefit with no
geometry at all. The obvious answer, named and beaten.

### DEEPER and SHALLOWER. A push and its pop.

| | in (Deeper) | out (Shallower) |
|---|---|---|
| incoming | scale 0.96 to 1.00 `spatialTravel`, alpha `effectsChange` | scale 1.04 to 1.00, alpha `effectsChange` |
| outgoing | scale 1.00 to 1.04 `spatialTravel`, alpha `effectsInstant` | scale 1.00 to 0.96, alpha `effectsInstant` |
| axis | depth. Nothing moves in x or y | depth |

**Depth, not a horizontal slide.** The horizontal slide is the statistically common answer and it is
refused twice: horizontal motion in the peripheral field is a named vestibular trigger, and this app has
already spent horizontal travel on the swipe gesture, so a screen sliding in from the right would collide
with the one gesture on an area card that means something. Depth is free of both, and it is the axis the
platform's own predictive back already animates, **so a pop by button and a pop by gesture are the same
movement.**

Both directions run on the same springs and therefore take the same time. `design-v3.md` has no rule that
going in should be longer than coming out, and inventing one would be inventing a number.

### LIFT. A sheet.

Sheet travel from the bottom edge on `spatialSettle`. **The scrim's opacity is a function of the sheet's
position, not of time:**

```
scrimAlpha = 0.42f * sheetTravelFraction
```

Today the scrim runs its own 200ms fade in and 180ms fade out while the sheet runs a spring, so the room
darkens first and the sheet arrives into an already dark room, which reads as the app preparing rather
than as a thing arriving. Binding the scrim to position is physically true, it is free, and it is
automatically correct on a dragged dismiss, an interrupted drag, a fling and a predictive back gesture,
none of which a timed fade handles. **It deletes both of 8.2's scrim numbers and needs no token at all.**

**The two-thirds rule does not forbid this**, and the distinction is worth stating so nobody has to argue
it later: the area rule in 2.5 governs elements that move *within* the viewport. A surface entering from
the edge it is anchored to is an Orient with a nameable inverse and the platform's own movement, and its
travel is the real distance between two real positions.

### WORLD. Daylight to Contemplative. Three doors.

> **A world change is carried by whichever of exactly three doors you came through.**
>
> **The dim.** The Daylight room goes out where it stands and the dark comes up in its place. Nothing
> travels. Outgoing alpha on `effectsInstant`, incoming alpha on `effectsChange`, incoming content scaling
> 0.97 to 1.00 on `spatialSettle`. Used when the Contemplative surface *replaces* the app: **the focus
> session and the focus chooser, and nothing else.**
>
> **The lift.** A dark room rises from the bottom edge over the position-linked scrim, and Daylight stays
> visible behind it. `spatialSettle`. Used when the Contemplative surface is a *moment* you will step back
> out of: **the Pulse, and nothing else.**
>
> **The iris.** A circular reveal from center, radius 0 to `hypot(w, h) / 2` on `spatialSettle`, which is
> 303ms perceived and about 630ms to visually complete at that radius. Used **exactly once in a person's
> entire use of the app**, at the end of onboarding beat 3. It is never used again, and that is precisely
> what makes it mean something.
>
> **And the Report gets none of them, because the Report is a tab.**

That last line resolves the contradiction rather than papering over it. The Report is reached by LATERAL,
which lands you on a dark ground with nothing on it. **The darkness is not the event. The Report being
built on it is.** That is why the Report has the longest and most elaborate entrance in the app and no
world transition at all, and it is why the ribbon draw is the animation this design is willing to spend
on. A world transition in front of the Report would be a second ceremony competing with the one that
matters.

---

# PART 4. THE THIRTEEN SURFACES

Notation. Springs: `sConf` = `spatialConfirm`, `sTrav` = `spatialTravel`, `sSett` = `spatialSettle`,
`sRel` = `spatialRelease`, `eInst` = `effectsInstant`, `eChg` = `effectsChange`, `eArr` = `effectsArrive`.
Delays: `markStep` 30, `rowStep` 60, `bandStep` 120. Travel: `pair` 5, `tight` 7, `near` 10.
Every number below is a token. Nothing here invents one.

## 4.1 Areas

Arrives by LATERAL, or by cold start.

| # | element | delay | alpha | travel | other | why here |
|---|---|---|---|---|---|---|
| 1 | `Areas`, `title` serif 24 | **0** | `eChg` | `near` 10, `sTrav` | | It names the room. It is the only thing on screen at 146ms and it is fully readable then |
| 2 | weekly banner | **120** | `eChg` | `near` 10 | | The only *new information* on the screen. It precedes the cards because it is the sentence about your week; the cards are the state you already know |
| 3 | area card 1 | 180 | `eChg` | `near` 10 | | The three-second glance test is about this card |
| 4 | card 2 | 240 | `eChg` | `near` 10 | | |
| 5 | card 3 | 300 | `eChg` | `near` 10 | | |
| 6 | card 4 and beyond | **360, capped** | `eChg` | `near` 10 | | Five cards fill the screen, so the cap and the fold coincide |
| 7 | Focus / Pulse / Inbox chips | 120 | `eChg` | **none** | | Chrome. Present while the cards are still arriving, competing with nothing. They sit above the banner spatially, so arriving them late would pop in |
| 8 | archive and settings glyphs | 120 | `eChg` | none | | Chrome |
| 9 | FAB | **240** | `eChg` | none | scale 0.90 to 1.00 on `sConf`, from its own center | Last, because it is an offer rather than information. The one control allowed a scale: a circle in a corner, not a plane |
| 10 | tab bar | 240 | `eChg` | none | | Cold start only |

**Legible at 506ms. At rest at 580ms.**

## 4.2 The area sheet, and every other sheet

LIFT. The sheet is traveling, so **its contents fade only.**

| # | element | delay | alpha | other |
|---|---|---|---|---|
| 0 | card, on tap | 0 | | scale to its press value on `sConf`, see 5.2 |
| 0 | card wash, while the sheet is open | 0 | `eChg` | brightens to the sheet's card color and **holds**. This is the shared-element substitute, 4.15 |
| 0 | scrim | 0 | | `0.42 * sheetTravelFraction`. No spec, no duration |
| 0 | sheet container | 0 | | full height from below on `sSett` |
| 1 | area dot and label | **60** | `eChg` | none |
| 2 | `Active` sidehead and rule | 120 | `eChg` | the rule **draws left to right** on `sTrav` |
| 3 | item title | 180 | `eChg` | |
| 4 | Complete button | 240 | `eChg` | |
| 5 | `Queue` sidehead and rows | **300, capped** | `eChg` | |
| 6 | `Area` sidehead, Archive, Delete | 300 | `eChg` | Last in the reading order, which is where the two consequential actions belong |

**Legible at 446ms.** Dismiss: sheet down on `sRel` carrying the release velocity, scrim following by
position. Contents do not fade individually; they go with the container.

**Every other sheet in the app takes this grammar unchanged**: add item, edit area, the color picker,
swap chooser, queue chooser, the unfiled inbox, the area chooser for filing, the erase confirmation, and
Settings' reflection, session length, reminder hour, export, privacy and licenses sheets. **Thirteen
sheets, one rule, no call site with a number in it.**

## 4.3 Focus session, entered by tapping an item

WORLD, the dim door, plus the app's one shared element.

| # | element | delay | alpha | travel | other |
|---|---|---|---|---|---|
| 0 | the Daylight room | 0 | `eInst` | none | It stays exactly where it is and goes out, like a light |
| 0 | the indigo ground | 0 | `eChg` | none | scale 0.97 to 1.00 on `sSett` |
| 0 | **the item title** | 0 | **never fades at any point** | card position to session position | `sharedBounds`, `ScaleToBounds`, `boundsTransform = sTrav` |
| 0 | area dot and label | 0 | | card to session | `sharedElement`, `sTrav` |
| 1 | the 240dp ring | **120** | `eChg` | none | **draws from 12 o'clock clockwise on `sSett`**, 303ms, because a 240dp element on a 412dp screen is over a third of the viewport and 2.5 assigns it directly |
| 2 | the numeral | 120 | `eChg` | none | **never scales.** It is a number being read |
| 3 | `remaining` | 180 | `eChg` | none | |
| 4 | End session pill | 240 | `eChg` | none | |
| 5 | `Add 10 minutes` | 300 | `eChg` | none | Last. It is the tertiary control |

**Legible at 446ms.** The ring completes its draw at 423ms and `FOCUS_START` fires there, not at the tap:
a ring that appears full and starts shrinking says nothing, and a ring that draws says "this is your time,
all of it, and now it starts", which for a person with time blindness is the whole point of the surface.

Exit: the same movement reversed, and the title carries back to its card **only if it is still active
there**. If completing the item promoted the next one there is no match, the title does not fly, and the
promotion in 5.4 runs uninterrupted on the card. `SharedContentState` match availability gives this for
free, with no branch in the exit code.

## 4.4 Focus chooser, entered by the Focus chip

WORLD, the dim door, **no shared element**, because there is no source element on Areas: the chip is
chrome.

| # | element | delay | alpha | travel |
|---|---|---|---|---|
| 0 | Daylight out, indigo in | 0 | `eInst` / `eChg` | none, scale 0.97 to 1.00 on `sSett` |
| 1 | the question line | **120** | `eChg` | `near` 10 on `sTrav`. The room is stationary, so contents travel |
| 2 | option 1..n | 180, `rowStep`, cap 4 | `eChg` | `near` 10 |
| 3 | the duration control | 360 | `eChg` | none. Chrome |

**Chooser to running session is not a world change**; you are already in the world. It is DEEPER, and the
chosen option's item title carries as the shared element into the session on the same key scheme. That is
the second and last shared element in the app.

## 4.5 The Pulse

LIFT into a different world, and the one place a background animates on arrival.

| # | element | delay | alpha | other |
|---|---|---|---|---|
| 0 | scrim | 0 | | `0.42 * sheetTravelFraction` |
| 0 | sheet | 0 | | full height on `sSett` |
| 0 | **the ambient ground** | 0 | `eArr` | the time-of-day gradient resolves from flat `deepBlack` into its gradient. Legitimate because 3.3 makes this ground a function of time and arrival is when it is computed. **It is the only animated background in the app** |
| 1 | question line, serif | **60** | `eChg` | none, the container is moving |
| 2 | pill 1 | 120 | `eChg` | none |
| 3 | pill 2 | 180 | `eChg` | none |
| 4 | History row | 240 | `eChg` | none. Last: it is a way out, not the question |

**Legible at 386ms.**

**Ambient mode**, when a Pulse has already been answered: LATERAL onto the ambient content, and the 14-day
dot row sweeps at 15ms per dot for 210ms, per 3.4.

**Pulse to history page**: DEEPER, **inside a stationary sheet**, so contents travel `tight` 7dp rather
than `near` 10dp. Back: SHALLOWER, and it becomes a `PredictiveBackHandler`.

## 4.6 Momentum

The screen with the largest gap between what is specified and what is on the phone: a dot cascade, a
number roll, and nine other elements that simply appear.

| # | element | delay | alpha | travel | other |
|---|---|---|---|---|---|
| 1 | headline, `voice` serif 30 | **0** | `eChg` | `near` 10 | It is the sentence, and it is the entire point of the screen |
| 2 | 14-day dot row | **120** | `eChg` | none | **one element**, internal sweep 15ms per dot, 210ms total, per 3.4 |
| 3 | today's ring | 330 | `eChg` | none | **stroke sweeps 0 to 360 degrees on `sTrav`.** Draws when the row's sweep finishes |
| 4 | `Active 3 of last 14 days` | 180 | `eChg` | none | A readout of the row above |
| 5 | area color blocks | 240 | `eChg` | `near` 10 | |
| 6 | `This week` sidehead and rule | **300** | `eChg` | `near` 10 | **the hairline draws left to right on `sTrav`.** A sidehead opening a section, not a label appearing |
| 7 | the three numerals | 360 | `eChg` | none | **roll from 0 on `sSett`**, once, never again while on screen |
| 8 | the three stat labels | 360 | `eChg` | none | |
| 9 | `Area balance` sidehead and rule | 420 | `eChg` | `near` 10 | below the fold |
| 10 | balance rows | 480, `rowStep`, cap 4 | `eChg` | `near` 10 | |
| 11 | `Focus patterns` sidehead and rule | 660 | `eChg` | `near` 10 | |
| 12 | the seven squares | 720 | `eChg` | none | one element, sweep 30ms per square, 180ms |
| 13 | footer line | 780 | `eChg` | none | |

**Above the fold legible at 506ms**, which is the ceiling. Rows 9 to 13 are below the fold on a 412 by
914dp screen and arrive by 926ms, well before any scroll can reach them, and the first scroll collapses
whatever is left.

**The drawn rule under each sidehead is the single highest-value addition on this screen.** Three sections
currently announce themselves with a static label and a static line. A line that draws is a section
opening.

## 4.7 The Report

Already the best-choreographed surface in the app. What changes is that its five bespoke constants become
tokens and it gains 400ms of headroom under its own ceiling.

| # | element | starts | alpha | travel | other |
|---|---|---|---|---|---|
| 1 | eyebrow, `Clarity Report, week of August 21` | **0** | `eChg` | none | |
| 2 | headline, `voice` serif 30 | **0** | `eChg` | none | **scale 0.96 to 1.00 on `sSett`.** The one scale on a text element anywhere in the app, because the Report is the app's one ceremony |
| 3 | ribbon bars, days 1 to 7 | **120**, sweep 30ms per day | `eChg` | | **scaleY 0 to 1 with the origin at the baseline** on `sTrav`. Each bar grows from the floor, because a bar's height is the datum. Last bar starts at 300 |
| 4 | day initials | with their bar | `eChg` | none | `micro` 10sp, up from 8 |
| 5 | ribbon caption | 330 | `eChg` | none | after the last bar starts |
| 6 | hairline rule | 360 | | | **draws left to right** on `sTrav` |
| 7 | first-week or decline line | 420 | `eChg` | `near` 10 | |
| 8 | section 1 sidehead and rule | 480 | `eChg` | `near` 10 | rule draws on `sTrav` |
| 9 | section bodies | 540, `rowStep`, cap 4 | `eChg` | `near` 10 | The cap is what holds the ceiling on a long report |
| 10 | footer | 840 | `eChg` | none | |

**Legible at 926ms, at rest at 1000ms.** Under 8.2 item 12's 1.4 second ceiling with **400ms of headroom**,
which is the margin that lets a long report grow without a rewrite. Re-arms on content change, 8.4's one
exception, unchanged.

## 4.8 The Trail

The one screen whose reading order genuinely *is* top to bottom, because it is a chronology.

| # | element | delay | alpha | travel |
|---|---|---|---|---|
| 1 | `Trail`, `title` serif 24 | **0** | `eChg` | `near` 10 |
| 2 | filter chips | 120 | `eChg` | none. Chrome |
| 3 | `Today` header, count and rule | 120 | `eChg` | `near` 10, rule draws on `sTrav` |
| 4 | rows in that day | 180, `rowStep`, cap 4 | `eChg` | `near` 10 |
| 5 | the next day header and its rows | continue the ladder | `eChg` | `near` 10 |

**Legible to the fold at 506ms.**

**Paging: no entrance.** New rows appended on scroll appear at rest, because the first-scroll rule has
already fired. What animates is the placeholder to real-row swap, which is a Replace.

**Sticky day headers.** `stickyHeader` in the existing `LazyColumn`. At 200 percent text one day can fill
three screens, and a chronology whose date has scrolled away is a list of times with no day attached. It
moves only as much as the list moves, never independently, and it answers *which day am I in*, which is
information rather than decoration. This is layout, not motion.

## 4.9 Settings

DEEPER. **The container traveled, so the contents fade only.**

| # | element | delay | alpha | travel |
|---|---|---|---|---|
| 1 | back chevron | **0** | `eChg` | none |
| 2 | `Settings`, `title` serif 24 | 0 | `eChg` | none |
| 3 | `Daily routine` sidehead and rule | **120** | `eChg` | none, rule draws on `sTrav` |
| 4 | its rows | 180, `rowStep`, cap 4 | `eChg` | none |
| 5 | remaining sections | **360, capped** | `eChg` | none |

**Legible at 506ms, and at rest at 506ms**, because nothing travels. A pushed screen therefore settles
faster than a tab, which is correct: you asked for it by name.

Back: SHALLOWER, via `PredictiveBackHandler`.

## 4.10 About

Identical grammar. The support block is last **for the same reason the FAB is last on Areas: it is an
offer, not information, and an offer never precedes the thing it is attached to.** In an app whose section
14 forbids upgrade prompts and premium badges, where the one money-adjacent element sits in the arrival
order is a statement, and it should be the quietest position on the screen.

| # | element | delay | alpha | travel |
|---|---|---|---|---|
| 1 | back chevron, `About` title | 0 | `eChg` | none |
| 2 | body paragraphs | 120, `rowStep`, cap 4 | `eChg` | none |
| 3 | the support block | 360 | `eChg` | none |

## 4.11 The archive

DEEPER, contents fade only. Chevron and title at 0, archived rows at 120 on `rowStep` capped at 4.
**Legible at 506ms.**

Empty state: the 150ms guard delay in 8.2 item 25 is **kept and is not a motion number.** It is a load
guard that stops a flash during a projection that resolves quickly, and 8.4 exempts it by name. The fade
after it is a Reveal on `effectsChange` like every other.

## 4.12 Onboarding

Four beats, currently hard-cutting into each other with a bare `when (state.beat)`. The most visible miss
in the app, in its most ceremonial moment.

**Between beats: DEEPER and SHALLOWER inside one dark room**, per 3.6, with the scale range narrowed to
0.97 / 1.03 because a beat is a panel rather than a screen. The swipe tracks the finger 1:1 on the same
axis via `DeferredAnimatedContent`, handing off with velocity at commit.

**Beat 1, See it work.** headline 0, body line 120, the demonstration 180. All `eChg` with a `near` 10dp
rise.

**Beat 2, Your areas.** headline 0, body 120, the two choices 180 and 240, suggestion chips 300 (one
element, sweep 30ms per chip, chrome so no travel), `Continue` 360. Nothing waits on the keyboard; the IME
is a platform surface and the beat is composed regardless.

**Beat 3, The reveal.** The app's one ceremony and its one iris.

| # | moment | at | spec |
|---|---|---|---|
| 1 | closing line fades in on black | 0 | `eArr`, 206ms |
| 2 | it holds | 206 | `hold` **900ms**. The only call site of the only duration token in the app |
| 3 | the iris opens | 1106 | radius 0 to `hypot(w, h) / 2` on `sSett`, `BlendMode.DstOut`, as built |
| 4 | the closing line fades out | 1106 | `eInst`, riding the iris |

**And the Areas screen behind the iris renders settled.** Its entrance flag is marked spent by the reveal,
per 3.5 item 4. This is the one place in the app where an entrance must be actively suppressed, and it is
currently unhandled.

**Beat 4, The depth.** Standard grammar on arrival; its internal moments are already built.

## 4.13 The tutorial

| # | element | delay | spec |
|---|---|---|---|
| **step 1** | scrim | 0 | `eArr` to its opacity |
| | cutout | 0 | radius 0 to target on `sSett`, from the target's own center |
| | the card | 120 | `eChg` plus `near` 10dp on `sTrav` |
| **steps 2 to 5** | cutout | 0 | bounds travel to the new target on `sTrav`. **One continuous move, never a fade-out then a fade-in** |
| | card, same side of screen | 0 | text crossfades in place. A Replace |
| | card, different side | 0 | `eChg` plus **`near` 10dp in the direction the cutout moved** |

**The card follows the spotlight.** If the spotlight goes down, the card goes down. That single rule is
what makes the tutorial feel like one object being pointed with, rather than five cards taking turns.

**The ring pulse becomes a Notice and fires once**, per 2.1: brighten to 0.45 on `effectsChange` when the
step arrives, and hold. No loop. `CALM_TUTORIAL_PULSE_OPACITY` is deleted along with the loop it was
compensating for.

Back skips the whole tutorial, unchanged, and becomes a `PredictiveBackHandler` so the preview shows the
app underneath returning, **which is exactly what "back skips the tutorial" means and currently has to be
learned by trying it.**

## 4.14 The re-entry screen

The highest-stakes screen in the app, and `design-v3.md` 11.2 refuses it ceremony: "Item 25's entrance, and
nothing else. No iris, no bloom, no transition into a different world."

**That refusal stands completely.** What changes is that "nothing else" stops meaning one 400ms block fade
and starts meaning the same entrance every other Daylight screen has, **because the grammar is nothing
else.** 11.2's point is that this screen must not be ceremonial, not that it must be inert.

| # | element | delay | alpha | travel |
|---|---|---|---|---|
| | the 150ms load guard | | | untouched, and not a motion number |
| 1 | the `read` serif line | guard + **0** | `eChg` | `near` 10 |
| 2 | the body line | guard + 120 | `eChg` | `near` 10 |
| 3 | `Keep everything as it is` | guard + 180 | `eChg` | none. Chrome |
| 4 | `Put active items back in their queues` | guard + 240 | `eChg` | none |

**Legible at 536ms including the guard.** Left aligned at the top where the Areas title sits, so the screen
after this one looks like the same screen with the sentences taken out, and now it *arrives* the same way
too. No back handler, so the system's own back-to-home preview is what a person gets, which is correct.

## 4.15 Shared elements: two places, not three

**1. Area card to focus session, and chooser option to focus session.** The item title exists on both
sides, and the area dot with it.

- `Modifier.sharedBounds` on the title with `ResizeMode.ScaleToBounds()`. The docs recommend this for
  `Text` specifically, because it scales graphically instead of remeasuring and therefore **does not
  reflow the title onto different lines mid-flight**, and because a remeasure per frame is the one thing
  Part 7's frame budget cannot absorb.
- `Modifier.sharedElement` on the 7dp area dot.
- Keys are **data classes**, never strings: `data class FocusSharedKey(val itemId: String, val part: Part)`.
- `boundsTransform = spatialTravel`.
- **The title never fades at any point.** It is the one thing continuously legible across the entire world
  change, which is the whole reason to do this: the room goes dark around a sentence that never moves out
  from under your eye.
- `permitTransformDuringDeferredTransition = false`, so the title does not fly around under a dragging
  finger during predictive back.

**One layout note that belongs to the container and grid work, not to motion.** On Areas the item title
sits at roughly 46 percent of screen height; on the Focus screen it sits at roughly 30 percent. **If those
two agreed, the Relate would be free in the full-motion form and exact in the reduced form**, because the
shared element would not have to move at all. Motion should push that into the grid rather than animate
around it.

**2. Nothing else.**

**The area card to area sheet is technically blocked and I am not going to pretend otherwise.** Compose
shared elements do not work with `ModalBottomSheet` or `Dialog`; they render in their own window. The
alternative is replacing `ModalBottomSheet` with an in-composition sheet inside the
`SharedTransitionLayout`, which means hand-building drag-to-dismiss with velocity, nested-scroll handoff,
IME handling for the sheet's text fields, window insets, focus management and TalkBack pane semantics.
`design-v3.md` 17.1 to 17.3 refuses exactly that trade.

**The substitute costs nothing and is arguably better.** While the sheet is open, **the card underneath
lights**: its wash brightens to the sheet's card color on `effectsChange` and holds. Alpha only, no
geometry. The thing you tapped stays visibly the thing that is open, through a 42 percent scrim. It
survives a scroll, it survives a rotation, it costs one color animation, and it works under reduce motion
unchanged.

**A third candidate, refused by name:** the Report's week ribbon into the Report history page. Tempting and
wrong. A 7-bar chart morphing into a list row is a transform whose start and end are not the same object,
which is motion for its own sake and is the thing the owner explicitly ruled out.

**The real cost, stated so the issue can be written honestly.** `ClarityShell` becomes a
`SharedTransitionLayout` and the Focus branch becomes an `AnimatedContent` target so there is an
`animatedVisibilityScope` to pass down. That changes `FocusSurfaceStore` disposal timing: **the store must
not clear until the exit transition completes**, or the title's match disappears mid-flight.
`DisposableEffect(focusStore)` currently clears on `onDispose`, which will be too early. **This is the one
architectural risk in this document and it belongs in the issue's acceptance criteria.**

## 4.16 Predictive back

Replace all eight `BackHandler` call sites with `PredictiveBackHandler`, driving SHALLOWER from `progress`
instead of from a clock. The eight are `ReportRoute.kt:72`, `OnboardingRoute.kt:170`, `FocusRoute.kt:98`,
`ClarityShell.kt:265`, `ArchiveScreen.kt:80`, `PulseRoute.kt:95`, `SettingsSurface.kt:103`,
`TutorialHost.kt:86`. No manifest change is needed at `targetSdk 37`.

**Use the platform's own published numbers so the app and the system agree.**

| property | value | on a Pixel 8, 412 by 914dp |
|---|---|---|
| current surface scale | `1 - 0.10 * progress` | 100 to 90 percent |
| x translation | `((width / 20) - 8).dp * progress`, signed by `swipeEdge` | **12.6dp** |
| y translation | `((height / 20) - 8).dp`, tracking `touchY` | **37.7dp** max |
| corner radius | rises to **`section` 28dp**, the sheet radius, so the app has one detached-surface radius | |
| destination behind | scale 0.96 to 1.00 tracking `1 - progress`, alpha = `progress` | |
| progress easing | `PathInterpolator(0f, 0f, 0f, 1f)`, the platform's `STANDARD_DECELERATE` | |
| commit | hand off to `spatialRelease` with the gesture velocity | |
| cancel | `spatialTravel` back to identity | |

**Where it applies:** every pushed screen, the Pulse history page, a non-root tab returning to Areas, and
the important one, **leaving a focus session.** 10.15 says back leaves the session running and must not
prompt or warn. A back preview showing Areas returning behind, with the session's own card still counting
down on it, is the most reassuring possible answer to "am I about to destroy my session", **and it says it
without a single word of copy.**

**Where it does not apply:** the re-entry screen has no back at all, so the system's own back-to-home
preview is correct and nothing should intercept it. Modal sheets already get predictive back from Material.

## 4.17 Scroll

**Nothing in this app animates on scroll.** The complete list:

1. **The first scroll cancels the entrance.** The only scroll-linked animation in the app, and it *stops*
   motion rather than starting it.
2. **The scroll edge fade** stays exactly as `ScrollEdge.kt` built it. It is a real answer to a real
   layering problem and it moves nothing.
3. **Keep Android's stretch overscroll, everywhere, unchanged, and add nothing.** This is the obvious
   answer, taken with a recorded reason as `design-v3.md` 15 permits: overscroll is the one motion in this
   app that answers a question the person asked with their finger, "is there more", and the platform's
   answer is the one their hand is already calibrated to across every other app on the device. Inventing a
   second answer would make Clarity the only app on the phone whose lists lie about their ends, and
   predictable behavior is worth more to this audience than a signature.
   **There is no pull gesture in this app and there must never be one.** There is nothing to fetch, and a
   pull-to-refresh would be a promise of remote data in an app that ships without the `INTERNET` permission.
4. **Parallax: refused by name.** Top of the vestibular trigger list, and the audience is the reason.
5. **A collapsing header: refused.** It is the statistically common answer. It is two type sizes for one
   string, it moves while you are reading it, `design-v3.md` has no toolbar for a title to shrink into, and
   it would put motion permanently under a moving finger on the four screens a person lives on.
6. **A FAB that hides on scroll: refused, and this one is close.** The FAB is the only element that
   occludes content, and a circle scaling from its own center is not a plane shift. It fails on 8.4's own
   reasoning: it makes one screen behave two ways for a reason the user cannot see. An element that vanishes
   at a threshold nobody can perceive is exactly the unpredictability this app removes everywhere else.


---

# PART 5. MOTION AT THE FINGERTIP

## 5.1 The press path, which is most of what "cheap" means

`Modifier.clickable` delays its press interaction by `TapIndicationDelay`, which is the platform's
`ViewConfiguration.getTapTimeout()`, **100ms**, whenever `delayPressInteraction()` is true, and it is true
for a row with a waiting scrollable parent. `clarityPressScale` reads `collectIsPressedAsState()`, so it
inherits that delay, and it then runs `springStandard`, which is 98 percent complete at **193ms**.

**100 + 193 is roughly 290ms from touch to visible acknowledgment**, on the area card, every settings row,
every archive row, every Trail row. The band the literature supports for press feedback is 100 to 160ms.
**This single number is most of what "cheap" means here.**

The fix is three rules and one formula.

**Rule 1. Press in on `spatialConfirm` from touch-down with no delay. Release on `spatialConfirm`. Cancel
on `effectsChange`, flat and quiet.** Three specs, and no control names its own. **131ms from finger to
acknowledgment, a 159ms improvement, inside the premium band.**

This requires the app's own `pointerInput` rather than `clickable`, which it needs anyway for rule 4.

**Rule 2. A press moves the control's nearest edge 1.5dp, and the scale is computed from the measured
layout.**

```kotlin
pressScale = (1f - Sizes.pressTravel / min(width, height)).coerceIn(0.93f, 0.98f)
```

`Sizes.pressTravel` is **3dp**, and its citation in `ClaritySizes.kt` is that it is the one value which
makes `design-v3.md` 8.2 item 2's 0.97 and item 16's 0.94 **consequences of a single formula rather than
two separate choices**:

| control | smallest dimension | formula gives | design-v3.md says |
|---|---|---|---|
| area card | 104dp | 0.971 | 0.97 (item 2) |
| FAB | 48dp | 0.9375 | 0.94 (item 16) |
| primary button | 50dp | 0.940 | |
| settings or switch row | 64dp | 0.953 | |
| chip | 38dp | 0.921, clamped to **0.930** | |

1.5dp per side is inside Confirm's `pair` 5dp cap, so **the hit rect never moves meaningfully under a
finger that is already on it.** No call site passes a scale.

**Rule 3. A control with a drawn container presses by scale. A control without one presses by ink**, to 62
percent on `effectsInstant` and back on `effectsChange`. Tertiary buttons, icon buttons, the sheet handle,
the archive and settings glyphs. Text has no body to compress, and scaled text is a legibility problem at
200 percent.

**Rule 4. The visual is optimistic. The haptic is not.** The press scale starts on touch-down, because if
the gesture turns into a scroll the control simply returns, which is itself correct feedback and is what
Compose already does with ripples. **The haptic waits for the release**, because a haptic cannot be taken
back and `design-v3.md` section 9 forbids a haptic on scroll. This is the resolution of the 290ms defect,
and it changes nothing about section 9.

**Rule 5. The haptic marks the moment the information exists.** Where a control's meaning is its touch, the
tick fires on release, on the frame the release spring starts. Where a control's meaning is the state it
produces, the haptic moves to the state's arrival: `TOGGLE_ON` when the thumb lands, `PROMOTE` when the new
title reaches full opacity, `PUT_DOWN` when the card lands, `COMPLETE` when the card is home. The app
already does this once, in section 9's "as the new title lands". **Rule 5 is that sentence made general.**

## 5.2 Every control

`pressScale` is rule 2 throughout. The dp in brackets is the control's smallest dimension as built.

| control | press in | press out | additional | haptic, and when |
|---|---|---|---|---|
| Area card [104dp] | 0.971 plus shadow `card` to zero, `sConf` | `sConf` | dark world: scale only, no elevation change, because stepping a dark card toward canvas reads as disabled | `TAP` on release |
| Primary button [50dp] | 0.940 plus fill darkens 6 percent, `eInst` | `sConf` | | `TAP` on release |
| Positive, Secondary [50dp] | 0.940 plus tint 13 to 18 percent, `eInst` | `sConf` | | `TAP` on release |
| Tertiary, text only | ink to 62 percent, `eInst` | `eChg` | no scale, per rule 3 | `TAP` on release |
| Destructive, inert | nothing moves | | **the enabling target brightens one ink step on `eArr` and returns.** Points at what needs doing instead of refusing | `REJECT` on release |
| Destructive, armed | 0.940 | `sConf` | fill arrives on `eArr` when the condition is met, silently, so a keystroke never buzzes | `WARN` on press-down, because the arming is the information |
| FAB [48dp] | 0.9375 | `sConf` | | `TAP` on release |
| Chip [38dp] | 0.930, clamped | `sConf` | selection fill and label on `eChg`, never overshooting | `TAP` on release |
| Segmented segment [48dp] | 0.9375 | `sConf` | **incoming ink on `eChg`, outgoing on `eArr`**, so at least one segment is filled at every instant and the new one is visibly brighter first | `SELECT` on release |
| Switch row [64dp] | row 0.953, `sConf` | `sConf` | thumb travel `sConf`, track color `eChg`. Two things move for one tap and that is correct: the row press is the touch, the thumb is the result | `TOGGLE_ON` / `TOGGLE_OFF` at the thumb's arrival, 131ms after release |
| Settings row [64dp] | 0.953 | `sConf` | chevron does not move | `TAP` on release |
| Sheet handle [4dp] | ink 18 to 30 percent, `eInst` | `eChg` | **fires whenever the sheet is being dragged**, including a drag begun on the sheet body, so it names the thing you are holding rather than the thing you touched | none. The sheet's own |
| Pulse response pill [50dp] | 0.940 | `sConf` | on release the press return and the amber radiate run together: the return is your finger leaving, the fill is the answer landing | `SELECT` on release |
| Plan accept pill [44dp] | 0.932 | `sConf` | see 5.4e | `PLAN_ACCEPTED` on release, tap weight, no celebration |
| Tab item [48dp] | 0.9375 | `sConf` | pill travel `sTrav` (an Orient with an inverse), icon outline to filled on `eChg` | `TAP` on release |
| Swipe face button [66dp] | 0.955 | `sConf` | | none on press. The action's own |

### Hold

Long press is 500ms and today nothing happens for the whole of it, then a thud. **From 100ms after
touch-down, the card travels from its press scale to 1.02 and its shadow grows from `card` to `tabBar`,
keyed linearly to hold progress so it arrives exactly at the 500ms threshold together with the `PICK_UP`
thud.** That is information: it says keep holding and something will happen, and it says it to the audience
least served by an invisible timer.

The obvious answer is a circular progress ring filling under the finger, which Material used for years.
**Refused: it is a progress ring, which `design-v3.md` 14 forbids by name, and it makes the wait feel like a
task.** The lift instead says what the wait is *for*, which is that the card is about to become liftable.

Hold progress is a Depict, not an animation. It has no spring, it is keyed to elapsed time, and it is the
one place a Confirm is allowed to exceed its 5dp cap, because the element has stopped being tappable and
has started being draggable.

### Cancel, in four kinds

All four return on `effectsChange`, 146ms, flat, no haptic, no action, because a canceled gesture should
read as nothing having happened.

- **Claimed by a scrolling parent.** Return, and the scroll proceeds. Compose already removes indication
  here and the scale must follow the same event.
- **Finger leaves the control's bounds.** Return, **but the gesture stays alive**, and coming back within
  bounds plus 12dp re-presses on `spatialConfirm`. Sliding off a control and back on is a thing people do,
  and today it is not modeled. `clickable` does not track re-entry, which is the second reason for the own
  `pointerInput`.
- **Pointer canceled by the system.** Return, silent.
- **A second pointer joins.** The press holds on the first pointer, per Compose's own filtering.

## 5.3 Every gesture

### 5.3a The swipe row

**`Delete is never committed by a swipe.` Unchanged, load bearing, and this section strengthens it.**

Replace the hand-rolled `draggable` plus manual clamp with `AnchoredDraggableState` carrying
`positionalThreshold`, `velocityThreshold`, `snapAnimationSpec` and `decayAnimationSpec`. That is where
velocity handoff and decay live for free, and it is the platform's own answer to the fact that
`onDragStopped` currently receives a velocity and reduces it to one boolean, so **a hard flick and a slow
push produce the identical animation.**

| zone | travel | feel |
|---|---|---|
| 0 to 25 percent, reveal | 1:1 | face fades in with the movement, glyph scales 0.8 to 1.0 |
| 25 to 55 percent, decide | 1:1 | face fully legible, sitting still under the moving card |
| **at 55 percent, arm** | 1:1 | **the committing face takes the whole reveal.** Below |
| past 55 percent | rubber band, `overVisual = chapter * over / (over + chapter)` | asymptote `chapter` 40dp. The card stops. **You can feel that it is loaded and that there is nothing beyond** |

The rubber band replaces the current clamp at `rowWidth`, which lets a card be dragged fully off screen
before release and leaves the commit animation nowhere to go.

**The arm, which is the best thing in this section.** Past the commit point, on `spatialTravel`: the **Swap**
face's width animates from 66dp to the full revealed width, the **Delete** face's width to zero, and
Delete's glyph and label fade on `effectsInstant`. The Swap glyph translates to the new center on the same
spring. Below the threshold, the exact reverse, same spec.

This answers an ambiguity the design's own rule creates. Two faces are revealed and only one commits, and
until now the interface never said which. Now, at the moment it matters, **Delete is physically removed from
under your thumb. The rule that destructive actions are not committed by momentum stops being a promise in a
document and becomes something the hand can feel.** On the right swipe there is one face, so Complete grows
from 66dp to fill the reveal by the same rule.

**Release. Every branch carries the release velocity, and every branch is `spatialRelease`.**

| release | target |
|---|---|
| below 25 percent | 0 |
| 25 to 55 percent, no fling | the detent, 66dp or 132dp |
| past 55 percent, or a fling above 1200 dp/s past 25 percent | commit |
| a row closed because another opened | 0, **with no velocity**: it is not responding to that finger and should be quieter |

**Haptics.** `SWIPE_THRESHOLD` at the crossing, once per gesture, never on the way back. Unchanged.

### 5.3b Drag to reorder

- **Pick up.** Scale to 1.02 and, in the light world, shadow `card` to `tabBar`, both on `spatialSettle`,
  arriving with the `PICK_UP` thud. **In the dark world there is no shadow and `raise` is darker than
  `card`, so the lift is expressed the other way: the other rows dim to 88 percent on `effectsArrive` and
  return on drop.** Everything else steps back. One device, transient, no new token.
- **Follow.** 1:1, no lag, no lead, no rotation. A tilt is a tell.
- **Make room.** `Modifier.animateItem(placementSpec = spatialTravel)`, explicit rather than the library
  default, so the rows moving aside have weight and never bounce past.
- **Drop.** Offset to zero, scale back to 1.0 and elevation back, all on one `spatialRelease` carrying the
  drag velocity, so the card lands as a single object. **`PUT_DOWN` fires when the scale reaches 1.0, not at
  release. You feel it land, not you feel yourself let go.**
- **Cancel.** The card returns to its **original** slot on `spatialSettle`, the preview order unwinds on
  `spatialTravel`, and **no `PUT_DOWN` fires**, because nothing was put down. Today `state.cancel()` snaps.

*Functional gap, flagged rather than specified:* there is no edge auto-scroll, so a card cannot be reordered
past a screenful. The motion for it, if it is ever built, is a rate ramping 0 to 900 dp/s across a 72dp band
at each end.

### 5.3c The bottom sheet

| beat | spec |
|---|---|
| entrance | sheet travel from the bottom edge on `spatialSettle`. Scrim by the position rule, 3.6 |
| drag | 1:1 down. **No rubber band upward, hard stop.** `skipPartiallyExpanded` is true, so there is no larger state and a rubber band would promise one |
| release above 40 percent, no fling | back up on `spatialRelease` with the velocity |
| release past 40 percent, or a downward fling | down on `spatialRelease` with the velocity, scrim following by position |
| predictive back | translation tracks `backEvent.progress`, scrim follows by position |
| handle | ink 18 to 30 percent on `effectsInstant` while any drag is in flight |

## 5.4 Every state change

The owner's test, applied to every one: **what does the motion tell the person that the absence of motion
would not?** Where the answer was nothing, it is cut, and it says so.

### 5.4a An item is completed. The hero.

**What it tells you:** which item you just finished, that it is now struck off rather than merely gone, that
the area is still here, and what is next. Four facts. Without motion the card simply shows a different
sentence, and change blindness is real: a screen change that seems striking to a designer is routinely
missed by the person looking at it.

**The amendment, and it is the one substantive disagreement with `design-v3.md`.** 10.3.1 says "the card
sliding fully off in 180ms". **It should not.** The card is the *area*; the item is the title line. Sliding
the card off says the area left, which is false, and it is the identical 180ms slide used for delete, where
a row genuinely does leave. **Using one movement for "this is gone" and "this is done" is the app telling a
person the same thing about two different events.** Under either resolution the current implementation is
also defective: `commit()` slides the card off, runs the action, then `offset.snapTo(0f)`, so the card
reappears at rest on one frame. That is a teleport.

The card is the slot. Replace's first rule is that the slot does not move. So the card comes home.

| t | what | spec | property |
|---|---|---|---|
| 0 | card returns home from wherever the finger left it | `spatialRelease` with the release velocity | `translationX` |
| 0 | `COMPLETE` haptic: tick 0.5, then click 0.8 at 60ms | | |
| 0 to 206 | area wash brightens to 11 percent | `effectsArrive` | wash alpha |
| **0 to 131** | **the strike-through draws left to right across the completed title** | `spatialConfirm`, a drawn stroke caused by a touch | line width 0 to 100 percent |
| 131 to 226 | completed title fades out and drops `tight` 7dp | `effectsInstant` alpha, `spatialTravel` travel | Replace, outgoing |
| 131 to 277 | next title fades in and rises `near` 10dp | `effectsChange` alpha, `spatialTravel` travel | Replace, incoming |
| **277** | **the new title is fully legible.** `PROMOTE` haptic fires here | | rule 5 |
| 206 to 412 | area wash returns | `effectsArrive` | wash alpha |

**Total 412ms, and the answer is readable at 277ms.** Compare with today: `promotionMillis` 250 plus a 180ms
slide plus a teleport, which is about 430ms for less information. **The hero gets more legible and does not
get longer.** It is under the 500ms ceiling, and nothing is blocked at any point.

**Why the strike draws.** A strike that appears fully formed is a state. A strike that draws is the app
performing the act of striking it off, in 131ms, and it is the single clearest statement of what just
happened. The Report ribbon is the precedent for a drawn line being the most satisfying thing in this design.

**Why the two halves overlap and are never both at full opacity.** That is Replace's rule, and 2.4's
asymmetric pair delivers it: combined opacity dips to 0.769 rather than sitting at 1.000, so the eye tracks
one thing changing rather than reading two sentences stacked.

**What the queue does behind it: nothing, and that is the answer.** `queueLength` exists on `AreaCardModel`
and appears only in the TalkBack description. The card draws no count and `design-v3.md` 14 forbids a numeric
badge, so there is nothing on screen for the queue to do, and inventing a visible queue in order to animate it
would be decorating data. Applying the owner's test to my own temptation: it tells the person nothing they did
not have, so it is cut.

**One exception, and it is the best use of restraint here.** When the completed item was the **last** one, the
incoming line is the idle invitation, it arrives on **`spatialSettle` (303ms rather than 146ms)**, and
**`PROMOTE` does not fire.** Nothing was promoted. The slower arrival and the missing haptic are the app
saying "that was the last one" without a word, a badge, a count or a celebration. **That is what the queue
does behind it, rendered as the only honest fact available.**

### 5.4b An item is promoted without a completion, by Swap or from the inbox

Identical to the incoming half of 5.4a. **No strike-through and no `COMPLETE` haptic.**

**What it tells you:** which of the two things happened. The strike is now the only difference between a
completion and a swap, in both the visual and the haptic. **The motion carries the distinction, so the copy
does not have to.**

### 5.4c A focus session starts, extends, ends

**Start.** The world dim of 3.6, plus the ring drawing per 4.3, plus `FOCUS_START` at the end of the draw.
This fires on every session start, not once per app session: 8.4 governs tab entrances, and a session
beginning is a state change that is genuinely new each time.

**Extend by ten minutes.** The arc grows to its new length and the numeral rolls, both on `spatialSettle`.
One addition that is a correctness catch rather than a flourish: **when the transition warning is on, the
five minute tick must travel to its new position on the same spring**, or the mark is lying about when it
will fire.

**End, naturally.** The ring collapses inward as a Depict reaching zero. Then the check appears **in the
ring's slot** as a Replace, `effectsInstant` out and `effectsChange` in. `FOCUS_END`, quick rise then thud at
120ms. **The expanding circle is deleted**, per 2.1, and with it the one place the app celebrates.

**End, early.** `DECISIONS.md` carries this as an open question and this settles it: the ring collapses to
center on `spatialTravel` and the world transitions back. **No check, no bloom, and no haptic beyond the
ordinary tap on the End control.** The person gets the same closure; the app neither congratulates a session
they cut short nor scolds it, **and the difference is an absence rather than a red state or a sentence.**

### 5.4d The Pulse is answered

Already correct and unchanged in structure: the amber grows from the recorded touch point, unselected pills
fade to 30 percent and drop `pair` 5dp on `effectsChange`, acknowledgment follows. The radiate takes
`spatialTravel`, because it is a real distance from a real point. The documented center fallback for keyboard
and TalkBack activation stays.

**What it tells you:** that the answer came from your hand rather than from the app, which is the difference
between a mood check and a form. **This is the one moment in the app that is already right, and everything in
5.1 is that pill's rules generalized.**

### 5.4e A plan is accepted

The pill fills from center on `spatialTravel`, the label crossfades to the confirmation as a Replace, and it
settles at reduced prominence. Never bounces, never celebrates.

One sequencing addition, which is what makes it read as logical rather than as three things happening: **the
offer block below collapses its height on `spatialSettle` first, and the pill's settle to reduced prominence
starts only when the collapse finishes.** The eye follows one thing at a time. Today the pill's crossfade and
the block's `AnimatedVisibility` run together and compete.

`PLAN_ACCEPTED` stays a tap-weight tick, because accepting is not an achievement. **Layer six never gets a
sentence out of this, and no motion here may imply one.**

### 5.4f An area is archived and restored

**Archived.** Not a swipe, so no slide. The card **fades to 0.4 on `effectsChange`, then collapses its height
to zero on `spatialSettle`**, and the cards below close up on `spatialTravel`. The fade before the collapse is
load bearing: there is deliberately no undo snackbar on archiving, so the person must be able to see *which*
card left, and a card that collapses without first identifying itself is a row vanishing.

**Restored.** The reverse, and it arrives **at its ordered position, not appended**: height 0 to full on
`spatialSettle`, content fading in on `effectsChange` over the last part of that travel. **No entrance
treatment**, no fade-and-rise, because the card is returning rather than arriving.

*Functional note, flagged:* after a restore the list should scroll so the restored card is on screen. That
changes what the app does and is out of scope.

### 5.4g Something is undone from the snackbar

| t | what | spec |
|---|---|---|
| 0 | `UNDO` tick, and **the depleting line stops immediately** rather than running on | |
| 0 | snackbar falls away | `spatialTravel` |
| 0 | the row expands its height back, from the same instant | `spatialSettle` |
| 157 | the row's content fades in over the last part of the expansion | `effectsChange` |

**The restored row gets no entrance.** It expands from the height it lost. An entrance would say "this is
new", and the whole meaning of an undo is that it is not.

The depleting line stays linear, and that is right rather than lazy: **it is a Depict of time, and time is
linear.** An eased countdown would misrepresent how much of the five seconds is left, to the audience least
able to afford that.


---

# PART 6. THE REDUCED FORM, AS A DESIGN

## 6.1 The principle, in one sentence

> **Reduce motion is a constraint on displacement, not on duration. The reduced app takes the same time and
> covers no ground.**

The current implementation collapses every animation into one `tween(150)`. That reduces both axes, and only
one of them was ever the problem. Check the sources. WCAG 2.3.3 is about *motion animation* triggered by
interaction. The vestibular literature is about *movement across the visual field*: large areas, parallax,
rotation, scaling. Apple's Reduce Motion replaces *slides and zooms*. **Not one of them is about how long an
opacity change takes.**

Apple's own product treats this as two switches, not one: Reduce Motion is the first, and "Prefer Cross-Fade
Transitions" is a *separate* setting underneath it. **Clarity currently applies Apple's second switch
unconditionally, to everything, which is why the reduced app reads as a fallback.**

There is also a measurable defect in the current path. `effectsFast` is 98 percent complete in **95ms**. Under
reduce motion it becomes `tween(150)`. **That is a 58 percent slowdown on the app's most immediate feedback,
applied in the name of an accessibility setting that says nothing about opacity.** A reduce-motion user has no
reason to want a chip fill slowed down.

## 6.2 The reduced token table, which is deliberately almost empty

| token | full | reduced |
|---|---|---|
| `spatialConfirm` | 0.82 / 900 | **unchanged.** Travel to 0. Press scale kept in full |
| `spatialTravel` | 0.86 / 380 | **unchanged.** No translation. Bounds change in place |
| `spatialSettle` | 0.90 / 240 | **unchanged.** No translation. Scale capped at 3 percent on anything over a third of the viewport |
| `spatialRelease` | 1.00 / 900 | **unchanged.** A Track is direct manipulation, which the literature explicitly permits |
| `effectsInstant` | 1.00 / 3800 | **unchanged** |
| `effectsChange` | 1.00 / 1600 | **unchanged** |
| `effectsArrive` | 1.00 / 800 | **unchanged** |
| `markStep`, `rowStep`, `bandStep` | 30, 60, 120 | **unchanged** |
| `hold` | 900 | **unchanged** |
| Depict | a ticker | **unchanged. Never suppressed.** |
| every travel | `pair`, `tight`, `near` | **0dp** |
| any scale on an element over a third of the viewport | as specified | **capped at 3 percent** |

**Not one spring changes.** With travel at zero there is nothing left to overshoot on a translation, and the
largest residual anywhere is `spatialConfirm`'s 1.11 percent on a press scale, which is 0.0003 of a 0.97 and is
not a thing a human eye can resolve. **The press scale is kept in full**, because the vestibular concern is
area and a 48dp control scaling by 6 percent moves 3dp, which is smaller than the reduced form's own hit-rect
tolerance. 2.5's area rule already draws this line and this inherits it rather than drawing a second one.

**Consequence: `ReducedMotion` is deleted.** `ClarityMotion` stops being two implementations that have to be
kept in agreement and becomes one spring set plus one boolean that call sites read for travel. Six copies of
`REDUCED_MILLIS = 150` disappear, and `crossfade()` disappears, and there is no longer any way for the two
paths to disagree about what arrives when, **because there is only one path.**

## 6.3 The three things that make it good rather than safe

**1. Reading order survives, and this is the whole argument.**

Setting `staggerMillis = 0`, which is what the app does today, does not remove an effect. It removes
*information*: the stagger is the app saying "read this first, then this". A screen where everything appears at
once has thrown that away, and it has thrown it away for the reader who benefits from it most.

**The vestibular cost of a staggered fade with no travel is zero, because nothing moves. The attention cost is
lower than a simultaneous appearance**, because a sequence directs the eye and a simultaneous flash of the whole
screen does not. So the stagger is not reduced, not halved, not shortened. It is untouched.

**2. The anchor survives.**

Every Orient and Relate has an origin, and in the reduced form that origin is expressed as *where the thing
appears* rather than *where it traveled from*. A sheet arrives anchored to the bottom edge: scale 0.98 to 1.00
with its transform origin at the bottom, on `effectsChange` and `spatialSettle`. Not a fade from nowhere.
**The anchor is what makes a fade feel considered instead of random**, and it is free, because the geometry is
already known.

The reduced form of a Relate is the best thing in this document: **the shared element does not move at all
while the world changes around it.** The item title holds its exact position and size; the card dissolves and
the Focus surface resolves behind it. That is more legible than the animated version, it costs zero
displacement, and it is why the layout note in 4.15 matters. If Areas and Focus placed the item title at the
same height, this would be exact rather than approximate.

**3. Depiction is untouched, and it is where the reduced app gets its life.**

The focus arc still depletes at 1Hz. The undo line still runs its five seconds. The Pulse's ground still shifts
through the day. The ribbon's numbers are still the week's numbers. **A reduce-motion user's app is not still.
It is a calm app in which the only thing that moves is the thing that is actually changing**, which is arguably
a better app than the full-motion one. Saying that out loud is the strongest available answer to "the reduced
form must be a design".

## 6.4 The reduced form, surface by surface, stated once

Because the transform is one rule, this table is short, and its shortness is the deliverable.

| what | reduced |
|---|---|
| every entrance | identical schedule, identical springs, `travel = 0.dp` |
| LATERAL | **identical.** It was already opacity only |
| DEEPER, SHALLOWER | alpha only. Scale held at 1.0, because a full screen is over two thirds of the viewport |
| LIFT | sheet still travels: it is a surface entering from the edge it is anchored to, and it is direct manipulation on dismiss. Scrim still position-linked, so it is correct for free |
| the dim | **identical.** It was already opacity plus a 3 percent scale |
| the lift, the Pulse | as LIFT |
| the iris | the black crossfades away on `effectsChange`. The one thing the reduced app loses outright, once per lifetime |
| shared elements | **not suppressed. Frozen.** The title holds its position; the world changes around it. Per 6.3 |
| the completion hero | the strike still draws, the Replace still runs, **both haptics still fire**. Only the 7dp drop and the 10dp rise go to zero |
| the swipe arm | **kept.** The Delete face still leaves your thumb. Width is a bounds change in place, not a translation |
| drag to reorder | pick-up scale kept, the dark world's row dimming kept, placement kept. Nothing was translating that the finger was not driving |
| predictive back | **the preview is kept, by opacity.** A back preview is a navigation aid, not decoration: it tells a person where back goes before they commit, and deleting it costs an accessibility user information. Destination behind fades to `progress * 0.4`, current screen to `1 - progress * 0.6`, no scale, no translate, cancel restores |
| the tutorial cutout | **still travels.** It is a spotlight following a target, and a cutout that teleports between targets is the failure item 19 already names |
| every Depict | untouched |

## 6.5 Calm mode

Calm mode joins the same boolean and adds **nothing beyond it.**

`design-v3.md` 16.2 says calm mode removes entrances entirely rather than reducing them. **Under this framing
that is the one place where the calmer setting produces a *less* legible screen**, because the reading order
goes with the entrance. **Decision: calm mode keeps the Reveal, at the full `rowStep` stagger with zero rise,
making calm mode's motion delta identical to reduce motion's and leaving calm mode to differ only in color,
which is what 16.4 already says it is.** This supersedes 16.2; see Part 9 item 12.

`CALM_TUTORIAL_PULSE_OPACITY` is deleted, because 2.1 deletes the loop it was compensating for.
`REDUCED_GLOW_OPACITY` survives only if the glow survives, and the glow survives only bound to a running
session.

---

# PART 7. ENFORCEMENT

## 7.1 The mechanism, and the obvious answer refused

The statistically common 2026 answer to "fail the build on a hardcoded dp" is a **custom Android Lint
detector**, or **detekt**, or **Konsist**. Refused, on three technical grounds and one house one:

1. **Lint's API is not stable.** `lint-api` carries no stability contract across AGP versions, and this build
   already documents an AGP 9 incompatibility at the top of the root `build.gradle.kts`. **A gate that breaks
   on the next AGP bump is a gate somebody switches off**, which is the failure mode `verifyLanguageHygiene`'s
   own KDoc is written against.
2. **Lint issues default to warnings** and have to be individually raised to `FATAL`. A source-scan test is a
   failure by construction, with nothing to configure and nothing to accidentally relax.
3. **Konsist reads declarations, not expressions.** It can see `private val BADGE_SIZE = 26.dp`. It cannot see
   `Modifier.padding(12.dp)` inside a composable body, which is where 239 of the 634 literals are. That is a
   technical fact, not a preference.
4. **This repository already has six gates in two mechanisms and they work.** A seventh mechanism is a seventh
   thing to learn.

**So: JUnit source scans in the house style, under
`app/src/test/java/com/kamsiob/claritynow/ui/theme/`, run by `testDebugUnitTest`, which `verifyClarity`
already depends on. No new Gradle task.** They inherit `DomainPurityTest`'s three load-bearing details: they
scan named directories rather than a package tree, they strip comment lines before matching, and **they assert
their own working directory first so they cannot pass vacuously.**

The regex weakness a parser would not have is closed by making every rule **structural rather than
positional**: not "no `.dp` at a call site", which needs a parser to decide what a call site is, but "`.dp`
exists in five files".

## 7.2 S1. `.dp` exists in five files and nowhere else

**Scans** `app/src/main/java/com/kamsiob/claritynow/`, every `.kt`, comment lines stripped.

**Permits** the token `.dp` only in `ui/theme/ClaritySpacing.kt`, `ui/theme/ClaritySizes.kt`,
`ui/theme/ClarityStrokes.kt`, `ui/theme/ClarityShapes.kt`, and `widget/WidgetDimens.kt` (Glance measures are
constrained by the launcher grid, not by this app).

**Three exemptions, each for a reason and not for a file:**

1. **`0.dp` is allowed everywhere.** It is the absence of a dimension, not a dimension. 23 sites. `Dp.Zero` is
   also allowed and is preferred.
2. **Test sources are not scanned.** `src/main` only, as `FaintInkTest` does.
3. **The migration allowlist**, 7.9.

**Not exempt: the 159 file-local `private val X = 26.dp` declarations.** They move to `ClaritySizes.kt`. That
is the point rather than a cost: `RING_DIAMETER` belongs where `design-v3.md` 11's "the diameter is 240dp" can
be checked against it, not at the bottom of `FocusRing.kt`.

**Failure message**, in the house form: file, line, offending text, the token to use instead, and the section
that says so.

```
ui/settings/SettingsComponents.kt:66: private val TITLE_CAPTION_GAP = 2.dp
  2dp is Space.hair. Use Space.hair.

ui/areas/AreasScreen.kt:206: Arrangement.spacedBy(ClaritySpacing.scaled(11.dp))
  11dp is not a step. Space.near is 10dp, 1dp away. Use Space.near.
  (scaled() is no longer needed: every step carries its own growth. See ClaritySpacing.)

ui/areas/AreasScreen.kt:204: bottom = navigationBar + TabBarHeight + TabBarInset + 76.dp
  76dp is not a token, and the ladder has nothing at 76. A gap this large usually means
  the screen wants Space.chapter (40dp) plus a real component measure, not one number
  holding both. If it is a component's own measure, name it in ClaritySizes.kt with the
  sentence of design-v3.md that fixes it, the way Sizes.ringDiameter does.

  The ladder, design-v3.md 6 as amended by MOTION_AND_STANDARDS 1.1:
    hair 2  pair 5  tight 7  near 10  block 14  page 20  section 28  chapter 40  part 56
```

## 7.3 S2. The token files cannot grow a fifteenth value

**This is the rule that makes S1 more than relocation.** Without it, S1 moves 62 arbitrary values into one file
and calls it a system.

**S2a.** Enumerate `ClaritySpacing`'s members by reflection, the trick `ContrastAudit.allThemeTokens()` already
uses on `ClarityColors`, and assert every declared `Dp` is one of the nine steps.

```kotlin
@Test
fun `every spacing token is a step on the ladder`() {
    val offenders = ClaritySpacing.declaredDimensions().filterNot { (_, dp) -> dp in LADDER }
    assertTrue(
        "a spacing token that is not a step is the fifteenth arbitrary gap arriving through the " +
            "front door instead of the back. The ladder is root 2 on the two numbers design-v3.md 6 " +
            "states, and it has an unused ninth step at 56dp for exactly this case:\n" +
            offenders.joinToString("\n") { (n, dp) -> "  $n = $dp, nearest step ${nearestStep(dp)}" },
        offenders.isEmpty(),
    )
}
```

**S2b.** Every declaration in `ClaritySizes.kt` and `ClarityStrokes.kt` carries a `design-v3.md` citation in
the KDoc block immediately preceding it, at least 40 characters, matching `ContrastAuditTest`'s existing rule
that "an exemption is a sentence of the specification, not a shrug".

**S2c.** Every radius in `ClarityShapes.kt` is one of `tight` 7, `near` 10, `page` 20, `section` 28, or
`RoundedCornerShape(percent = 50)`.

## 7.4 T1 to T5. Type

**T1. `.sp`, `FontWeight(`, `letterSpacing =`, `lineHeight =` and `fontFamily =` exist in two files:**
`ui/theme/ClarityType.kt` and `widget/WidgetTheme.kt`. Glance needs its own `TextStyle` type and cannot read
the Compose one, so it is an allowed file, not an exemption.

**Additionally banned everywhere, including in those two files' consumers:** `.copy(fontSize = ...)`,
`.copy(fontWeight = ...)`, `.copy(letterSpacing = ...)`. **A role that needs a variant is a role, declared
once.** Current offenders: 18 `.sp` sites, 10 `FontWeight(` sites, and the four `.copy(fontWeight =` sites.

```
ui/areas/ColorPicker.kt:264: style = type.caption.copy(fontSize = 10.sp)
  A role is not a size with an override. 10sp is `micro`. Use type.micro.
  If micro's weight (700) is wrong here, that is a new role, and a new role goes in
  ClarityType.kt with its tracking computed by ClarityType.trackingFor(size).
```

**T2. The tracking law.** Every Hanken role's `letterSpacing` equals
`clamp(0.032 - 0.054 * log2(size / 10.5), -0.030, 0.036)` within 0.001em, by reflection over
`ClarityTypography`. A new role gets its tracking computed, never chosen, **and the ramp becomes impossible to
break by adding a step in the middle.**

**T3. The baseline lattice.** Every role's `lineHeight` in sp is a multiple of 4. One assertion, and it is
half of Relation 1.

**T4. The size ladder.** Every role's `fontSize` is one of `{10, 12, 15, 18, 24, 30, 64}`. Failure names the
two nearest steps.

**T5. The hierarchy survives 200 percent.** The important one, and the one that catches 0.2.

Embed the AOSP `FontScaleConverterFactory` tables as test data, cited, and assert across
`{1.0, 1.15, 1.3, 1.5, 1.8, 2.0}`:

- **No inversion.** For every pair of roles A and B where A is larger at 1.0, A is still larger at every scale.
  **Note honestly that this is nearly free: the converter is a monotone piecewise-linear map, so it cannot swap
  two sizes.** The assertion exists to catch a future table that is not monotone, and it is cheap. **The real
  risk is compression, not inversion**, which is what the next assertion measures.
- **The dominant content line stays dominant.** `item / caption` stays at or above **1.60** at every scale.
  Proposed: 2.000 at 1.0 and 1.692 at 1.3. Today's `itemTitle / label` is 1.615 at 1.0 and **1.454 at 1.3**,
  and would fail this test as written, which is the point of writing it.
- **`spacingScale()` equals the ratio the density applies to `body.fontSize`, to zero error, at every scale.**
  This replaces `TextSizeScaleTest`'s "it is linear above one" with the true statement that rule was reaching
  for.

The test carries its own honesty clause in the KDoc: the tables are the platform's as of API 34, the test
asserts a property rather than the table, and **when Google changes the curve this test is what tells you.**

## 7.5 M1 to M5. Motion

**M1. No animation spec outside two files.** `spring(`, `tween(`, `keyframes(`, `CubicBezierEasing(`, `Ease*`,
a bare `Animatable(`, `durationMillis =`, `dampingRatio =`, `stiffness =`, and any integer literal on a line
naming one of them, exist in `ui/theme/ClarityMotion.kt` and `ui/theme/ClarityEntrance.kt` and nowhere else.
**A screen names a spec: `motion.effectsChange()`, never `tween(200)`.**

The sharpest failure message in the suite is for the pattern that appears six times today:

```
ui/focus/FocusRing.kt:248: durationMillis = if (motion.reduced) 150 else 400

  ClarityMotion already owns this branch, and after MOTION_AND_STANDARDS Part 6 there is no
  branch left to own: reduce motion changes travel, not duration, so both sides of this
  expression are now effectsChange(). Five other files write the same line with three
  different values for what is meant to be one transition:
    ClarityShell.kt:180 (180), AreasScreen.kt:644 (400), TrailScreen.kt:769 (400),
    ReEntryScreen.kt:113 (400), FocusChooserScreen.kt:237 (400)
  Call motion.effectsChange(). MOTION_AND_STANDARDS 2.4 and 6.2.
```

**M2. Opacity finishes first.** Every `alpha` animation resolves to `effectsInstant`, `effectsChange` or
`effectsArrive`, and `effectsArrive` only on a file-level allowlist of grounds, scrims and washes. **This is
the guarantee that no motion in this app ever delays reading past 146ms**, and it is the test form of 3.3
guarantee 2.

**M3. No horizontal travel.** No `translationX`, `slideIn*` or `slideOut*` outside four gesture files:
`SwipeableRow.kt`, `Reorderable.kt`, the onboarding drag detector, and the predictive back handler.
**Horizontal motion in this app means exactly one thing: a finger is on the element. If it moves sideways and
your finger is not on it, that is a defect**, and this is the test that says so.

**M4. Predictive back is supported everywhere.** `PredictiveBackHandler` count equals the total handler count,
and `BackHandler(` count is **zero**. This makes `design-v3.md` 10.15's "predictive back is supported
everywhere" a fact a test asserts instead of a sentence a document asserts. It is currently false at all eight
sites.

**M5. One stagger, one hold.** The integers `30`, `60`, `120` and `900` appear in `ClarityMotion.kt` and
nowhere else, and no other integer is passed to `delay(`.

## 7.6 C1. Color

`ContrastAuditTest` and `FaintInkTest` are total over the *theme*, and neither can see a color declared outside
it. `SettingsComponents.kt:110-118` declares **seven raw hex colors** outside `ui/theme/`. Measured, they run
**4.731 to 4.835 to one** against `design-v3.md` 13's floors, so they pass. **The finding is not that they
fail. It is that nothing knows they pass**, and `ContrastAuditTest`'s own coverage test was written to be
impossible to narrow and was narrowed by declaring a color somewhere it does not look.

**Rule: `Color(0x`, `Color.White` and `Color.Black` fail outside `ui/theme/`.** `Color.Transparent` is allowed,
for the same reason `0.dp` is.

The cascade is the proof the gates compose: moving the seven badge colors into `ClarityColors` drags them into
`allThemeTokens()`, which fails "every color token the theme declares has a role" until somebody writes a
sentence about each, which puts them in front of the contrast audit against every ground in four worlds
including calm mode, **which is where 16.4's "the one place where serving one accessibility need could break
another" finally gets exercised for a color that is currently invisible to it.**

Current offenders: 9 `Color(0x` sites plus roughly 40 `Color.White` / `Color.Black` sites, mostly
`ScrollEdge.kt`'s gradient stops, which are a mask and become a named `Mask` token.

## 7.7 H1. Headers

**H1a. Every screen resolves to a declared header role.** Scan every `*Screen.kt` and `*Route.kt` under `ui/`.
Each either calls `PageHeader(` or appears in `HEADERLESS`, a map from file name to the sentence of
`design-v3.md` that permits it, with the same minimum-length rule the contrast audit puts on its exemptions.

```kotlin
private val HEADERLESS = mapOf(
    "FocusSessionScreen.kt" to
        "design-v3.md 11: \"Six elements only\", and a page header would be a seventh.",
    "PulseRoute.kt" to
        "design-v3.md 11: the amber night is one observation and its answer, and 11 fixes " +
            "the room at 520dp collapsing to 320dp with nothing above the line.",
    "FocusCompleteScreen.kt" to
        "design-v3.md 11: \"There is no dot and no color on this screen\", the quietest " +
            "surface in the app, and a header is furniture on it.",
)
```

**H1b. A header style is reachable only through its component.** `type.title` and `type.voice` appear only in
`ui/components/PageHeader.kt`. `type.sidehead` appears only in `ui/components/ClarityText.kt`. **This is the
enforceable form of "one component rather than eight layouts"**, and it catches the four hand-rolled sideheads.

**H1c. Every eyebrow sits above a `voice` line.** `type.caption` is used inside `PageHeader.kt` only in the
eyebrow slot, and the eyebrow slot renders only in `Led` mode. Structural, **so 15.1's "badge above a centered
headline" is foreclosed by the component's shape rather than by a reviewer noticing.**

## 7.8 What the gates cost to run

All eleven assertions are line scans over roughly 100 files, plus reflection over three data classes and some
arithmetic. `ScrollEdgeTest` and `FaintInkTest` already do the same walk and the suite does not notice them.
**Total added runtime: under one second.** None needs a device, a font, or a rendered frame, which is what
keeps them inside `verifyClarity` rather than in an instrumented suite nobody runs.

## 7.9 The allowlist, which is what makes a 634-site migration safe

Every gate ships on day one with a `MIGRATION` map from file path to its current offending count.

```kotlin
/**
 * Files not yet migrated, with the number of literals each still has.
 *
 * This list only ever shrinks. [MIGRATION_TOTAL] is asserted against the sum, so a commit that
 * adds a literal to a file already on the list fails just as loudly as one that adds a literal
 * to a file that is not, and there is no way to make room by editing a number upward.
 */
private val MIGRATION = mapOf(
    "ui/areas/AreaSheets.kt" to 61,
    "ui/areas/ColorPicker.kt" to 37,
    "ui/areas/AreasScreen.kt" to 25,
    "ui/trail/TrailScreen.kt" to 25,
    "ui/momentum/MomentumScreen.kt" to 24,
    // ... 55 more
)
private const val MIGRATION_TOTAL = 634
```

Three assertions: a file not on the list has zero; a file on the list has **at most** its stated count; and the
sum equals `MIGRATION_TOTAL`, so lowering a count is a deliberate edit that shows in the diff.

**The build is green from the commit that introduces the gate, and no new debt can enter from that moment.**
That is the whole trick, and it is why this is a plan rather than a big-bang rewrite that gets abandoned at 60
percent.

## 7.10 The migration order, and why

**Type before space**, because the type change alters every line box and vertical rhythm is measured against
line boxes. Doing space first means doing it twice. Type is also the smaller change with the louder payoff, so
it is the right thing to put on a phone first.

**Space before motion**, because travel distances are ladder steps and the motion work would otherwise be
written against numbers that are about to move.

**Components before screens.** `ui/components/` is 13 files and about 60 literals, and every screen inherits
the result. Migrating `Buttons.kt`, `ClaritySheet.kt`, `Fields.kt`, `SwipeableRow.kt` and `UndoSnackbar.kt`
changes the look of all thirteen pages for 60 edits.

**The four tabs before everything else**, because they are what the owner looks at. **Onboarding and the
tutorial last**, because they are seen once and a half-migrated state costs least there.

| # | stage | files | edits | ends with |
|---|---|---|---|---|
| 1 | `ClaritySpacing`, `ClaritySizes`, `ClarityStrokes`, `ClarityShapes` rewritten; all eleven gates; the allowlist | 4 new, 6 test | 0 call sites | green, nothing visibly changed |
| 2 | type ladder, the eleven roles, the tracking law, `opsz` from rendered size | ~40 | ~120 | on the phone |
| 3 | `PageHeader`, `Sidehead` as the only path, H1 | ~12 | 10 screens each losing 15 to 30 lines | on the phone |
| 4 | space migration: `ui/components/` | 13 | ~60 | |
| 5 | space migration: the four tabs | 8 | ~120 | on the phone |
| 6 | space migration: sheets, pushed screens, Settings, About, Archive | 18 | ~170 | |
| 7 | space migration: Focus, Pulse, onboarding, tutorial | 27 | ~170 | allowlist empty |
| 8 | `ClarityMotion` rewritten to the seven springs; `ReducedMotion` deleted; `Modifier.arrives`; M1 to M5 | ~20 | ~60 | |
| 9 | choreography, surface by surface, in the order of Part 4 | ~25 | ~200 | on the phone after each tab |
| 10 | interaction: the press path, `AnchoredDraggableState`, reorder, the position-linked scrim | ~15 | ~120 | on the phone |
| 11 | predictive back, all eight sites | 8 | ~40 | |
| 12 | shared elements, and the `FocusSurfaceStore` disposal fix | 6 | ~50 | **the one architectural risk** |
| 13 | color tokens and C1 | ~10 | ~25 | |

**Roughly 60 files and 1,135 edits, across thirteen commits. This is not an afternoon.** Anyone who says
otherwise has not counted the 159 named constants that have to be read, understood, categorized as space or
size or stroke, and given a citation. **That reading is most of the work and it is also most of the value**,
because it is the first time anybody will have looked at all 62 dimensions in this app side by side.

**Green at every step.** Each stage ends with `./gradlew verifyClarity` and `./gradlew :app:installDebug`, and
`adb logcat -d | grep -A 20 "FATAL EXCEPTION"` after, because several defects in this project were silent exits
that a screenshot passed. The allowlist means a half-finished stage is a valid, committable, green state, so
**stopping in the middle costs nothing.** `allWarningsAsErrors` is on, so a token that stops being used fails
the build as an unused declaration, which is a free check that the migration removed something rather than
adding a parallel path.

**Two existing tests this deliberately breaks, named up front so nobody discovers them as a surprise:**
`TextSizeScaleTest`'s "it is linear above one", replaced by T5; and any assertion pinning `ClaritySpacing`'s
current twelve values.


---

# PART 8. THE BUDGET

Motion costs frames and it costs attention. Both are stated here, and if the answer had been that the app
takes noticeably longer to use, that would be a failure and this section would say so.

## 8.1 The frame budget

A Pixel 8 renders at **120Hz, 8.33ms per frame**, dropping to **60Hz, 16.67ms** on the always-on and low-power
paths. Every number below is against 8.33ms.

**What an animation in this app is allowed to be.** An entrance is a `graphicsLayer` reading `alpha` and
`translationY` on already-composed, already-measured, already-laid-out content. That is a **draw-phase** read:
no recomposition, no measure, no layout. It costs one render-node property update per animated element, which
is tens of microseconds, and it is why 3.3 guarantee 1 is written the way it is.

**Three ceilings, each of which is also a design rule you have already read:**

1. **At most 12 concurrently animating `graphicsLayer`s on any screen.** Areas peaks at 7: title, banner, five
   cards. The Report peaks at 9: eyebrow, headline, seven ribbon bars. Momentum peaks at 6 above the fold plus
   one Canvas. **The cap at 4 stagger steps is what holds this**, because without it a 40-row Trail would
   arrive as 40 layers.
2. **A row of more than 8 marks animates inside one Canvas, never as N layers.** That is why 3.4 makes a mark
   row one element with an internal sweep rather than a stagger of children: 14 dots as 14 layers is 14 render
   nodes for a row that occupies 30dp of height.
3. **No animation may cause a measure or layout pass**, with two named exceptions: the archive collapse and the
   undo expand, which are genuine height animations. **At most one height animation may be in flight at a time,
   and never concurrently with an entrance.** This is also why `sharedBounds` uses `ScaleToBounds` rather than
   `RemeasureToBounds`: a remeasure per frame is the one thing this budget cannot absorb, and it would relayout
   the item title on a screen where the item title is the point.

**Drawn strokes are the expensive class**, because a stroke draw is a Canvas redraw per frame rather than a
render-node property. **At most 3 concurrent drawn strokes.** The Report has four sidehead rules and they run
at 480, 540, 600 and 660, so they are sequential by construction rather than by a rule.

## 8.2 What is dropped first on a slow device, and it is one list

The app cannot detect "slow", so this is not an automatic tier. It is the order in which a human removes
things, and **it is the same order the reduce-motion switch already removes them in**, which means there is one
list and Part 6 has already implemented it.

| order | dropped | cost of dropping it |
|---|---|---|
| 1 | **travel.** Every `pair` / `tight` / `near` rise and drop to 0 | nothing. It is the least informative half of a Reveal |
| 2 | **scale on anything over a third of the viewport.** DEEPER, the world dim's 0.97, the Report headline's 0.96 | the depth axis. A push and a tab change stop being distinguishable, which is a real loss and is why it is second |
| 3 | **shared elements.** The title holds position instead of traveling | the only thing that touches layout, so it is the largest single frame saving in the app |
| 4 | **drawn strokes.** Ribbon bars, sidehead rules, the focus ring's arrival, the strike-through | Canvas redraws, and the strike is the one that costs information, which is why it is last |

**Never dropped, at any tier, on any device:** every Depict, every stagger, and every opacity. Those three are
the app's information, its reading order, and its legibility, in that order.

## 8.3 The total added animation, opening the app and completing one item

The errand: cold start, Areas arrives, tap the top area card, tap Complete in the sheet, the sheet dismisses.

| step | what the person waits for | first time in a session | every time after |
|---|---|---|---|
| the app opens | the `Areas` title legible | **146ms** | 146ms |
| | the fifth card legible | **506ms** | **0ms**, no entrance on a return |
| tap the card | the press acknowledged | **131ms** | 131ms |
| | the item title in the sheet legible | **326ms** | 326ms |
| | the last sheet control legible | 446ms | 446ms |
| tap Complete | **the next item legible** | **277ms** | 277ms |
| | the wash finished | 412ms | 412ms |
| **total exposure** | | **1364ms** | **858ms** |

**Not one millisecond of that is a block.** Every target is hittable at frame one, every string is in the
accessibility tree at frame one, TalkBack reads a screen in full regardless of alpha, and every spring
retargets, so **a person who taps faster than the animation is never made to wait for it.** The 1364ms is
exposure, not latency, and the distinction is the whole of the answer.

### Against what the app does today

| interaction | today | proposed | direction |
|---|---|---|---|
| press to visible acknowledgment | ~290ms (100ms tap timeout plus 193ms spring) | **131ms** | **159ms faster, on every tap in the app** |
| Areas, last card legible | 550ms (200ms stagger plus a 350ms tween) | **506ms** | 44ms faster, and correctly ordered |
| the Report reveal | its stated ceiling is 1.4s | **926ms legible, 1000ms at rest** | 400ms of headroom |
| the promotion hero | ~430ms, and it teleports the card back | **412ms, with a strike-through added** | slightly faster and materially more legible |
| tab change | 180ms, 50/50 double exposure at the midpoint | **146ms, fading through the ground** | 34ms faster, and it stops being two screens at once |
| eight surfaces with no entrance | 0ms | **506ms, once per app session each** | **this is the cost** |

**Worst case, honestly stated:** a person who opens all thirteen surfaces exactly once in a session pays
**about 4.0 seconds of arrival across the entire session**, none of it blocking, spread over eight surfaces
that today cut into existence. That is the price of the thing the owner asked for, and it is the only price.

### The failing condition, so it can be checked rather than argued

> **Measure the time from touch-down to the first frame in which the pressed state is visible, on an area card
> inside the `LazyColumn`. Today it is about 290ms. The target is 131ms. If that number is ever above 160ms,
> this document has failed, regardless of how good anything else looks.**

Two more, in the same form:

- **No screen's above-the-fold content is legible later than 520ms after the screen appears**, except the
  Report, whose ceiling is 1.4 seconds and which is the app's one ceremony.
- **No frame in any animation in this document takes longer than 8.33ms on a Pixel 8.** Verify with
  `dumpsys gfxinfo` after each choreography stage, not at the end.

**The answer to the question the assignment asks is no: the app does not take longer to use.** Every repeated
interaction gets faster, the press gets 159ms faster on every tap, and the only thing that gets slower is the
first arrival of a surface that currently has no arrival at all.

---

# PART 9. THE DIFF AGAINST `design-v3.md`

CLAUDE.md says a genuine contradiction stops and asks. These are the contradictions, stated so they can be
approved or rejected one at a time. **Nothing in `design-v3.md` is edited by this document.** Items 1 to 14
are recommended for adoption; item 15 is flagged as the one to photograph both ways before agreeing.

### 1. Section 6, the spacing grid

> "**Spacing.** 4dp base grid. Screen padding 20dp. Card padding 18dp horizontal, 17dp vertical. Vertical
> rhythm between cards 11dp. Section spacing 28dp. Sheet content top padding after the handle 18dp."

**Becomes:** root 2 on 20 and 28, nine steps, resolving onto a 2dp lattice. Card padding becomes a squish inset
at `page`: **20 inline, 14 stack.** Card rhythm becomes `near` **10**. Sheet content top becomes `page` **20**.

**Reason:** this is arguably a correction rather than a change, because **six of the twelve tokens the document
names already sit off the grid it declares**, including the two in this very sentence. A linear 4dp ladder was
scored against the app's 634 real literals and lost on both axes: 14 to 17 percent steps at the top, and 50
percent of sites moving anyway. 1.1.

### 2. Section 6, shape

> "**Shape.** Content cards 18dp. Rows 12dp. Bottom sheets 28dp top ... Buttons 12dp. ... Widget internals 8dp.
> Momentum tiles 11dp. Settings icon badges 8dp. ... Color picker swatches 16dp."

**Becomes:** four steps of the same ladder. Cards **20**, rows and buttons and tiles **10**, badges and widget
internals and swatches **7**, sheets **28**, pills at 50 percent. The app icon's 22 percent stays, because it
is a launcher asset and not a container.

**Reason:** because each ladder step is exactly half the step two above it, **insetting a container by half its
own radius makes the inner radius exactly half the outer, at every level.** A nesting question that was a
judgment becomes arithmetic. 1.1.

### 3. Section 5.3, the sans ladder

> "**The sans sizes are a ladder: 13, 15, 17, 19, 21**, one even step per rung"

**Becomes:** 10, 12, 15, 18, 24, 30, plus 64 off-ladder. Eleven roles rather than fourteen.

**Reason:** an even *step* is a decaying *ratio*, 1.154 falling to 1.105, so the top of the ladder is the
flattest part of it. Five of fourteen roles currently occupy 1.6sp of range. 0.2 and 1.3.

### 4. Section 5.3, tracking

Nine hand-picked values **become one law**,
`t(size) = clamp(0.032 - 0.054 * log2(size / 10.5), -0.030, +0.036) em`, which reproduces all nine to within
0.0006em and reproduces the timer's clamp without the timer being an exception. **Reason:** a new role then
computes its tracking instead of guessing it. 1.3.

### 5. Section 10.1, the Areas title

> "Serif title at displayTitle, left aligned"

**Becomes:** `title`, serif **24sp**, not 30sp.

**Reason:** at 30sp the screen's own name is **1.43 times the active item**, and the active item is the one
string the app exists to render. At 24sp against `item` 24sp/700 the item wins on weight and family at equal
size, which is the correct hierarchy. 0.2.

### 6. Section 11.1 item 3, the Report headline

> "**The headline** in displayHero, centered, generous space above and below."

**Becomes:** `voice`, serif **30sp**.

**Reason:** at 40sp the headline does not grow at all until well past 150 percent text, so at the settings this
audience uses it is the *least* responsive element on the page. At 30sp it grows to 38sp at 200 percent, and
the ratio to its own body improves at every scale. 1.3.

### 7. Section 11.1 item 4, the day initials

> "Day initials beneath at 8sp textDim."

**Becomes:** `micro`, **10sp**.

**Reason:** 8sp is 2.5sp below the app's own stated floor of 10.5sp, and it is the smallest type in the app in
an app built for people who need type larger, not smaller.

### 8. Section 8.1, the curves

Five named curves plus two tweens **become seven springs named for jobs**, plus three delays and one hold.
`springSnappy` and both tweens are deleted. **Reason:** a speed name makes every call site a taste question,
and taste questions produced twelve durations. Two tweens in a spring system are two animations that cannot be
interrupted, and interruptibility is the entire reason Material moved to springs. 2.2.

### 9. Section 8.2 item 4, the staggered entrance

> "4. **Staggered entrance.** 40 to 60ms per item, fading from 0 and translating up 16dp over 350ms easeOut."

**Becomes:** `rowStep` **60ms**, capped at 4 steps, fading on `effectsChange` (146ms) and rising **`near`
10dp** on `spatialTravel`.

**Reason:** 16dp is over half a body line box and nothing in any layout sits 16dp above where it lands, so it
reads as travel rather than as settling, **which is why the entrance currently feels like an effect instead of
like typography.** The fade gets 204ms faster. 1.1 and 3.2.

### 10. Section 8.2 items 5 and 23, the sheet scrim

Two timed fades, 200ms in and 180ms out, **become one derived value**: `scrimAlpha = 0.42f * sheetTravelFraction`.

**Reason:** a timed scrim darkens the room before the sheet arrives, which reads as the app preparing rather
than as a thing arriving, and it is wrong during a drag, an interrupted drag, a fling and a predictive back
gesture. The derived form is correct in all four for free **and deletes two numbers rather than replacing
them.** 3.6.

### 11. Section 10.3.1 and 8.2 item 21, the completion slide

> "Release past threshold runs the action with the card sliding fully off in 180ms"

**Becomes:** the card returns home on `spatialRelease` carrying the release velocity. Only the title changes.

**This is the one substantive disagreement in this document.** **Reason:** the card is the *area*; the item is
the title line. Sliding the card off says the area left, which is false, and it is the identical movement used
for delete, where a row genuinely does leave. **One movement cannot mean both "this is gone" and "this is
done".** The current implementation is also defective under either resolution: it slides off, runs the action,
then `snapTo(0f)`, so the card reappears at rest on one frame. 5.4a.

### 12. Section 16.2, entrances in calm mode

> "Calm mode goes further than 8.3 in two places: the entrances in 8.4 do not fire at all rather than firing as
> a crossfade, and the tutorial ring pulse holds"

**Becomes:** calm mode keeps the Reveal at the full stagger with zero rise, and the tutorial ring pulse is
deleted along with the loop, so calm mode's motion delta becomes identical to reduce motion's.

**Reason:** the stagger is not an effect, it is the reading order, and removing it is the one place where the
calmer setting produces a *less* legible screen for the reader who benefits from sequence most. This leaves
calm mode differing only in color, **which is what 16.4 already says it is.** 6.5.

### 13. Section 8.3, what reduce motion does

Every animation becoming a 150ms crossfade **becomes** travel to zero and nothing else. `ReducedMotion` is
deleted; there is one spring set and one boolean.

**Reason:** WCAG 2.3.3, the vestibular literature and Apple's own implementation are all about *displacement*,
and none of them is about how long an opacity change takes. Today `effectsFast`, at 95ms, is **slowed by 58
percent** under an accessibility setting that says nothing about opacity. 6.1.

### 14. Section 8.2 items 8, 9 and 19

**Item 8, the breathing glow:** survives only while bound to a running session, at which point it is a Depict
of "this is live". **Item 9, the completion bloom:** the expanding circle is deleted; the ring collapsing is a
Depict reaching zero and the check is a Replace in its slot. **Item 19, the tutorial ring pulse:** fires once
and holds, exactly like item 27's transition mark.

**Reason:** each is a loop or a flourish with no job in the taxonomy, and 15.3 already refuses "a pulse, a
breathe, a glow loop or an ambient shimmer" by name. Item 9's circle is also the one place the app celebrates,
which section 14 forbids. 2.1.

### 15. Section 11.1, four treatments and no more than four

Adding a `Led` page header to the Report is a fifth structural element, even though it uses two lines the screen
already draws. **This is the one I would want the owner to look at on a phone before agreeing**, because the
Report is read 52 times a year and it is the one screen where a header could be exactly the wrong kind of
furniture. Build it behind a flag and photograph both ways.

---

## 9.1 Section 14, replaced rather than deleted

Section 14 currently says:

> "And nothing that is still. An app that never moves is an app that feels broken, and that holds in calm mode
> too, 16.4."

and narrows itself at 15.3 to:

> "**The only thing this app moves on its own is time.** ... **A screen with no time on it does not invent
> motion to prove it is alive.**"

The owner has overridden the second sentence. **A prohibition cannot simply be deleted, because the thing it
was protecting against is real.** Here is the replacement.

> ### 14, replaced
>
> **An app is alive because it arrives, responds and changes, not because something on it is moving.**
>
> Every animation in this app belongs to one of the eight jobs in Part 2.1, and every job is caused by exactly
> one of four things: **an arrival, a touch, a state change, or the passage of real time.** Nothing in this app
> animates for any other reason, and an animation that cannot name its cause is a defect rather than a
> flourish.
>
> It follows that **a screen at rest, with no finger on it, nothing arriving and no clock on it, is still, and
> it is still because there is nothing to say rather than because motion is forbidden.**
>
> The failure the original sentence was written against is real, and it was misdiagnosed. The app did not need
> an idle animation. **It needed the eleven of thirteen surfaces that hard-cut into existence to arrive, and it
> needed the every-navigation-is-a-hard-cut problem solved.** Those are the two things this document adds, and
> they add motion throughout the app without spending a single one of 15.3's refusals.

**15.3's refusals therefore stand, entirely and unchanged**, and the replacement rule is what now makes them
consequences rather than a list:

- **A pulse, a breathe, a glow loop, an ambient shimmer.** Refused, because a loop has no cause. There is no
  arrival, no touch, no state change and no clock behind it.
- **A collapsing header, a parallax, a hide-on-scroll FAB.** Refused in 4.17, each with its own reason.
- **A number counting up while a person reads it.** Refused in 2.6 rule 8, because counting through wrong
  intermediate values is a lie for as long as it lasts.

## 9.2 One thing this document does not touch

**Backend and functionality.** Nothing proposed here changes what the app does. Three items are flagged as
functional gaps that motion revealed and motion may not fix: reorder has no edge auto-scroll (5.3b), a restored
archive card is not scrolled into view (5.4f), and `FocusSurfaceStore` disposal must be deferred before shared
elements can ship (4.15). All three belong in issues of their own.
