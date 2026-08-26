# Clarity Now by Kamsiob: Design System, v3

Visual and interaction source of truth. `MASTER_BUILD_PROMPT.md` defers to this file on every visual and interaction question.

**Companion mocks.** `clarity-now-visual-reference-v3.html` shows the screens that changed in v3. `clarity-now-visual-reference-v2.html` covers everything else and remains valid for those. Neither is shipping code.

**`design-v3.md` states every dimension in dp and is the only source for a number. The mocks are for proportion, structure and color relationships, never for arithmetic.** Their px values are not uniformly scaled: layout dimensions sit at roughly 0.82 of dp, but corner radii, icon sizes and touch targets were drawn at their true dp value. Converting a mock px figure to dp will produce wrong results. Read the number from the design document.

**American English throughout.** color, license, behavior, prioritize, ads, organize. No em dashes, no en dashes, no emojis anywhere in the UI.

---

## 0. Version history

**v2** removed six documented machine-generated design tells: the colored left border, Inter, Instrument Serif, all-caps section labels, serif italic as an accent, and elements carrying two separation devices. It replaced the type system, restructured the Report, and introduced the open-choice rule.

**v3** locks the slim card treatment, replaces the app mark, specifies swipe gestures, rebuilds the color and appearance pickers, and converts everything to American English.

| # | v3 change |
|---|---|
| A2 | slim cards locked. The alternative row treatment considered in v2 is retired |
| M1 | new app mark. The gradient square and check are gone |
| S1 | swipe gestures fully specified, section 9.3.1 |
| P1 | area color picker rebuilt as two stages |
| P2 | appearance picker rebuilt as real miniatures |
| U1 | American English, including user-facing response labels |

---

## 1. Brand Personality

Five words govern every decision: **calm, warm, honest, layered, restrained.**

**Calm.** Nothing flashes, shakes, or shames. Motion is springy but soft. Empty states are welcoming.

**Warm.** Backgrounds are never pure white or pure black. Light surfaces lean slightly cool grey with warmth in the cards. Dark surfaces are warm blacks.

**Honest.** The app never decorates data. Numbers shown are real numbers. Language observes, it does not judge. Both answers to any Pulse question feel equally valid.

**Layered.** Depth comes from soft shadow **or** a background lightness shift, never both, and never from a border on top of either.

**Restrained.** One serif family for moments that matter, one sans for everything else, area colors as accents at low opacity, never as slabs.

**Calm is not colorless.** The palette is rich, the accents are saturated, the contemplative surfaces are atmospheric. Restraint means color appears in four controlled forms only: a 7dp dot, a low opacity wash, a 60 percent tile, and the small area label. Never a filled card, a colored header, a background block, or a stripe down the edge of anything.

---

## 2. The Two Worlds

**The Daylight World** covers Areas, Momentum, Trail, Settings, About and all Daylight sheets. Soft light surfaces, faint area color washes, dark ink text, a single action blue. Under system dark mode this becomes a warm dark equivalent with identical structure.

**The Contemplative World** covers Focus, Pulse, Clarity Report and Onboarding. Always dark regardless of the theme setting. Deep warm blacks, serif typography, one accent hue per surface, subtle radial glows, tiny specks of light.

These screens ignore Light, Dark and System entirely. Implement as a separate theme scope, never as a conditional inside the Daylight theme, so they can never be accidentally inverted.

Entering a Contemplative surface feels like the room dimming: content fades and scales from 0.97, the dark background fades over roughly 350ms.

---

## 3. Color Tokens

### 3.1 Daylight, light
| token | value | use |
|---|---|---|
| canvas | `#F1F1F6` | page background |
| card | `#FFFFFF` | cards and sheets |
| raise | `#FAFAFC` | the 3 percent lightness step used *instead of* a border |
| cardWash | area accent, 5 to 7 percent | pooled toward a corner chosen by hashing the area id |
| cardWashActive | area accent, 12 to 14 percent | in-session state |
| inkPrimary | `#17171C` | |
| inkSecondary | `#17171C` at 60 percent | |
| inkTertiary | `#17171C` at 38 percent | |
| hairline | `#17171C` at 8 percent | row separators only, never on a card that also has a shadow |
| actionBlue | `#2D7FF9` | FAB, active tab, primary buttons |
| positiveGreen | `#22C55E` | completion only |
| warnAmber | `#F59E0B` | the Pulse ready dot, nothing else |
| parchment | `#EFEEE2` | weekly banner |
| deleteMuted | `#8A5A5A` | the delete swipe action only. Never a saturated red |

### 3.2 Daylight, dark
| token | value |
|---|---|
| canvas | `#0E0E13` |
| card | `#191921` (lifted, no shadow) |
| raise | `#15151C` |
| cardWash | area accent, 7 to 9 percent |
| cardWashActive | area accent, 15 to 17 percent |
| inkPrimary | `#F0EEF1` |
| inkSecondary | `#F0EEF1` at 62 percent |
| inkTertiary | `#F0EEF1` at 38 percent |
| hairline | white at 9 percent |
| actionBlue | `#4DA3FF` |
| parchment | `#211F16` |

Area label text in dark mode uses a lightened accent (blend 30 percent white) to clear 4.5:1 against `#191921`. Dots and washes use the true accent.

### 3.3 Contemplative
| token | value |
|---|---|
| deepBlack | `#0B0B10` |
| surfaceRaised | `#14141C` |
| textBright | `#F3F1EC` |
| textDim | `#F3F1EC` at 55 percent |
| textFaint | `#F3F1EC` at 32 percent |
| specks | 8 to 14 dots, 1 to 2dp, white at 3 to 6 percent, fixed seed per surface so they never re-randomize |

**Focus, indigo.** Radial gradient `#262A5E` center through `#191C42` to `#10122B` at the edges over deepBlack. Ring track white at 16 percent. Progress stroke `#8BA4FF`. Tip a filled circle in `#B9C8FF` with a soft blur.

**Pulse, amber.** Accent `#E8A15C`. Background shifts with time of day and must be felt rather than noticed: dawn 05 to 11 blends a whisper of `#2B2340` into the top; midday 11 to 17 stays neutral warm black; evening 17 to 05 blends `#2E1F14` upward from the bottom.

**Report, gold editorial.** Accent gold `#D4B16A`. Body text `#EDE9DF`. Rules are horizontal gradients fading to transparent at both ends, never solid lines. A radial gold glow at 6 to 8 percent sits behind the headline block, and a second, fainter one behind the closing line, so the page has two centers of light.

**Onboarding.** Warm black with a per beat glow: beat 1 actionBlue, beat 2 twilight violet, beat 4 cycles amber, blue, gold.

### 3.4 Area palette: 48 colors, 8 moods

All 48 available to everyone. No locked subset.

- **Ocean** `#2D7FF9` `#4DA3FF` `#18BFFF` `#1B6ACB` `#3B82F6` `#06B6D4`
- **Twilight** `#6366F1` `#4F46E5` `#7C3AED` `#8B5CF6` `#A855F7` `#C084FC`
- **Berry** `#D946EF` `#EC4899` `#F472B6` `#E11D48` `#BE185D` `#DC2626`
- **Ember** `#EF4444` `#F97316` `#FB923C` `#F59E0B` `#FBBF24` `#EAB308`
- **Meadow** `#22C55E` `#16A34A` `#4ADE80` `#10B981` `#059669` `#14B8A6`
- **Earth** `#CA8A04` `#92400E` `#A16207` `#B45309` `#D97706` `#8B7355`
- **Stone** `#A68B6B` `#78716C` `#57534E` `#7F8C8D` `#95A5A6` `#9CA3AF`
- **Slate** `#0D9488` `#64748B` `#475569` `#6B7280` `#334155` `#1E293B`

Color appears only as: a 7dp dot, a 5 to 14 percent wash, a 60 percent tile in Momentum, and the area label text. **Never as a stripe, bar, edge, border or filled block.**

Area label text uses the accent at full strength, 13sp semibold. Verify 4.5:1 against the card. If a color fails in light mode, darken the label variant only by blending 25 percent black. Never adjust the dot or wash to compensate.

**Default assignment.** New areas walk the mood groups in order taking the first color of each, so the first four are distinct without the user choosing.

---

## 4. The Mark

### 4.1 What it is

A queue seen face on: one solid card in front, two narrower cards peeking out behind it at decreasing opacity. It is the product's core idea, one thing at the front and the rest waiting, expressed as a shape.

Geometry on a 100 unit square:
- back card: `x 30, y 14, w 40, h 11, r 5.5`, currentColor at 26 percent
- middle card: `x 22, y 29, w 56, h 12, r 6`, currentColor at 50 percent
- front card: `x 14, y 46, w 72, h 40, r 11`, currentColor at 100 percent

**The proportions are load bearing.** The two behind are deliberately short so they read as edges rather than as cards in their own right. Three equal bars would read as a hamburger menu. Unequal sizes with the largest in front reads as depth.

### 4.2 Application

| context | treatment |
|---|---|
| app icon | flat `#141A2E` background, mark in `#F3F1EC`. Two colors, no gradient |
| adaptive icon | same, with the mark inset to the safe zone |
| About screen | 62dp rounded square at 16dp radius, same colors |
| notification | monochrome silhouette, single value |
| widget header | monochrome, inherits the widget's ink color |
| light backgrounds | mark in `#17171C` |
| brand blue backgrounds | mark in `#FFFFFF` |

The mark survives being reduced to one value because depth is carried by opacity, not hue. It holds legibly to 22px.

### 4.3 What the mark is not

Not a check mark, which announces the category the app argues against. Not a gradient square, which is on the tell list. Not an orb with rings, which belongs to Kam AI. Not a letter C. Not three equal bars.

---

## 5. Typography

**Two families, both variable, both SIL OFL, both bundled as files in `res/font` and subset.**

**Never Downloadable Fonts.** That API fetches over the network and this app has no `INTERNET` permission. See `MASTER_BUILD_PROMPT.md` 3.1.

### 5.1 Newsreader, the serif

One family serving two roles through its optical size axis. Ship with the `opsz` axis intact.

**Display role**, `opsz 44 to 68`, weight 400. Report headline, screen titles, onboarding statements, past-report headlines.

**Text role**, `opsz 14 to 34`, weight 400 to 500. Momentum headline, Pulse observation, Report body, the closing line, the About paragraph.

Italic exists and is available for genuine emphasis inside a sentence. **Never as a section-level accent device.**

### 5.2 Hanken Grotesk, the sans

Everything else. Weights 400 to 800. **Verify at build time:** tabular figures (`tnum`) support, and that the lightest weight is adequate for the timer numeral. If tabular figures are unavailable, use a fixed-width numeral treatment rather than switching families.

### 5.3 Scale

All in sp, respecting the user's font scale. No fixed pixel text anywhere.

| role | spec | used for |
|---|---|---|
| displayHero | Newsreader 40, opsz 68, w400, lh 1.08, tracking -0.012em | Report headline |
| displayTitle | Newsreader 30, opsz 48, w400 | screen titles |
| readSerif | Newsreader 26, opsz 34, w400, lh 1.36 | Momentum headline, Pulse observation |
| closingLine | Newsreader 24, opsz 34, w400, lh 1.42 | the Report's one thing |
| bodySerif | Newsreader 17, opsz 17, w400, lh 1.62 | Report section prose |
| itemTitle | Hanken Grotesk 21, w650, tracking -0.022em | active item on the area card |
| title | Hanken Grotesk 19, w700 | sheet titles |
| body | Hanken Grotesk 16, w400, lh 1.5 | |
| bodyStrong | Hanken Grotesk 16, w600 | |
| label | Hanken Grotesk 13, w600 | area labels, tab labels, chips |
| sidehead | Hanken Grotesk 13, w700, **sentence case** | section labels. Not all caps |
| swipeLabel | Hanken Grotesk 10.5, w700 | swipe action labels |
| caption | Hanken Grotesk 12, w400 | timestamps, footers, helper text |
| timerNumeral | Hanken Grotesk 64, w250, tabular, tracking -0.03em | focus countdown, caps at 1.3x font scale |

Every updating numeric display uses tabular figures so digits do not jitter.

---

## 6. Shape, Elevation, Spacing

**Shape.** Content cards 18dp. Rows 12dp. Bottom sheets 28dp top with a 34 by 4dp handle at 18 percent ink. Buttons 12dp. Pills fully rounded. Widget internals 8dp. Momentum tiles 11dp. Settings icon badges 8dp. App icon 22 percent radius. Color picker swatches 16dp.

### 6.1 The separation rule

**Every element carries exactly one separation device.** Apply in this order and stop as soon as it reads:

1. **Whitespace.** Usually enough.
2. **A background lightness shift of 3 to 5 percent**, or the area color wash.
3. **Soft elevation.**
4. A hairline, only if all three above have genuinely failed.

Never a hairline and a shadow on the same element. Never a border on a card that has a wash. A test walks the component set and fails the build on any element declaring two.

**Elevation values** where step 3 applies:
- Light mode card: `y 1dp blur 3dp black 4%` plus `y 6dp blur 20dp black 5%`. One device expressed as a paired shadow, not two devices
- Tab bar: `y 2dp blur 10dp black 7%` plus `y 10dp blur 30dp black 8%`
- FAB: `y 5dp blur 16dp actionBlue 40%`
- Sheets: `y -8dp blur 40dp black 28%`
- Dark and Contemplative worlds: elevation is **lightness only**. No shadows at all

**Spacing.** 4dp base grid. Screen padding 20dp. Card padding 18dp horizontal, 17dp vertical. Vertical rhythm between cards 11dp. Section spacing 28dp. Sheet content top padding after the handle 18dp.

---

## 7. Iconography

Material Symbols Rounded, weight 500. Filled for the active tab, outlined for inactive. Never multicolored; icons take ink colors or the surface accent.

Areas `home`, Momentum `arrow_outward`, Report `article`, Trail `history`, Add `add`, Focus `play_arrow`, Pulse `graphic_eq`, Settings `settings`, Archive `inventory_2`, Completed `check_circle`, Promoted `arrow_circle_up`, Focus event `timer`, Regenerate `refresh`, Report history `history`, Copy `content_copy`, Export `file_download`, Import `file_upload`, Erase `delete_outline`, Privacy `shield`, Licenses `menu_book`, Feedback `mail`, Support `favorite`, Reorder `drag_handle`, **Swap `swap_vert`**, **Delete swipe `delete_outline`**.

**No sparkle or magic-wand iconography anywhere.** `auto_awesome` reads as an AI affordance, which is the opposite of what this app claims. The Report identifies itself with a line of text.

---

## 8. Motion

**This app must not feel dead.** Every animation is soft, spring based and short.

### 8.1 Named curves

| name | spec | used for |
|---|---|---|
| springStandard | `spring(dampingRatio = 0.8f, stiffness = 380f)` | presses, promotions, selections, tab pill |
| springGentle | `spring(dampingRatio = 0.9f, stiffness = 200f)` | sheets, reveals, large elements settling |
| springSnappy | `spring(dampingRatio = 0.75f, stiffness = 600f)` | swatches, chips, small immediate feedback |
| easeOut | `tween(350, EaseOutCubic)` | entrances, fades |
| easeSlow | `tween(600, EaseInOutCubic)` | world transitions, breathing glow |

### 8.2 The catalogue. All required.

1. **Queue promotion, the hero.** Completed title strikes through and fades while sliding down 8dp over 250ms; the next queued title slides up with springStandard; the wash brightens to 11 percent and returns over 500ms. Old and new titles never both at full opacity. If only one thing is polished, it is this.
2. **Press.** Cards and buttons scale to 0.97, springStandard.
3. **Long press lift.** Scale 1.02, elevation deepens, springGentle.
4. **Staggered entrance.** 40 to 60ms per item, fading from 0 and translating up 16dp over 350ms easeOut.
5. **Sheet entrance.** springGentle from the bottom, scrim fades over 200ms.
6. **World transition.** Outgoing fades, incoming scales 0.97 to 1.0, dark fades in over 350ms easeSlow.
7. **Focus ring depletion.** Continuous at 1Hz from a single ticker Flow. Only the numeral and arc redraw.
8. **Focus glow breathing.** Opacity 0.85 to 1.0 over 8 seconds, infinite, easeSlow.
9. **Focus completion bloom.** Ring collapses inward, a soft circle expands from center over 700ms springGentle fading as it grows, then the check scales in from 0.6.
10. **Pulse pill fill.** Amber fills from the tap point over 220ms. On release the unselected pill fades to 30 percent and drops 4dp, then the acknowledgment fades in over 400ms after a 250ms hold.
11. **Pulse ambient settle.** Crossfade over 450ms, the 14 day dot row fills left to right at 30ms stagger.
12. **Report reveal.** Eyebrow, then headline scaling from 0.96 with springGentle, then **the week ribbon drawing left to right at 45ms per day**, then sections fading and rising 12dp at 90ms stagger. Under 1.4 seconds. The ribbon draw should be the most satisfying single animation after the promotion.
13. **Momentum dot cascade.** Left to right at 35ms stagger. The today ring draws last.
14. **Momentum number roll.** The three stats count up from 0 over 600ms easeOut, first entry per session only.
15. **Tab pill slide.** springStandard, icon crossfades outlined to filled.
16. **FAB press.** Scale 0.94, springSnappy.
17. **Swatch selection.** Scale 1.06 springSnappy, ring fades in over 150ms, preview wash crossfades over 250ms.
18. **Onboarding iris.** Circular reveal from center over 600ms springGentle.
19. **Tutorial spotlight.** Cutout animates between targets with springGentle. Ring pulses 0.25 to 0.45 over 2 seconds, infinite.
20. **Undo snackbar.** Rises with springGentle above the tab bar, holds 5 seconds with a depleting line, falls away.
21. **Swipe actions.** Card tracks the finger 1:1. The action background fades in from 0 as the card moves. Past the commit threshold the background deepens by 40 percent and one haptic fires. Release past threshold runs the action with the card sliding fully off in 180ms; release before it springs back with springStandard.
22. **Placeholder shimmer.** 4 percent ink moving slowly. Never a spinner.
23. **Sheet dismiss.** The reverse of the entrance but faster: slides down over 220ms with easeOut while the scrim fades over 180ms. A dragged dismiss tracks the finger and completes on release past 40 percent or on a downward fling.
24. **Tab content transition.** Switching tabs crossfades the content over 180ms with no slide. A slide implies spatial relationship between tabs and there is none; these are four views of the same data.
25. **Empty state entrance.** Fades in over 400ms easeOut after a 150ms delay, so it never flashes during a load that resolves quickly.
26. **Accept tap on the closing line.** The pill fills from the center over 250ms, the label crossfades to a confirmation, and it settles at reduced prominence. Never bounces, never celebrates, never produces a toast.

### 8.3 Reduce motion

When the animator duration scale is 0 or the accessibility setting is on, every animation becomes a 150ms crossfade. The breathing glow holds at 0.92. The ribbon appears complete. Swipe still tracks the finger but the commit is instant. The timer still updates. **One global check, not 23 individual ones.**

---

## 9. Haptics

One `ClarityHaptics` abstraction. Checks primitive support once at startup, respects the system haptic setting, falls back gracefully. No screen touches `Vibrator` directly.

| event | primitive | where |
|---|---|---|
| tap | `PRIMITIVE_TICK` 0.4 | card press, chip, tab, swatch, mood pill |
| select | `PRIMITIVE_CLICK` 0.6 | Pulse answer, segmented choice, theme tile |
| toggle | `TOGGLE_ON` / `TOGGLE_OFF` | switches |
| complete | `PRIMITIVE_TICK` 0.5 then `PRIMITIVE_CLICK` 0.8 at 60ms | item completed |
| promote | `PRIMITIVE_QUICK_RISE` 0.5 | as the new title lands |
| pickUp | `PRIMITIVE_THUD` 0.5 | long press to drag |
| putDown | `PRIMITIVE_TICK` 0.4 | drop |
| swipeThreshold | `PRIMITIVE_TICK` 0.3 | crossing the commit point, once per gesture |
| focusStart | `PRIMITIVE_LOW_TICK` twice, 90ms apart | session begins |
| focusEnd | `PRIMITIVE_QUICK_RISE` 0.6 then `PRIMITIVE_THUD` 0.7 at 120ms | natural completion |
| reportReady | `PRIMITIVE_SPIN` 0.4, or `QUICK_RISE` if unsupported | generation finishes |
| planAccepted | `PRIMITIVE_TICK` 0.5 | deliberately the same weight as an ordinary tap, because accepting is not an achievement |
| warn | `PRIMITIVE_THUD` 0.7 | destructive confirmation arms |
| reject | `PRIMITIVE_LOW_TICK` 0.3, once | an action that cannot be performed was attempted, such as a swipe on a disabled row. Deliberately quieter than a normal tap, because a rejection should feel like nothing happening rather than like being told off |
| undo | `PRIMITIVE_TICK` 0.4 | the undo action in a snackbar is tapped |
| step | `PRIMITIVE_TICK` 0.25 | a tutorial step or onboarding beat advances. The lightest event in the system, because it fires several times in a row |

Never on scroll, screen entry, notification arrival, or more than once per user action. Focus sessions fire nothing between start and end. The Pulse reminder is silent.

---

## 10. Components

### 10.1 Top of Areas
Serif title at displayTitle, left aligned, archive and settings icons at inkSecondary on the right. Below, two pill chips: Focus and Pulse, card colored, **soft elevation only, no border**. The Pulse chip carries a 6dp warnAmber dot at its top right when a Pulse is ready and unanswered.

### 10.2 Weekly banner
Full width, parchment, 14dp radius, no border, no progress track. A bodyStrong sentence and a caption line, both from the Logic Engine.

### 10.3 The area card

**A2 slim card, locked.** Card at `#FFFFFF`, 18dp radius, soft elevation, **no border**, 18 by 17dp padding, 11dp gaps between cards, the wash pooled toward the hashed corner. Five areas fit comfortably on screen.

**Content.**
- Row one: a 7dp color dot and the area name at label size in the area color
- Row two: the active item title at itemTitle in inkPrimary. **This never shrinks.** It is the most important string on the screen
- Row three: the status line, shown **only when it carries information**. Idle areas show `Last active 21 days ago`. In-session areas show the live countdown. An ordinary active area shows nothing

**Idle state.** Title reads `Add your first item` at inkTertiary weight 500. No wash. The dot drops to 45 percent.

**In-session state.** The wash doubles from 6 to 13 percent and the status line becomes `In focus, 7 minutes left` in the area color at semibold with a small play glyph. **There is no colored bar, stripe or edge treatment anywhere in this app.**

### 10.3.1 Swipe gestures

Three actions across two directions. **Delete is never reachable by a full swipe.**

| gesture | behavior |
|---|---|
| swipe right past **25 percent** | reveals **Complete**, positiveGreen at 18 percent background, check icon and label in `#15803D`, 66dp action width |
| swipe right past **55 percent** | commits Complete. One haptic tick at the threshold |
| swipe left past **25 percent** | reveals **Swap** then **Delete**, side by side, 66dp each. Swap on actionBlue at 12 percent with the area color; Delete on deleteMuted at 13 percent |
| swipe left past **55 percent** | commits **Swap** only |
| tap Delete on the revealed row | commits delete, with the 5 second undo snackbar |

**Rationale.** Destructive actions must not be committed by momentum. A full left swipe commits the safe action, and delete requires deliberate contact with a specific target.

**State gating.**
- An **idle area** offers neither Complete nor Swap. A left swipe reveals Delete only, and that deletes the **area**, with a typed confirmation rather than an undo
- An area with an **empty queue** offers Complete but not Swap. The left swipe reveals Delete only
- During an **active focus session** for that area, Complete and Swap are both available; completing ends the session naturally

**Swap opens a chooser** naming the item being demoted, so nothing disappears silently. Swaps carry no warning tone in copy or color.

**Accessibility, mandatory.** All three actions must also be reachable from a long press context menu on the card **and** from the area detail sheet. Swipe is invisible to TalkBack and is an accelerator, never the only path.

**Visuals.** The action background fades in from 0 as the card moves and deepens by 40 percent past the commit threshold. Never full bleed alarm colors. The revealed row is clipped by the card's own 18dp radius so the corners stay consistent. The action icon scales from 0.8 to 1.0 across the reveal so it arrives rather than appears.

**Commit by distance or by velocity.** A fling above 1,200dp per second commits even below the 55 percent distance threshold, which is what makes a quick flick feel responsive rather than ignored. Below that, distance decides.

**Edge cases, all required.**

- **One row open at a time.** Opening a swipe on any card closes any other open row first, with springStandard. Two open rows is a state the user cannot reason about
- **A tap anywhere while a row is open closes it** and is consumed, rather than passing through to the card underneath. The second tap does what it looks like it does
- **Swipe is disabled during a drag reorder** and re-enabled when the drag ends
- **Swipe is disabled while any sheet is open**, including the scrim
- **Swipe is disabled on the last remaining area** for the delete action only, since an app with zero areas is a state reached deliberately through the archive view, not by accident on a list
- **On delete, the row slides fully off over 180ms and then collapses its height to zero over 200ms**, so the list closes rather than snapping. The undo snackbar appears as the collapse begins, and undoing expands the row back with springGentle
- **Scroll wins over swipe.** A gesture whose initial direction is predominantly vertical is a scroll and never becomes a swipe, even if it curves

### 10.4 Floating tab bar
A 61dp tall pill inset 17dp from the edges and bottom, card colored, elevation only. Four items. The active item sits in an inner pill with actionBlue at 10 percent, filled icon and label in actionBlue. Inactive at inkSecondary. The inner pill slides with springStandard.

### 10.5 FAB
48dp circle in actionBlue, white add icon, above the tab bar at the trailing edge.

### 10.6 Sheets
All secondary flows are bottom sheets over a 42 percent scrim. 28dp top radius, handle, springGentle entrance. **No cards inside sheets.** Structure comes from sideheads.

**One exception, and only one.** The color picker's live preview in 10.9 renders an actual miniature area card, because the entire purpose of that element is to show the user what their card will look like. It is a rendering of a component, not a container wrapping content, and the distinction is what the rule is protecting against. No other sheet may contain a card, and the exception may not be extended by analogy.

### 10.7 Buttons
- **Primary:** actionBlue fill, white bodyStrong, 50dp, 12dp radius, no border
- **Positive:** positiveGreen at 13 percent, green label, no border
- **Secondary:** ink at 5 percent fill, inkPrimary label, no border
- **Tertiary:** text only in actionBlue
- **Destructive:** inert grey until its condition is met, then ink filled. Never red
- **Contemplative primary:** the surface accent at 14 percent with a bright label, or a translucent white pill at 9 percent

### 10.8 Chips and filters
Pill chips in a horizontally scrolling row. `All` is a solid ink pill with inverted text when selected. Area chips carry their color dot. Unselected chips are card colored with soft elevation, no border.

### 10.9 The area color picker, two stages

**Stage one, the mood strip.** A horizontally scrolling row of eight mood pills. Each pill is a 46dp wide, 26dp tall band at 8dp radius, divided into six equal vertical slivers showing that mood's six colors. **Discrete slivers, never a gradient.** The mood name sits beneath at 10sp in inkTertiary, becoming inkPrimary at weight 700 when selected. The selected pill gains a 2dp ring in inkPrimary at a 1.5dp offset.

**Stage two, the swatches.** The six colors of the selected mood in a three by two grid, each a square at 16dp radius with 12dp gaps. The selected swatch scales to 1.06, gains a 2.5dp ring in the swatch color at 50 percent offset by 3dp, and shows a white check at 20dp.

**Above both, the live preview.** An actual miniature area card in the currently selected color, updating instantly on every tap, including the wash. This is the premium moment of the app and it is the one place worth spending animation budget on a crossfade.

Switching mood re-renders stage two with a 40ms staggered entrance. The previously selected color is retained if it belongs to the newly selected mood, otherwise nothing is selected until the user taps.

### 10.10 The appearance picker

Three tiles, each a **real miniature** of the Areas screen rather than grey bars: the correct canvas color as the tile background, a title bar at the correct ink opacity, and three miniature area rows at the correct card color, each carrying a real 4dp area dot in Ocean, Meadow and Earth.

- **Light** renders `#F1F1F6` canvas with `#FFFFFF` rows
- **Dark** renders `#0E0E13` canvas with `#191921` rows
- **System** splits diagonally at 103 degrees, showing both halves including the rows

Tile height 84dp, 12dp radius, 9dp gaps. The selected tile carries a 2dp actionBlue ring and a 14dp filled actionBlue check badge at its top right. Labels sit beneath at 9.5sp, becoming actionBlue at weight 700 when selected.

Below the row, one caption line: `Focus, Pulse and Report are always dark by design.`

### 10.11 Settings rows
**No card containers.** Rows sit directly on the canvas separated by hairlines, grouped under sideheads. Each row carries a 26dp rounded-square icon badge tinted at 11 to 14 percent of a per-group color, a title at 15sp semibold, a trailing value at caption inkTertiary, and a chevron where it navigates.

### 10.12 Sideheads
A **sentence-case label** at sidehead spec, followed by a hairline running to the trailing edge, vertically centered on the label. Gold in the Report, inkSecondary in the Daylight world.

`Your week, honestly` not `YOUR WEEK, HONESTLY`. `Area balance` not `AREA BALANCE`.

### 10.13 Empty states
Invitations, never scolds, naming the next action in plain words. No illustration, no mascot, no exclamation marks.

### 10.14 Snackbar
Rises above the tab bar, card colored, 12dp radius, one line plus an action in actionBlue, with a thin depleting line showing the 5 second window. Undo only.

---

### 10.15 Navigation and back

**Every screen has an obvious way out, and no screen is reachable only by accident.**

### Structure

Four tabs at the root: Areas, Momentum, Report, Trail. Everything else is a bottom sheet or a pushed screen over one of them. There is no drawer, no hamburger, no hidden navigation.

| destination | entered from | left by |
|---|---|---|
| Area detail | tapping a card | drag down, scrim tap, or back |
| Add item | the FAB, or the add row in a detail sheet | same, or after adding |
| Edit area | the settings glyph in the area detail sheet | same |
| Color picker | the edit area sheet | Save, or back without saving |
| Archived areas | the archive glyph in the Areas header | back |
| Settings | the settings glyph in the Areas header | back |
| About, Privacy, Licenses | rows in Settings | back |
| Erase confirmation | the Erase row in Settings | Keep my data, or back |
| Focus chooser | the Focus chip | back |
| Focus session | choosing an area | End session, or back, see below |
| Pulse | the Pulse chip | drag down, scrim tap, or back |
| Pulse history | the History row in Pulse ambient mode | back |
| Report history | the history glyph on Report | back |
| Swap chooser | swipe left then Swap, or the detail sheet | Never mind, or back |
| Queue chooser | completing an item when the setting is Choose from queue | dismiss, which leaves the area idle |

### Back behavior

**Predictive back is supported everywhere.** Every destination declares its handler so the system's back preview shows the correct destination rather than a blank frame. This is a visible quality difference on Android 14 and later and its absence reads as an unfinished app.

- **On a sheet:** back dismisses it, identically to a drag down
- **On a non-root tab:** back returns to Areas
- **On Areas:** back exits the app. No double-tap-to-exit prompt, which is a pattern that exists only where navigation is confusing
- **In a focus session:** back **navigates away and leaves the session running.** It does not end it, does not prompt, and does not warn. The ongoing notification is the way back in, and the Areas card shows the live countdown. Ending a session is a deliberate act with a button, never a side effect of navigating
- **In onboarding:** back returns to the previous beat, and is hidden on beat 1
- **In the tutorial:** back skips the whole tutorial, identically to the Skip control, because stepping backwards through a five step overlay is not something anyone wants
- **In the erase confirmation:** back dismisses without erasing, and the typed text is discarded

### Two states that are easy to leave unhandled

**Zero areas.** Reachable by archiving or deleting everything. The Areas screen shows an empty state reading `No areas yet. An area is a part of your life you want to keep track of.` and **the FAB creates an area rather than an item** while this state holds. The tab bar stays visible and the other three tabs show their own empty states.

**A queued item tapped.** Opens an edit sheet with title, note, a Delete row, and a Move to front action. This is the only way to edit a queued item and it must exist, since the queue is otherwise read-only.

### First launch

Cold start reads two flags in order. `hasCompletedOnboarding` false routes to onboarding. Otherwise, `hasSeenTutorial` false routes to Areas with the tutorial queued to start once the first frame has settled. Otherwise, Areas. **Onboarding beat 3 writes the selected areas as real events**, so a user who force-quits after beat 3 and relaunches lands on a populated Areas screen rather than starting over.

---

## 11. Surface Art Direction

**Areas.** The Daylight home. Five areas fill the screen comfortably. Must pass a three second test: what is active everywhere, at a glance.

**Focus.** The indigo night. Five elements only: area label with dot, item title in bold sans at 26sp, the 240dp ring with the timer numeral, the word `remaining` in textFaint, and the End session pill. Nothing else, ever.

**Focus complete.** Ring replaced by a circle bloom and check. `Session complete` in serif, item title, a small line reading duration and area, then `Mark item complete` in the accent and `Done` beneath.

**Pulse.** The amber night. The observation in readSerif centered, the question in body at textDim, then response pills. After answering, an acknowledgment fades in, then ambient mode: a 14 day rhythm row, today's answered card, and a History entry. Filled amber means answered, a hollow ring means generated but unanswered, faint means a silent day.

### 11.1 The Report

**Four treatments and no more than four**, because this is read 52 times a year and anything clever becomes exhausting by the tenth reading.

1. **Controls**, faint, top right. History, regenerate, copy.
2. **Eyebrow.** `Clarity Report · Week of July 13` in caption at textDim, centered. One line.
3. **The headline** in displayHero, centered, generous space above and below.
4. **The week ribbon.** The glance layer. Seven vertical marks, one per day, 5dp wide at 2dp radius, height and opacity scaled to that day's activity against the week's busiest. Gold. Day initials beneath at 8sp textFaint. Below it one caption line reading the three headline numbers.
   **Constraints.** No axes, no gridlines, no values on the marks, no card around it, no gradient. The only non-text element in the entire report. It repeats at 60 percent scale in the past reports list and nowhere else.
5. **A gold rule**, full width, fading at both ends.
6. **Sections.** Each a sidehead followed by bodySerif prose, 28dp apart. Sentence-case labels: `Your week, honestly`, `What you said`, `Focus`.
7. **The pattern break.** The `Pattern` section only. Bleeds to full screen width, sits on gold at 4.5 percent, inset 30dp horizontally so its measure is visibly narrower, set in Newsreader at opsz 28 rather than 17, bounded top and bottom by full-bleed gold rules. **The one deliberate grid break.**
8. **The closing line.** 34dp of space above. Centered. A caption eyebrow reading `One thing`. The line in closingLine, **roman, never italic**, at textBright. Beneath it two options: an accept pill in gold at 14 percent reading `I'll do that`, and a decline in text only at textFaint reading `Not this week`. Both optional, both costless, neither ever mentioned again.
9. **Footer.** `Generated on your device`, then the basis line, both in textFaint at caption.

The background gradient extends under the status bar to the very top edge.

**Momentum.** Daylight, the calmest screen in that world. Headline in readSerif, the 14 dot row, area tiles, three stats as pure typography with no cards, then insight modules under sideheads.

**Trail.** Daylight. Day headers as bodyStrong with an inline count and a hairline to the trailing edge. Events as icon plus text rows, the icon a 23dp circle tinted with the event color. Completed events get a mint wash card at positiveGreen 8 percent.

**Settings.** Rows on canvas under sideheads. Order: Daily routine, Focus, After completing, Appearance, Your data, Privacy, Help, then the support block, then the version line.

**About.** App mark at 62dp, name in displayTitle, version and `by Kamsiob`, one paragraph in bodySerif, a quiet link list under an `Elsewhere` sidehead, then the support block, then license lines. Links findable but subordinate; the support block is the only warm colored element on the screen.

**Paywall.** Does not exist.

**Onboarding and tutorial.** Contemplative. The tutorial uses a 56 percent black radial dim, a cutout with an 8dp feathered edge, a slowly pulsing 2dp white ring at 38 percent, and tooltip cards in surfaceRaised with a step indicator.

---

## 12. Widgets

One design DNA: 16dp padding, serif for the single large element, sans for everything else, area tints at 3 to 5 percent light and 5 to 7 percent dark, 8dp inner radii, **no borders and no colored edges**. Every widget renders correctly in dark mode, scales text without clipping at the smallest grid size, and shows a sensible state when its area no longer exists.

---

## 13. Accessibility

- Contrast minimum 4.5:1, verified per area color in both modes
- Touch targets minimum 48dp
- Content descriptions everywhere, reading sensibly aloud
- TalkBack order verified per screen. Pulse reads observation, question, options. The Report reads eyebrow, headline, **then a spoken summary of the ribbon** ("busiest on Wednesday, quiet at the weekend"), then sections
- **All swipe actions duplicated in a long press context menu and in the detail sheet**
- Font scale to 200 percent without clipping. The timer numeral caps at 1.3x
- Color is never the only signal. Idle versus active differ in text and opacity. Completed Trail events carry the check icon. The Pulse ready state is a dot plus a changed chip label. **The ribbon is never the sole carrier of any claim; the caption beneath states the numbers**
- Contemplative text stays at or above 55 percent opacity where it is meant to be read
- Reduce motion honored globally

---

## 14. What This Design Never Does

No pure white or pure black backgrounds. **No colored stripe, bar or edge treatment on any element, ever.** No red warning states for normal behavior. No streak breaking or shame mechanics. No confetti. No emojis. No em or en dashes. **No all-caps section labels.** **No serif italic as a section-level accent.** **No element carrying two separation devices.** **No cards inside cards, and no cards inside sheets except the color picker preview in 10.9.** **No destructive action committed by a full swipe.** No default Material purple. No dynamic color from the wallpaper. No stock Material list rows on primary screens. No loading spinners where a shimmer can stand in. No sparkle or magic-wand iconography. No locked features, upgrade prompts, premium badges or comparison tables. No screen that fails the three second glance test.

And nothing that is still. An app that never moves is an app that feels broken.

---

## 15. The open-choice rule

**When this design system leaves a choice open, the answer is never the most statistically common option available.**

This exists because v1 was highly specified and still produced six independently documented machine-generated design tells. Specification alone does not protect you, because a specification written at one moment encodes the defaults of that moment.

In practice: when something is not pinned down here, identify what the obvious answer would be and then deliberately choose something else that serves the brief as well or better. If the obvious answer is genuinely best, use it and record why in the changelog.

### 15.1 The current tell list, dated August 2026

Checked before every release. **Expect this list to change.** Treat it as a dependency needing updates, not a rule that stays true. The research behind each entry is logged in `docs/DESIGN_RESEARCH.md`.

- A colored 3 to 4px border or stripe on one side of a card
- Inter as the interface typeface
- Instrument Serif, particularly paired with Inter
- Space Grotesk paired with a display serif
- Serif italics used as an accent device
- All-caps section labels
- Lavender or indigo-to-purple gradients
- A gradient applied to a large number for impact
- Glassmorphism used as decoration rather than to solve a layering problem
- A hairline border and a diffuse shadow on the same element
- Cards nested inside cards
- A row of three identical feature cards with a thin-line icon at the top
- A badge sitting above a centered headline
- Numbered 1-2-3 step sequences
- Stat banners
- Sparkle or magic-wand iconography
- Dark mode with low contrast body text
- A gradient rounded square with a check mark as an app icon
- A blue to purple gradient, the single loudest tell of 2026
- A purple to cyan gradient
- Glassmorphism combined with a neon glow
- Six identical cards in a row
- A bounce on every hover or press, rather than overshoot reserved for weight
- Weightless headline copy, the `Build faster. Ship smarter.` register
- Interchangeable thin line icons with no relationship to the product

### 15.2 Release gate

The verification checklist includes an anti-slop pass against the dated list above. **Update the list before each release** rather than trusting the version in this file.
