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
| S1 | swipe gestures fully specified, section 10.3.1 |
| P1 | area color picker rebuilt as two stages |
| P2 | appearance picker rebuilt as real miniatures |
| U1 | American English, including user-facing response labels |

**v3.1** records **Addendum 01: Executive Function Support**, an owner directive transcribed at `docs/addenda/ADDENDUM_01_EXECUTIVE_FUNCTION.md`. It adds calm mode, the analog time rule, the platform-first rule, the Live Update surface and the eight-widget family, and it forbids one word. **Everything it adds is pending.** This revision is a record, not a build. The reasoning behind the direction, and the four things it deliberately rules out, are in `DECISIONS.md`.

| # | v3.1 change | where |
|---|---|---|
| CM1 | calm mode, one switch that turns motion and saturation down together | 16 |
| PF1 | platform first, and the six components this app builds by hand | 17 |
| IC1 | Material Symbols wherever a symbol fits, and a mapping table | 7.1, 7.2 |
| AT1 | analog time: duration reads as a shape before it reads as a number | 11.3 |
| RE1 | the re-entry state, the highest-stakes screen in the app | 11.2 |
| LU1 | the Live Update, and the standing rule that it is the only one | 11.4 |
| WD1 | the widget family, eight specified and six required | 12 |
| IB1 | the unfiled inbox, the first step field, the estimate | 10.16, 10.17 |
| FC1 | add ten minutes, and the transition warning mark | 8.2, 10.18 |
| ST1 | entrance animations fire once per session | 8.4 |
| LG1 | one forbidden word, and the words this app cannot use | 13.1, 14 |
| OB1 | a `Just start` path, and Pulse announced before it appears | 11 |
| AX1 | where the accessibility investment goes, and one refusal | 13 |

Two of those changes contradict something v3 had settled, and neither is resolved silently. The focus surface moves from five elements to six, 11. A focus session can now fire one haptic between start and end, section 9. Both are recorded where they land and both are logged in `DECISIONS.md`.

**v3.2** records what phase 3b, the executive function retrofit, actually built, and it is the first revision since v3.1 in which anything Addendum 01 added is real. Calm mode, section 16, is built and audited: 16.6 states what it does to every animation in 8.2, 16.7 what it does to every token in section 3 with a number against each, and 16.8 what it does to every component shipped in phases 1, 2 and 3. The entrance rule in 8.4 is built and is now stated once rather than repeated beside items 4 and 14. Two things came out of the audit that were not calm mode's: the area label's contrast was being verified against the wrong ground and was failing at 3.83 to one on an in-session card, 16.7, and the platform bottom sheet cannot honor calm mode at all, 16.8. Both are recorded rather than quietly fixed or quietly left.

**v3.3** records **phase 12b, the surfaces half of the polish pass.** Phase 3c took tokens and type, because those are inherited free by screens that do not exist yet; this half took the choices that could not be judged with two screens on the table, and it ran after the three Contemplative worlds the deferral was waiting for were built. Content stops passing hard edged under the status bar and behind the floating tab bar, 6.1. Section 10 gains a field entry it never had, 10.19, and a field stops being four stacked underlined rules. 10.4 states the tab label rule instead of inheriting a platform default. Section 11 says what the Trail's icon column carries, which turned out to be a proof that it could not carry a color at all. And section 14's "nothing that is still" is narrowed to what is true: the only thing this app moves on its own is time.

**Two of these are recorded refusals rather than changes, and that is an outcome the phase was designed to allow.** 6.1's sheet shadow has no call site and cannot get one through the platform sheet, so 6.1 now says so and says what carries the separation instead. And nothing new moves at rest, because every conventional way to make section 14's sentence true is on 15.1 or beside it, and none of them says anything about a person's data.

**One question this revision does not answer.** 10.6's "no cards inside sheets, structure comes from sideheads" was written for the area detail sheet, and applied to the filing chooser it produces rows with no affordance at all. That is still open, the answer is still not a card, per 15.3, and it belongs to whoever next touches 10.6.

| # | v3.3 change | where |
|---|---|---|
| SE1 | scroll edges fade at both ends, and the blur is refused and tested | 6.1, 15.3 |
| SH1 | the sheet shadow is unreachable through the platform component, and the scrim already separates | 6.1, 15.3 |
| TL1 | one tab label of four, stated with the measurement behind it | 10.4 |
| TF1 | a field is a sidehead and a well | 10.19 |
| TR1 | the Trail's icon column is a glyph, and there is no event color | 11 |
| RM1 | the only thing that moves at rest is time | 14 |
| TK1 | 3.1 and 3.2 carry the values phase 3c shipped | 3.1, 3.2 |

**3.1 and 3.2 were stale for four phases and that is worth naming.** Phase 3c moved six tokens in the code and recorded the reasoning in `ClarityColors.kt`, in three commits and in two tests, and did not update the two tables in this document that are the authority for them. So the file that wins on every visual question said `card` was `#FFFFFF` while the app drew `#FCFBF9`, and every later phase read a table that was wrong. The values now match the code, and the reasoning that lived only in Kotlin lives here too.

**Sections 16 and 17 are appended rather than inserted**, because section numbers in this document are cited by the master prompt, by the engine document and by comments in the code, and renumbering would silently break every one of them.

---

## 1. Brand Personality

Five words govern every decision: **calm, warm, honest, layered, restrained.**

**Calm.** Nothing flashes, shakes, or shames. Motion is springy but soft. Empty states are welcoming. Calm is also a setting: section 16 states what calm mode turns down, and it exists so the expressive direction can be taken further everywhere else.

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

In calm mode the dimming is a crossfade and the atmosphere is flatter, but the two worlds are still two worlds. Calm mode never turns a Contemplative surface into a Daylight one, and never turns either into grey. Section 16.

---

## 3. Color Tokens

### 3.1 Daylight, light

**These three neutrals are a ladder, not three greys.** Ground, then the rank below
content, then content. The order and the size of each step are held by
`SurfaceLadderTest` rather than by this table, because a table says what the values are
and only the relationships decide whether a person can see a card.

| token | value | use |
|---|---|---|
| canvas | `#E6E6EC` | page background, and a focused field's well |
| card | `#FCFBF9` | cards and sheets |
| raise | `#F4F3F0` | the 3 percent lightness step used *instead of* a border. The floating tab bar, an unselected chip, a text field's well |
| cardWash | area accent, 5 to 7 percent | pooled toward a corner chosen by hashing the area id |
| cardWashActive | area accent, 12 to 13 percent | in-session state |
| inkPrimary | `#17171C` | |
| inkSecondary | `#17171C` at 64 percent | |
| inkTertiary | `#17171C` at 38 percent | never on text. See below |
| hairline | `#17171C` at 8 percent | row separators only, never on a card that also has a shadow |
| actionBlue | `#004BAE` | FAB, active tab, primary buttons, every text button's label, and the Swap swipe face and its label |
| positiveGreen | `#22C55E` | completion only, and **a fill only**: the 13 percent positive button, the Trail's 8 percent mint, the 18 percent Complete swipe face |
| positiveInk | `#03652B` | every green foreground: the completion check, the positive button's label, the Complete swipe face's icon and label |
| warnAmber | `#F59E0B` | the Pulse ready dot, nothing else |
| parchment | `#EFEEE2` | weekly banner |
| deleteMuted | `#724444` | the delete swipe action only. Never a saturated red |

**Four of these values moved in phase 3c and this table is where they are recorded.**
`canvas` went from `#F1F1F6` and `card` from `#FFFFFF`, which resolved the contradiction
sections 1 and 14 had with this one: both say backgrounds are never pure white, this
table said `#FFFFFF`, and the build had followed the one rather than the two. Card
against canvas went from 1.126:1 to 1.202:1 and the ladder's span from 4.73 to 7.19 L*.
`raise` went from `#FAFAFC`, which sat 1.7 L* under the card and would have been
invisible even if anything had drawn it. `inkSecondary` went from 60 percent because it
had to: on the new canvas 60 percent measures 4.33:1 against section 13's floor of 4.5,
and 64 percent measures 4.88. Changing a background is not a background change; it is a
change to every ratio measured against it.

**Three of the function colors moved in the phase 13 contrast audit and one is new, and this table is where that is recorded.** The audit measures every pair the app can put on a screen rather than the pairs somebody listed, and it found three tokens being asked for a job their value could not do. Each replacement is a value chosen against a measurement, on the old token's own OKLab hue.

- **`actionBlue` `#2D7FF9` to `#004BAE`.** One color was carrying two jobs it could not both hold: as a fill it has to be dark enough for a label to sit on it, and white measured **3.81:1** on the old value; as text it has to be dark enough to read on the ladder, and it measured **3.06:1** on the canvas at five call sites from the tertiary button to the undo snackbar. Both wants point the same way, so one value serves both. The ladder now reads 6.45, 7.23 and 7.76 and the selected tab's label on its own 10 percent pill reads 6.14. The value is pinned by the hardest of its grounds, which is 10.3.1's Swap face: that face is this same token at 12 percent deepening to 16.8, so the token has to be legible on its own tint, and that is what takes it past `#0058C8` to `#004BAE`.
- **`deleteMuted` `#8A5A5A` to `#724444`, and to `#B98685` in dark, 3.2.** One value in both worlds is only possible for a token nothing reads, and this one carries the Delete action's glyph and label on a face tinted from itself. It measured 3.66:1 on its own face in light and 2.94:1 on the dark card.
- **`positiveInk` is new, and it is the reason `positiveGreen` did not move.** The completion color is a fill in three places and every one of them has to stay light enough for what sits on it: section 11 calls the Trail's completed ground a mint, which a forest green at 8 percent is not. As a foreground the same value measured 1.83:1 on the canvas and 1.68:1 as the positive button's own label. The two wants are opposite, so this table states two tokens rather than one compromise. **`positiveGreen` now carries no foreground anywhere in this app**, which a source gate holds rather than a floor, the same way `inkTertiary` is held below.
- **A filled action surface inverts its label to `card`**, 10.5 and 10.7, which is the inversion 10.8 already used for the destructive button and the selected chip. In the dark world there is no blue that is both light enough to read on `#0E0E13` and dark enough to hold white, so one rule replaces the word white in both worlds.

**`inkTertiary` carries no text anywhere in this app.** It measures 2.40:1 on the card
and 2.37:1 on the canvas, which is section 15.1's "dark mode with low contrast body
text" in its light mode form. **Four sections record the correction and each one is
where it happened**: 10.3 on the string this document calls the most important one on
the screen, 10.19 on a field's placeholder, 10.9 on the mood name in the color picker,
and 10.11 on a settings row's trailing value, caption and chevron. It survives as an
opacity for shapes that are not read, and the test of that is section 13's own rule
rather than the word shape: the idle mark in Momentum's rhythm row and in the Rhythm
widget is drawn **smaller** as well as fainter, so the two states differ in form and
opacity is never the only signal. **A floor cannot hold this sentence and a source scan
does**, `FaintInkTest`, because a contrast test measures the pairs it is handed and has
no way to notice a screen that has quietly started drawing a token.

### 3.2 Daylight, dark
| token | value |
|---|---|
| canvas | `#0E0E13` |
| card | `#1D1D25` (lifted, no shadow) |
| raise | `#18181F` |
| cardWash | area accent, 7 to 9 percent |
| cardWashActive | area accent, 15 to 16 percent |
| inkPrimary | `#F0EEF1` |
| inkSecondary | `#F0EEF1` at 62 percent |
| inkTertiary | `#F0EEF1` at 38 percent |
| hairline | white at 9 percent |
| actionBlue | `#4DA3FF` |
| positiveGreen | `#22C55E`, a fill only, as in 3.1 |
| positiveInk | `#22C55E` |
| deleteMuted | `#B98685` |
| parchment | `#211F16` |

Area label text in dark mode uses a lightened accent (blend 30 percent white) to clear 4.5:1 against the card it actually sits on, which is the card carrying that area's own wash rather than the bare token. Dots and washes use the true accent.

**`actionBlue` is deliberately held at `#4DA3FF` while the light world's moved, and `deleteMuted` and `positiveInk` are stated here because they differ from 3.1.** An action color has to be legible on the ladder of its own world and the two ladders run in opposite directions, so light needs a dark blue and dark needs a light one. What the dark world cannot then do is put white on that blue: white measured **2.63:1** on the primary button's label and on the FAB's glyph, and no value is both light enough to read on `#0E0E13` and dark enough to hold white. The label inverts to `card` instead, 10.7, which reads 6.38:1 here. `positiveInk` holds `positiveGreen`'s own value in this world, because a mint light enough to tint a near black surface is already light enough to be read on one, at 8.45:1 on the canvas; the pair exists so that the light world's difference between a fill and a foreground is visible in the token set rather than hidden in a branch at a call site.

**`card` and `raise` moved in phase 3c on dark's own measurement rather than by analogy**, from `#191921` and `#15151C`. Dark has the same shape of problem as light and one aggravating factor: 6.1 gives it no shadows, so the lightness ladder is not one of its separation devices, it is the only one. **`canvas` is deliberately held.** Dark's depth had to be bought upward, which is the reverse of the statistically common answer of taking a dark theme toward pure black for OLED: section 14 bans pure black outright, and the Contemplative `deepBlack` is only 0.97 L* below this canvas, so a darker Daylight ground would collapse the two worlds' floors and take the room dimming out of entering Focus. Section 15.

### 3.3 Contemplative
| token | value |
|---|---|
| deepBlack | `#0B0B10` |
| surfaceRaised | `#14141C` |
| textBright | `#F3F1EC` |
| textDim | `#F3F1EC` at 55 percent |
| textFaint | `#F3F1EC` at 32 percent, never on text. The Contemplative twin of 3.1's `inkTertiary` |
| specks | 8 to 14 dots, 1 to 2dp, white at 3 to 6 percent, fixed seed per surface so they never re-randomize |

**`textFaint` carries no text anywhere in this app either, and section 11 is where that
was corrected.** It measures 2.64:1 on `deepBlack` against section 13's floor of 4.5,
and 13's own sentence puts the line at 55 percent: Contemplative text stays at or above
55 percent opacity where it is meant to be read. Section 11 named it for the word
`remaining` on the Focus surface, and 11.1 for the Report's controls, its day initials,
its decline and its footer; all five now read `textDim`, at 5.70 on the same ground. It
is held to shapes by the same source scan that holds `inkTertiary`, `FaintInkTest`, and
for the same reason.

**Focus, indigo. Built, phase 4.** Radial gradient `#262A5E` center through `#191C42` to `#10122B` at the edges over deepBlack. Ring track white at 16 percent. Progress stroke `#8BA4FF`. Tip a filled circle in `#B9C8FF` with a soft blur.

The gradient's center sits at 0.5 across and **0.42 down**, above the middle, because that is where the ring is and the pool of light is the room the ring sits in. It is in the same place on the chooser and on the completion screen, which have no ring, since a light that moved when the content changed would make the room itself feel like it had moved. It reaches 0.72 of the surface diagonal, so the darkest stop arrives before the corners and the corners are the edge color rather than a fourth value. The blur on the tip is drawn as a radial falloff at 15dp and 38 percent rather than as a mask filter, which is what a blurred point of light looks like and costs one draw call instead of an off screen pass. The specks are placed by hashing a per speck key with `StableHash` rather than by a seeded random, which gives one arrangement per surface that survives a recomposition, a rotation and a process death with nothing to store.

**Pulse, amber.** Accent `#E8A15C`. Background shifts with time of day and must be felt rather than noticed: dawn 05 to 11 blends a whisper of `#2B2340` into the top; midday 11 to 17 stays neutral warm black; evening 17 to 05 blends `#2E1F14` upward from the bottom.

**Report, gold editorial.** Accent gold `#D4B16A`. Body text `#EDE9DF`. Rules are horizontal gradients fading to transparent at both ends, never solid lines. A radial gold glow at 6 to 8 percent sits behind the headline block, and a second, fainter one behind the closing line, so the page has two centers of light.

**Onboarding.** Warm black with a per beat glow: beat 1 azure `#2D7FF9`, beat 2 twilight violet, beat 4 cycles amber, blue, gold. Beat 1's glow was written as "actionBlue" and is now written as its value, because the phase 13 audit took `actionBlue` to `#004BAE` and a glow at 9 percent behind a beat is atmosphere rather than the action color: it is the same hue, and it did not follow the token down because a Contemplative surface with a dimmer center of light is not calmer, it is a darker rectangle.

Every accent and gradient in this table passes through the calm mode transform in 16.2. The gradients keep their geometry and lose their intensity, because a Contemplative surface with no center of light is not calmer, it is a black rectangle.

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

Color appears only as: a 7dp dot, a wash between 3 and 16 percent, a 60 percent tile in Momentum, and the area label text. **Never as a stripe, bar, edge, border or filled block.** The wash range is the span of every depth 3.1, 3.2 and 12.1 permit, from a widget's 3 percent tint to a dark in-session card's 16; it read "5 to 14 percent" until the phase 13 audit, which covered neither end.

**The four forms are a closed list and phase 13 is where that was tested.** 10.3.1 asked the Swap swipe action to draw its 22dp glyph and its 10.5sp label in the area's accent, which is a fifth form, and it failed design-v3.md 13's floor on 43 of the 48 colors in light and 28 in dark, worst at 1.03:1. The remedy this section names, darkening the label variant, does not reach that ground: on a card the variant sits on a 13 percent wash of its own hue, so a small blend keeps it recognizable, while a swipe face is a fixed blue and 44 of the 48 would have to move, a median of 34 percent toward black and five of them by more than half. A color blended past half is no longer the color it identifies. The face gave up the accent instead, 10.3.1, and identity stays where this section puts it: the card being swiped carries the dot and the area's name eight dp away.

Area label text uses the accent at full strength, 13sp semibold. **Verify 4.5:1 against the card as drawn, which is the card carrying that area's own wash at the deepest opacity the design permits, 13 percent in light and 16 in dark. Not against the bare `card` token.** If a color fails in light mode, darken the label variant only by blending 25 percent black; in dark mode lighten it by blending 30 percent white. Never adjust the dot or wash to compensate.

The distinction in that first sentence is not pedantry. Verifying against the bare token clears on all 48 colors and is what shipped, and the same labels measure as low as 3.83:1 on an in-session card. The measurement, and the twenty eight label variants that moved when it was corrected, are in 16.7.

**Default assignment.** New areas walk the mood groups in order **starting at Berry**, taking the first color of each that is not one of this document's own function colors, so the first four are distinct from each other and from every colored control on the screen without the user choosing. The first eight are `#D946EF`, `#EF4444`, `#16A34A`, `#CA8A04`, `#A68B6B`, `#0D9488`, `#18BFFF`, `#6366F1`, and the walk then wraps into each mood's next color.

**Four colors are never assigned automatically, and all four remain choosable.** `#4DA3FF` is `actionBlue` in dark, `#22C55E` is `positiveGreen` and `#F59E0B` is `warnAmber`, byte for byte, so an area carrying one puts identity and function at the same pixel value, and an identity indistinguishable from a status is not an identity. **`#2D7FF9` is the fourth, and phase 13 changed why rather than whether:** it was `actionBlue` in light until that audit took the light token to `#004BAE`, which is the same OKLab hue at a lower lightness. An area drawn in it sits beside a FAB and a primary button that are that color brightened, which is the collision this list exists to prevent, so it stays out of the walk on its hue rather than on its bytes. Byte identity was never the point; it was the evidence. This is **not** a locked subset: the sentence at the top of this section still holds, the picker still offers all 48, and what changed is only what the app hands out on its own. A color someone chooses is their decision; a color the app assigns is the app's, and the app should not make this one.

**Recorded under section 15, because the walk's start was an open choice made badly.** The shipped walk began at Ocean and gave the first area `#2D7FF9`, so on the first run screen, where the only two colored elements are one area and one FAB, they were the same color. It also gave area five `#22C55E`, the completion color, which the fix above catches and a change of starting point alone could not: the walk reaches both inside the first eight areas from every start. On the choice of Berry: this section asks the walk for distinctness, and the shipped start delivered `#2D7FF9` at hue 216 next to `#6366F1` at 239, a 23 degree step and two blues in a row. Berry's first four are 292, 0, 142 and 41, whose narrowest step is 68 degrees, the widest that any of the eight possible starts produces. The obvious move, one step along the list to Twilight, is the worst available on both counts: `#6366F1` is 25 degrees from `actionBlue`, which sits at hue 214 since phase 13, and it walks toward the family 15.1 names twice. Earth and Stone sit further from `actionBlue` than Berry but put two muted yellows 8 degrees apart in the first four, which trades this defect for the one this sentence is about.

**Calm mode leaves identity alone.** The 7dp dot and the area label text keep their accent at full strength, because they are how an area is recognized and they are the two places a contrast ratio was verified. Washes, tiles and every atmospheric use of the accent desaturate. The transform, and the list of what is excluded by name, are in 16.2.

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

**Tracking is part of the scale, not a note on two rows of it.** Every role carries
a value, and a role without one is a defect rather than a default.

| role | spec | tracking | used for |
|---|---|---|---|
| displayHero | Newsreader 40, opsz 68, w400, lh 1.08 | -0.012em | Report headline |
| displayTitle | Newsreader 30, opsz 48, w400, lh 1.2 | 0, opsz | screen titles |
| readSerif | Newsreader 26, opsz 34, w400, lh 1.36 | 0, opsz | Momentum headline, Pulse observation |
| closingLine | Newsreader 24, opsz 34, w400, lh 1.42 | 0, opsz | the Report's one thing |
| bodySerif | Newsreader 17, opsz 17, w400, lh 1.62 | 0, opsz | Report section prose |
| itemTitle | Hanken Grotesk 21, w650 | -0.022em | active item on the area card |
| title | Hanken Grotesk 19, w700, lh 1.26 | -0.014em | sheet titles |
| body | Hanken Grotesk 15, w400, lh 1.5 | +0.004em | list rows, sheet prose, field text, snackbar message |
| bodyStrong | Hanken Grotesk 17, w600, lh 1.35 | -0.006em | Trail day headers, button labels, emphasis inside a run of body |
| label | Hanken Grotesk 13, w600 | +0.016em | area labels, tab labels, chips |
| sidehead | Hanken Grotesk 13, w700, **sentence case** | +0.024em | section labels. Not all caps |
| swipeLabel | Hanken Grotesk 10.5, w700 | +0.032em | swipe action labels |
| caption | Hanken Grotesk 12, w400 | +0.022em | timestamps, footers, helper text, the first step line |
| timerNumeral | Hanken Grotesk 64, w250, tabular | -0.030em | focus countdown, caps at 1.3x font scale |

Every updating numeric display uses tabular figures so digits do not jitter.

**The sans ramp.** A face drawn to fit at reading sizes leaves proportionally too
much room between letters when it is set large and too little when it is set small,
because the space does not need to grow at the same rate the letters do. So the sans
scale opens at 15sp and below, crosses zero between 15 and 17, and closes from 17sp
up, on a curve that flattens as the size rises: +0.032, +0.022, +0.016, +0.004, -0.006, -0.014, -0.022, -0.030. `sidehead` is
the one value deliberately off that curve, at +0.024em against `label`'s +0.016em at
the same 13sp, because a section label has to read as a marker and the conventional
way to buy that is capitals, which this section and 15.1 both forbid. Tracking says
the same thing at a fraction of the volume and leaves the sentence case intact.

The serif roles are not on the ramp. Newsreader carries an optical size axis and
every serif role sets it, which is the same correction made inside the outlines
rather than between them; `displayHero` adds -0.012em on top at 40sp.

**The sans sizes are a ladder: 13, 15, 17, 19, 21**, one even step per rung, with 12
and 10.5 beneath it for captions and swipe labels. `body` and `bodyStrong` were both
16sp through phase 3b, which left no size step available inside a run of text, and
section 11 gives a Trail day header to `bodyStrong` and the rows under it to `body`,
so a day header could outrank a row by weight alone. `body` drops to 15 and
`bodyStrong` rises to 17, which puts most of the step below rather than above,
because `bodyStrong` is also the button label in 10.7 and the undo action in 10.14
and one step is as far as either can move without starting to shout. 16sp is in any
case Material's `bodyLarge` default, so moving off it is the choice section 15 asks
for, and 10.11 already sets a settings row title at 15sp.

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
- Sheets: `y -8dp blur 40dp black 28%`, **which is not drawn.** See below
- Dark and Contemplative worlds: elevation is **lightness only**. No shadows at all

**The sheet value has never had a call site, and phase 12b settled that rather than fixing it.** A sheet's shadow points up out of its own top edge, so only something outside the sheet can draw it, and the platform `ModalBottomSheet` in 17.3 exposes nothing outside it that an app can reach: its parameter list carries a shape, a container color, a content color, a tonal elevation and a scrim color and no shadow of any kind, and the caller's own modifier attaches to a node that stays at the top of the window while the sheet's content is placed at an offset inside it. 17.4 says a polish pass raises an unreachable idea as a decision rather than reimplementing a working component, and this is the second entry of that kind against this component; 16.8 carries the first.

**It costs nothing, and the reason is this section's own rule.** A sheet sits on a 42 percent scrim, which takes the ground under it from the canvas to roughly L* 56 while the sheet is at L* 98.6. That is a 42 point step, and this section says to stop at the first device that reads. It reads at step 2. The shadow would have been the third device on an element that already had one, which is the thing the first line of this section forbids.

**Scroll edges.** Content that scrolls under the status bar or behind the floating tab bar in 10.4 fades out rather than ending at a hard edge: fully gone at the screen edge, fully present 12dp past the status bar and 16dp above the tab bar's top edge. The fade removes the content's own opacity rather than painting the ground over it, so it is correct over the Contemplative gradients as well as over a flat canvas, and it takes no color. **It is not a second separation device on the tab bar**, and the reason is structural rather than an argument: it belongs to the scrolling surface, not to the bar, and the bar still carries exactly one device, elevation in the light world and a lightness step in the dark one. **The obvious answer here is a translucent blur and it is refused**, by 15.1's "glassmorphism used as decoration rather than to solve a layering problem" and by 15.3, which writes out this exact case. A fade is also the truer of the two: behind the tab bar there is a page, and a page is what a reader should see less and less of. Built in phase 12b.

**Spacing.** 4dp base grid. Screen padding 20dp. Card padding 18dp horizontal, 17dp vertical. Vertical rhythm between cards 11dp. Section spacing 28dp. Sheet content top padding after the handle 18dp.

---

## 7. Iconography

Material Symbols Rounded, weight 500. Filled for the active tab, outlined for inactive. Never multicolored; icons take ink colors or the surface accent.

Areas `home`, Momentum `arrow_outward`, Report `article`, Trail `history`, Add `add`, Focus `play_arrow`, Pulse `graphic_eq`, Settings `settings`, Archive `inventory_2`, Completed `check_circle`, Promoted `arrow_circle_up`, Focus event `timer`, Regenerate `refresh`, Report history `history`, Copy `content_copy`, Export `file_download`, Import `file_upload`, Erase `delete_outline`, Privacy `shield`, Licenses `menu_book`, Feedback `mail`, Support `favorite`, Reorder `drag_handle`, **Swap `swap_vert`**, **Delete swipe `delete_outline`**.

**No sparkle or magic-wand iconography anywhere.** `auto_awesome` reads as an AI affordance, which is the opposite of what this app claims. The Report identifies itself with a line of text.

### 7.1 A symbol first, a drawn glyph second

Iconography follows the platform-first rule in 17. A Material Symbol is used wherever one carries the right meaning. Symbols are committed as vector drawables rather than pulled from an icon font at runtime, because that fetch would need a network permission this app does not have.

The custom SVG icons in the reference mocks are illustrative and are not shipping assets.

Where no symbol carries the right meaning, one is drawn: the same style, the same weight 500, the same optical size, the same corner treatment and the same 24dp grid as the set around it, and recorded as custom in 7.2. A drawer of mismatched stock icons is worse than one well drawn addition, and the failure this protects against is a set that is 90 percent Rounded and 10 percent something else, which reads as carelessness long before anyone can name why.

**This is already the case.** Phase 2 committed 41 Material Symbols Rounded icons at weight 500 as vector drawables, named by meaning in `ui/components/ClarityIcons.kt` so that no screen reaches for a resource id directly and a mapping can change in one place. `docs/BUILD_STATE.md` records the set. There are no custom glyphs in it, and the only drawn asset in the app is the mark in section 4, which is a mark and not an icon.

### 7.2 The mapping table

**Built, phase 13.** Every icon the app ships, read out of `ClarityIcons.kt` and `res/drawable/` rather than from memory. The name this app calls it, the resource, the Material Symbols name it comes from, and where it is used.

**Forty one glyphs, and forty of them are Material Symbols Rounded at weight 500, Apache License 2.0.** Each drawable carries that provenance in a comment at the top of the file, so the claim is checkable one file at a time rather than only here. The one exception is the mark, which is not an icon.

| this app calls it | resource | Material Symbols | used in |
|---|---|---|---|
| `areas` / `areasFilled` | `ic_home` / `ic_home_filled` | `home` | the tab bar, the Trail's area events |
| `momentum` / `momentumFilled` | `ic_arrow_outward` / `ic_arrow_outward_filled` | `arrow_outward` | the tab bar |
| `report` / `reportFilled` | `ic_article` / `ic_article_filled` | `article` | the tab bar, the Trail's report events |
| `trail` / `trailFilled` | `ic_history` / `ic_history_filled` | `history` | the tab bar, the Report's history page |
| `add` | `ic_add` | `add` | the FAB, add sheets, the Trail's add events |
| `focus` | `ic_play_arrow` | `play_arrow` | the focus chip, Settings |
| `pulse` | `ic_graphic_eq` | `graphic_eq` | the Pulse chip, Settings, the Trail |
| `settings` | `ic_settings` | `settings` | the Areas header, the Trail |
| `archive` | `ic_inventory_2` | `inventory_2` | area archive, the Trail |
| `unarchive` | `ic_unarchive` | `unarchive` | the Trail's restore events |
| `completed` | `ic_check_circle` | `check_circle` | completion rows, the Trail |
| `promoted` | `ic_arrow_circle_up` | `arrow_circle_up` | the Trail's promotion events |
| `focusEvent` | `ic_timer` | `timer` | the Trail's session events, Settings |
| `regenerate` | `ic_refresh` | `refresh` | the Report, Settings, the Trail |
| `copy` | `ic_content_copy` | `content_copy` | the Report |
| `export` | `ic_file_download` | `file_download` | Settings |
| `importData` | `ic_file_upload` | `file_upload` | Settings |
| `erase` and `deleteSwipe` | `ic_delete_outline` | see the note below | Settings, the swipe action |
| `privacy` | `ic_shield` | `shield` | Settings |
| `licenses` | `ic_menu_book` | `menu_book` | Settings |
| `feedback` | `ic_mail` | `mail` | About |
| `support` | `ic_favorite` | `favorite` | About |
| `reorder` | `ic_drag_handle` | `drag_handle` | the Trail |
| `swap` | `ic_swap_vert` | `swap_vert` | the swap action, the Trail |
| `check` | `ic_check` | `check` | everywhere a choice is confirmed, and the widgets |
| `close` | `ic_close` | `close` | onboarding |
| `chevron` | `ic_chevron_right` | `chevron_right` | rows that push a screen |
| `back` | `ic_arrow_back` | `arrow_back` | every pushed screen |
| `expand` | `ic_expand_more` | `expand_more` | the queue disclosure, the Trail |
| `edit` | `ic_edit` | `edit` | the Trail's edit events |
| `editArea` | `ic_tune` | `tune` | area edit, Settings |
| `moveToFront` | `ic_vertical_align_top` | `vertical_align_top` | the queue overflow |
| `reminders` | `ic_notifications` | `notifications` | Settings |
| `appearance` | `ic_palette` | `palette` | the Trail's color events |
| `time` | `ic_schedule` | `schedule` | Settings, the Trail |
| `openExternal` | `ic_open_in_new` | `open_in_new` | About's Elsewhere rows |
| `overflow` | `ic_more_vert` | `more_vert` | **nothing. See below** |
| `mark` | `ic_mark` | **custom, and not an icon** | About, Settings |

**Three things this table found, which is what a mapping table is for.**

**`ic_delete_outline` is the one resource whose name is not its symbol's name.** Material Symbols Rounded has no `delete_outline`; its glyph for this meaning is `delete`, and `delete_outline` is the name the older Material Icons set used. The file records itself as Material Symbols Rounded weight 500 like every other, so the resource almost certainly carries the right glyph under a legacy name. **Rename it when something else is being done to that file**, not on its own: it is referenced from two `ClarityIcons` entries and nothing else, and a rename for tidiness is a commit whose only effect is churn.

**`overflow` is declared and used by nothing.** It is `more_vert`, which is the glyph a kebab menu hangs from, and this app does not have one: 10.7 gives the queue row a long press and an expanding disclosure instead. Left in place rather than deleted, because it is one line and the row above records why it is empty, which is more useful than its absence.

**`arrow_outward` is the weakest glyph in the set, and 12b's tab rule is what surfaced it.** An unlabeled tab has to be decodable on its own and an arrow pointing out of a box is not a word anyone reads as Momentum. It is recorded here rather than fixed, because a replacement is a new drawable rather than a token and nothing among the forty already committed fits the meaning. The two candidates worth a look are `trending_up`, which is the obvious answer and reads as a chart going up, which is the growth framing section 1 rejects, and `landscape`, which does not. **The mark is the only asset in this app drawn by hand**, and section 4.1 covers it. No Material Symbol was considered for it, because a logo is not an icon.

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
4. **Staggered entrance.** 40 to 60ms per item, fading from 0 and translating up 16dp over 350ms easeOut. An entrance, so 8.4 governs when it fires.
5. **Sheet entrance.** springGentle from the bottom, scrim fades over 200ms.
6. **World transition.** Outgoing fades, incoming scales 0.97 to 1.0, dark fades in over 350ms easeSlow.
7. **Focus ring depletion.** Continuous at 1Hz from a single ticker Flow. Only the numeral and arc redraw.
8. **Focus glow breathing.** Opacity 0.85 to 1.0 over 8 seconds, infinite, easeSlow.
9. **Focus completion bloom.** Ring collapses inward, a soft circle expands from center over 700ms springGentle fading as it grows, then the check scales in from 0.6.
10. **Pulse pill fill.** Amber fills from the tap point over 220ms. On release the unselected pill fades to 30 percent and drops 4dp, then the acknowledgment fades in over 400ms after a 250ms hold.
11. **Pulse ambient settle.** Crossfade over 450ms, the 14 day dot row fills left to right at 30ms stagger.
12. **Report reveal.** Eyebrow, then headline scaling from 0.96 with springGentle, then **the week ribbon drawing left to right at 45ms per day**, then sections fading and rising 12dp at 90ms stagger. Under 1.4 seconds. The ribbon draw should be the most satisfying single animation after the promotion.
13. **Momentum dot cascade.** Left to right at 35ms stagger. The today ring draws last.
14. **Momentum number roll.** The three stats count up from 0 over 600ms easeOut. An entrance, so 8.4 governs when it fires. This is the animation the rule in 8.4 was generalized from.
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
27. **Transition mark reached.** Only when the transition warning is on, 10.18. The faint tick already sitting on the ring track at the five minute position brightens once over 400ms easeOut and holds. No color change, no pulse, no repeat, and nothing moves except the tick. **Built, phase 4.** The haptic that fires at the same moment is in section 9 and is not built; this animation does not depend on it.
28. **Session extended.** The ring's remaining arc grows to its new length over 500ms springGentle rather than jumping, and the numeral rolls to its new value over the same interval. Nothing else acknowledges the tap. **Built, phase 4.** Every other second is a snap rather than an animation, because item 7 puts the depletion at 1Hz from one ticker and animating between ticks would turn the countdown into a per frame animation.

### 8.3 Reduce motion

When the animator duration scale is 0 or the accessibility setting is on, every animation becomes a 150ms crossfade. The breathing glow holds at 0.92. The ribbon appears complete. Swipe still tracks the finger but the commit is instant. The timer still updates. **One global check, not one check per entry in 8.2.**

### 8.4 Entrance animations fire once per session

**The rule is stated here once and applies to every entrance in 8.2. It is deliberately not repeated next to the individual animations.**

**Items 4, 11, 12, 13 and 14 are entrances.** An entrance is a way of saying "this is new". A screen opened twenty times a day is not new, and an entrance that fires every time is not delight, it is a toll: it delays the content by its own duration, every time, for the reader least able to afford the wait.

An entrance fires on the first open of its tab per app session and not again. Returning to a tab renders the list already settled, at rest, with no fade and no offset. Item 14, the Momentum number roll, carried this rule as a special case from v3 and is the precedent the general rule was written from rather than an exception to it.

**Keyed to the tab, not to the screen.** A sheet opening over Areas is not a first open, and neither is a detail view closing. The flag lives in the per-tab saveable state the shell already holds for scroll position, 10.15.

An app session begins at process start and ends when the process ends. A configuration change, a rotation or a theme switch, is not a new session and does not re-arm anything. A process death is, and the entrance fires again on the next launch. The alternative considered was re-arming after some period in the background. It is rejected: it invents a threshold no one asked for, it makes one screen behave two different ways for a reason the user cannot see, and predictable interface behavior is worth more to this audience than a second chance to show an animation.

**One exception, and it is content, not time.** The Report reveal, item 12, fires again whenever the report being shown changes, because a different report is different content and the ribbon draw is the one animation this design is willing to spend on. Re-reading the same report does not re-animate it.

**Two things are not entrances and this rule does not reach them.** Item 24, the tab content crossfade, is a transition: it fires on every tab switch, all day, because it is how the app says the content underneath has been replaced. And item 25, the empty state entrance, is a guard rather than an announcement: its 150ms delay exists to stop a flash during a load that resolves quickly, so it fires whenever an empty state appears, and in calm mode and under reduce motion the delay is kept while the fade shortens to 150ms. Shortening a fade is motion; removing a delay would only reintroduce the flash the delay exists to prevent.

### 8.5 Calm mode

Calm mode, section 16, takes the path 8.3 already defines. One flag is true when the system asks for reduced motion **or** calm mode is on, and every animation reads that one flag. There is no third motion level and no per-animation opt out.

Calm mode goes further than 8.3 in two places: the entrances in 8.4 do not fire at all rather than firing as a crossfade, and the tutorial ring pulse holds, which 8.3 never says. 8.3's own rule for the breathing glow, holding at 0.92, is unchanged. What calm mode adds beyond motion is color and atmosphere, and that is in 16.2.

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
| transitionWarn | `PRIMITIVE_LOW_TICK` 0.35, once | five minutes remain in a session, **only when the transition warning is on**, which is off by default. **Still pending after phase 4**, and it is the one row in this table with no implementation: `ClarityHaptics` carries the other sixteen events and not this one. Everything visible about the moment is built, 10.18, and the notifications layer publishes the moment as an in app signal with nothing collecting it |
| reportReady | `PRIMITIVE_SPIN` 0.4, or `QUICK_RISE` if unsupported | generation finishes |
| planAccepted | `PRIMITIVE_TICK` 0.5 | deliberately the same weight as an ordinary tap, because accepting is not an achievement |
| warn | `PRIMITIVE_THUD` 0.7 | destructive confirmation arms |
| reject | `PRIMITIVE_LOW_TICK` 0.3, once | an action that cannot be performed was attempted, such as a swipe on a disabled row. Deliberately quieter than a normal tap, because a rejection should feel like nothing happening rather than like being told off |
| undo | `PRIMITIVE_TICK` 0.4 | the undo action in a snackbar is tapped |
| step | `PRIMITIVE_TICK` 0.25 | a tutorial step or onboarding beat advances. The lightest event in the system, because it fires several times in a row |

Never on scroll, screen entry, notification arrival, or more than once per user action. The Pulse reminder is silent.

**Focus sessions fire nothing between start and end, with one exception the user has to switch on.** v3 stated that rule without an exception, and Addendum 01 4g adds a five minutes left signal that is off by default and lives behind a switch in Settings. The rule becomes: nothing fires during a session unless the user asked for one thing to fire, in which case exactly one event fires, once, at one known moment. A signal a person went looking for is not an interruption. Logged in `DECISIONS.md`.

**Phase 4 built the rule and not the exception.** A running session fires no haptic between start and end, which is the half that matters most and the half a defect would be invisible in. `focusStart` fires as a session begins and `focusEnd` fires at a natural completion while the person is watching the ring, and neither fires on screen entry, so a session restored after a process death arrives silent. **A session ended by a person fires the ordinary `tap` on the End control and nothing on the completion screen after it**, which is a choice this document did not make and `DECISIONS.md` carries as an open question rather than as a settled one.

---

## 10. Components

### 10.1 Top of Areas
Serif title at displayTitle, left aligned, archive and settings icons at inkSecondary on the right. Below, two pill chips: Focus and Pulse, card colored, **soft elevation only, no border**. The Pulse chip carries a 6dp warnAmber dot at its top right when a Pulse is ready and unanswered.

**Both permanent chips are built: Focus in phase 4, Pulse in phase 6**, in that order, and neither was built before the surface behind it existed, because a chip that opens nothing is worse than a chip that is not there. The dot is the one use of `warnAmber` that 3.1 scopes it to, it is taken straight from the token rather than through the calm mode transform per 16.2, and nothing else in this row may grow one; the inbox chip carries a count in its label and never a badge. **The dot is not the signal on its own.** Section 13 requires a second one, and it is the label: the chip reads `Pulse` at rest and `Today's Pulse` when there is one waiting, which is the name `MASTER_BUILD_PROMPT.md` 13.5 already gives this destination for the shortcut that opens it. The rejected options were `Pulse ready` and a count, both of which report on the person rather than name the destination. Neither the dot nor the label says anything about not having answered, and there is no content description on the dot for the same reason. Logged in `DECISIONS.md`.

**A third chip, reading `Inbox 4`, joins them when the unfiled inbox is not empty**, 10.16, and is absent when it is empty. It sits last so it can never displace the two permanent chips, and it carries no dot and no color. Built in phase 3b.

### 10.2 Weekly banner
Full width, parchment, 14dp radius, no border, no progress track. A bodyStrong sentence and a caption line, both from the Logic Engine.

### 10.3 The area card

**A2 slim card, locked.** Card at the `card` token, 18dp radius, soft elevation, **no border**, 18 by 17dp padding, 11dp gaps between cards, the wash pooled toward the hashed corner. Five areas fit comfortably on screen. The card names a token rather than a hex value: this line read `#FFFFFF` and was the third statement in the pure white contradiction 3.1 resolves, and a component section repeating a color out of the token table is how that contradiction survived two phases.

**Content.**
- Row one: a 7dp color dot and the area name at label size in the area color
- Row two: the active item title at itemTitle in inkPrimary. **This never shrinks.** It is the most important string on the screen
- Row three: the item's **first step**, 10.17, when it has one, at caption in inkSecondary, one line, ellipsized. Absent entirely when there is none. Never a placeholder and never an invitation to add one, because a card is not a form. Built in phase 3b
- Row four: the status line, shown **only when it carries information**. Idle areas show `Last active 21 days ago`. In-session areas show the live countdown. An ordinary active area shows nothing

The card never exceeds four lines. When a first step and a status line are both present, the first step truncates first, because the status line is about now and the first step is about what to do next, and now wins on a card that has to pass the three second test.

**Idle state.** Title reads `Add your first item` at **inkSecondary** weight 500. No wash. The dot drops to 45 percent. The status line beneath it, `Last active 21 days ago`, is inkSecondary at caption.

**inkSecondary is a correction, and this section is where it is recorded.** v3 said inkTertiary here, and inkTertiary measures **2.40:1** on the card in light and 3.22:1 in dark, against section 13's floor of 4.5:1. That is a contradiction inside this document between 10.3 and 13, on the same string this section calls the most important one on the screen, and **13 wins, because a floor is a floor.** inkSecondary is the one step down from inkPrimary that clears it, at 5.29:1 in light and 6.36:1 in dark on the phase 3c card, and the idle state loses nothing by it: the weight drop from 650 to 500 is what says "waiting to be filled", and it says it whether or not the color is also below the threshold at which a person can read the invitation they are being given. The status line moved with it and was the worse of the two, being 12sp rather than 21.

**In-session state.** The wash doubles from 6 to 13 percent and the status line becomes `In focus, 7 minutes left` in the area color at semibold with a small play glyph. **There is no colored bar, stripe or edge treatment anywhere in this app.**

### 10.3.1 Swipe gestures

Three actions across two directions. **Delete is never reachable by a full swipe.**

| gesture | behavior |
|---|---|
| swipe right past **25 percent** | reveals **Complete**, positiveGreen at 18 percent background, check icon and label in `positiveInk`, 66dp action width |
| swipe right past **55 percent** | commits Complete. One haptic tick at the threshold |
| swipe left past **25 percent** | reveals **Swap** then **Delete**, side by side, 66dp each. Swap on actionBlue at 12 percent, its glyph and label in actionBlue; Delete on deleteMuted at 13 percent, its glyph and label in deleteMuted |
| swipe left past **55 percent** | commits **Swap** only |
| tap Delete on the revealed row | commits delete, with the 5 second undo snackbar |

**Rationale.** Destructive actions must not be committed by momentum. A full left swipe commits the safe action, and delete requires deliberate contact with a specific target.

**Every face carries its own token as ink, and the phase 13 audit is why two of those lines changed.** A face is its action's color at 12 to 18 percent over the page, deepening by 40 percent past the commit threshold, and the glyph and the 10.5sp label on it are that same color at full strength, so **a swipe face is the one ground in the app where a token has to be legible on its own tint**. Measured, it was the weakest reading in the app: Swap at 1.03:1, Complete at 3.40 and Delete at 2.93 in dark, against section 13's floor of 4.5. Complete's ink was written here as the literal `#15803D`, and a hex in a component section is a value nothing re-measures when a ground moves, which is the same defect 10.3 records against the card's `#FFFFFF`; it is `positiveInk` in 3.1 now and reads 4.91. Delete's token became one value per world, 3.1 and 3.2, and reads 4.93 and 4.88. **Swap gave up the area color**, which failed on 43 of the 48 in light: 3.4 permits an area accent in four forms and an action label is not one of them, and the reasoning is recorded there. Its glyph and label take `actionBlue`, which reads 4.95 and 5.74 and is the color its own face is tinted from.

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
A 61dp tall pill inset 17dp from the edges and bottom, at the `raise` rank of 3.1's ladder, elevation only. Four items, each a 48dp target. The active item sits in an inner pill with actionBlue at 10 percent, filled icon and label in actionBlue. Inactive at inkSecondary. The inner pill slides with springStandard.

**An inactive tab is its glyph alone, and one label of four is the rule rather than an accident.** v3 was silent here, so phase 2 took the platform default, which is the move 15.3 names by itself: "adopting a Material 3 Expressive default because it is the default". Section 15 requires the choice to be made and recorded, and it is made the same way it was found: by measurement.

**This bar is the only element in the app with a width it cannot grow out of.** Icons do not scale with the system font scale and labels do, and a 360dp phone leaves this bar 314dp between its 17dp insets and its own padding. At 200 percent text, which section 13 requires without clipping, one label and four icons come to roughly 290dp of that and four labels come to roughly 570dp, which is nearly twice the room there is. Labels on every tab therefore either clip a destination name, which is exactly the failure the rule would exist to prevent, or make the one piece of chrome present on every screen grow past 90dp and take four screens' content padding with it. Neither is a trade this audience benefits from.

**10.15's "no hidden navigation" is satisfied and is a different claim.** All four destinations are on screen at all times, in a fixed order, at a full touch target, with nothing behind a drawer or a gesture. Every one of them announces its name to TalkBack whether or not it is drawn, so the label is never absent for the reader least able to guess at a glyph.

**The rule that makes an unlabeled state legitimate is a constraint on section 7, not on this section: a destination whose glyph cannot be recognized on its own does not get an unlabeled state.** Where that fails, the glyph is what changes. It fails today for Momentum's `arrow_outward`, which is an arrow and not a subject, and that is recorded against 7.2's mapping table rather than patched here, because a replacement is a new drawable and not a token.

### 10.5 FAB
48dp circle in actionBlue with the add glyph in `card`, above the tab bar at the trailing edge. The glyph was white and measured 2.63:1 on the dark `actionBlue`, section 13; a filled action surface inverts its label to `card` in both worlds, 10.7, which is the inversion 10.8 already used for the destructive button.

### 10.6 Sheets
All secondary flows are bottom sheets over a 42 percent scrim. 28dp top radius, handle, springGentle entrance. **No cards inside sheets.** Structure comes from sideheads.

**One exception, and only one.** The color picker's live preview in 10.9 renders an actual miniature area card, because the entire purpose of that element is to show the user what their card will look like. It is a rendering of a component, not a container wrapping content, and the distinction is what the rule is protecting against. No other sheet may contain a card, and the exception may not be extended by analogy.

### 10.7 Buttons
- **Primary:** actionBlue fill, `card` bodyStrong, 50dp, 12dp radius, no border
- **Positive:** positiveGreen at 13 percent, `positiveInk` label, no border
- **Secondary:** ink at 5 percent fill, inkPrimary label, no border
- **Tertiary:** text only in actionBlue
- **Destructive:** inert grey until its condition is met, then ink filled. Never red
- **Contemplative primary:** the surface accent at 14 percent with a bright label, or a translucent white pill at 9 percent

**Two of those label colors were white and green and the phase 13 audit moved both.** White on the primary's fill measured 3.81:1 in light and 2.63 in dark, and no blue is both light enough to be read on the dark ladder and dark enough to hold white, so the label inverts to `card` in both worlds rather than the light world keeping a word the dark world cannot use. A `positiveGreen` label on its own 13 percent `positiveGreen` fill measured 1.68:1: the fill has to stay a light mint for what sits on it and a label on it has to be dark, which is the split 3.1 records as two tokens.

### 10.8 Chips and filters
Pill chips in a horizontally scrolling row. `All` is a solid ink pill with inverted text when selected. Area chips carry their color dot. Unselected chips are card colored with soft elevation, no border.

### 10.9 The area color picker, two stages

**Stage one, the mood strip.** A horizontally scrolling row of eight mood pills. Each pill is a 46dp wide, 26dp tall band at 8dp radius, divided into six equal vertical slivers showing that mood's six colors. **Discrete slivers, never a gradient.** The mood name sits beneath at 10sp in inkSecondary, becoming inkPrimary at weight 700 when selected. The selected pill gains a 2dp ring in inkPrimary at a 1.5dp offset.

**The mood name said inkTertiary until the phase 13 contrast audit, and this is the third time this document has contradicted 3.1 on the same point.** 10.3 was the first and 10.19 the second, and it resolves the way both of those did: 3.1 says inkTertiary carries no text anywhere in this app, 13 states one floor of 4.5:1, and the name measured **2.40:1** on the sheet it is drawn on. **13 wins, because a floor is a floor.** Nothing is lost, because the weight step from 400 to 700 in the sentence above is what says which mood is selected, and it says it whether or not the other seven names are also below the line at which a person can read them. The same audit corrected the live preview card in this section's stage two, where the idle line now takes 10.3's own answer, weight 500 in inkSecondary, rather than a second one: the preview is an actual miniature area card and had been the one place where color alone told the idle state from a filled one.

**Stage two, the swatches.** The six colors of the selected mood in a three by two grid, each a square at 16dp radius with 12dp gaps. The selected swatch scales to 1.06, gains a 2.5dp ring in the swatch color at 50 percent offset by 3dp, and shows a check at 20dp in whichever of white or ink reads on that swatch. **A white check fails on 17 of the 48**, worst at 1.67:1 against section 13's floor of 3.0 for a graphic: a swatch is the accent at full strength, the only place in the app where an area color is a large solid field, so half the palette is too light to hold white and the other half too dark to hold ink. The worst of the 48 then measures 4.23:1. A drop shadow or an outline on the check is refused by 6.1, which gives an element one separation device, and choosing the ink per color is the mechanism 3.4 already uses for the area label. Nobody sees the two inks at once: this grid has one selected swatch.

**Above both, the live preview.** An actual miniature area card in the currently selected color, updating instantly on every tap, including the wash. This is the premium moment of the app and it is the one place worth spending animation budget on a crossfade.

Switching mood re-renders stage two with a 40ms staggered entrance. The previously selected color is retained if it belongs to the newly selected mood, otherwise nothing is selected until the user taps.

### 10.10 The appearance picker

Three tiles, each a **real miniature** of the Areas screen rather than grey bars: the correct canvas color as the tile background, a title bar at the correct ink opacity, and three miniature area rows at the correct card color, each carrying a real 4dp area dot in Ocean, Meadow and Earth.

- **Light** renders `#F1F1F6` canvas with `#FFFFFF` rows
- **Dark** renders `#0E0E13` canvas with `#191921` rows
- **System** splits diagonally at 103 degrees, showing both halves including the rows

Tile height 84dp, 12dp radius, 9dp gaps. The selected tile carries a 2dp actionBlue ring and a 14dp filled actionBlue check badge at its top right. Labels sit beneath at 9.5sp, becoming actionBlue at weight 700 when selected.

Below the row, one caption line: `Focus, Pulse and Report are always dark by design.`

**Built, phase 11**, as `ui/settings/AppearancePicker.kt`. Every dimension above is on the screen: 84dp tall, 12dp radius, 9dp gaps, the 2dp `actionBlue` ring, the 14dp filled check badge, the labels at 9.5sp going `actionBlue` at weight 700 when selected, and the System tile clipped at 103 degrees with both halves drawing their own rows.

**The tiles read the tokens rather than the four hex values written above, and that is deliberate.** Three of those four moved in phase 3c, and the light card in particular moved because sections 1 and 14 both forbid a pure white background while the old 3.1 specified one. A miniature drawn from `#F1F1F6` on `#FFFFFF` today would be a picture of a screen this app no longer has, which defeats the one thing the word **real** in this section is doing. The tiles resolve `ClarityLightColors` and `ClarityDarkColors` instead, so a later token change reaches them with nobody remembering the file. The three dots come from the mood groups in 3.4 for the same reason, and they take no calm mode transform, because 16.2 excludes the area dot by name and a miniature of a dot is a picture of the same thing.

**The angle is read as it is written on paper**, counterclockwise from the positive x axis, so 103 degrees leans thirteen degrees off vertical. Screen coordinates grow downward and the implementation negates the y component at the point of drawing rather than in the caller, because an angle in a design document is not a quantity in a canvas.

### 10.11 Settings rows
**No card containers.** Rows sit directly on the canvas separated by hairlines, grouped under sideheads. Each row carries a 26dp rounded-square icon badge tinted at 11 to 14 percent of a per-group color, a title at 15sp semibold, a trailing value at caption inkSecondary, an optional caption under the title in the same ink, and a chevron where it navigates, also in inkSecondary.

**All three said inkTertiary until the phase 13 contrast audit, and this is the fourth time after 10.3, 10.19 and 10.9.** These rows sit directly on the canvas, where inkTertiary measures **2.34:1** against 13's floor of 4.5 for text and 3.0 for a graphic that carries meaning, and the trailing value is the current state of the setting, which is the one thing on the row somebody came to read. A chevron that says a row navigates is not an ornament either. What keeps all three a rank under the title is 5.3's caption role against 15sp semibold and the trailing position, which is 6.1's first separation device rather than a second one.

**Built, phase 11**, as `ui/settings/SettingsComponents.kt`. The tint is 12.5 percent, the midpoint of the range, so that neither end of it is a rounding accident. The hairline is the row's **one** separation device per 6.1, which is also the reason the group is not a card: a card would be a second one, and the last row in a group is passed its divider explicitly rather than deriving it, because a hairline under the last row is a rule under the group instead of between two rows.

**The per group colors are none of the four function colors.** 3.1 scopes `actionBlue`, `positiveGreen`, `warnAmber` and `deleteMuted` to one job each, and a settings badge in any of them is the second meaning that scoping exists to prevent. They come from the mood groups in 3.4 instead, one hue apart, and they are never the color of a real area on a real card, because an area accent reaches the screen only through the four forms 3.4 permits and a 26dp badge is none of them. What is shared is a hex value, not a meaning.

**The glyph is `inkSecondary` rather than the group color, which is the deliberate choice under section 15.** The statistically common settings screen of 2026 puts a saturated glyph on a tinted square of the same hue, which turns a column of rows into a column of badges and makes color the first thing read on a screen whose whole content is words. Holding the glyph in ink leaves the tint as a quiet grouping signal under the icon that carries the meaning, which is also what section 13 asks: color is never the only signal, and here it is not a signal at all.

**The whole row is the target, not the control on it.** A toggle row hands its switch a null callback so the switch can never take a tap the row was going to handle, which is what makes the target the full width rather than a control at the trailing edge.

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

**The floating tab bar belongs to the four views and is not drawn over anything else. Settled, phase 12c, issue #58.** Through 0.9.0 the bar kept floating over Settings with `Areas` shown as selected, which was accurate, and tapping that selected tab did nothing at all. Back worked, so section 17's requirement that every screen can be left was met, and what was left was a control that looks live and is inert, which is the thing 10.16 objects to elsewhere in this app.

**The statistically common answer is that tapping the selected tab pops to that tab's root**, which is what every application with a bottom bar does, and section 15 makes that a reason to look at it twice rather than a reason to take it. It is refused. It gives one control two meanings that differ by state a person cannot see: `Areas` means "go to Areas" from three tabs and "leave Settings" from Settings. It is also undiscoverable, so the bar goes on looking live to everybody who does not already know the convention. This audience pays for both.

Not drawing the bar serves the brief better on three counts. The inert control is removed rather than given a second job. The app already applies this rule to the Focus surface, which covers the bar because it is not one of the four views, so a pushed screen doing the same is the app being consistent with itself rather than with the platform. And it deletes a state instead of adding one: with the bar gone there is no way to leave a pushed screen sideways, so there is no returning to the Areas tab and finding Settings still open where it was left. What it costs is the sideways move, one tap from a pushed screen to another tab, and back is one press and lands on the tab the bar would have. It reaches Settings, About and the Report's history page; a bottom sheet is unaffected, because a sheet is a window of its own over a scrim. `ui/nav/PushedScreens.kt` is the mechanism, one line at each pushed screen, and `PushedScreensTest` asserts the bar has one call site and that it is inside the guard.

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
| Unfiled inbox | the Inbox chip in the Areas header, 10.16 | drag down, scrim tap, or back |
| Area chooser for filing | Move to an area, on an inbox row | same, or after filing |
| Re-entry state | opening the app after 14 or more days away, 11.2 | choosing one of its two options. There is no back |

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

**Zero areas.** Reachable by archiving or deleting everything. The Areas screen shows an empty state reading `No areas yet. An area is a part of your life you want to keep track of.` and **the FAB creates an area rather than an item** while this state holds. The tab bar stays visible and the other three tabs show their own empty states. The unfiled inbox is unaffected by this state and may still hold items, 10.16.

**A queued item tapped.** Opens an edit sheet with title, note, a Delete row, and a Move to front action. This is the only way to edit a queued item and it must exist, since the queue is otherwise read-only.

### First launch

Cold start reads two flags in order. `hasCompletedOnboarding` false routes to onboarding. Otherwise, `hasSeenTutorial` false routes to Areas with the tutorial queued to start once the first frame has settled. Otherwise, Areas. **Onboarding beat 3 writes the selected areas as real events**, so a user who force-quits after beat 3 and relaunches lands on a populated Areas screen rather than starting over.

**Built, phase 10**, as `ui/onboarding/FirstRunGate.kt`, and three things about how are worth carrying forward.

**The two flags are read once and the answer is latched for the process.** That is what makes the force quit rule above work rather than a decoration on top of it: beat 3 writes `hasCompletedOnboarding` while onboarding is still showing, and a route that recomputed itself from the flows would swap onboarding out for the app in the middle of the reveal. It also means `Replay the welcome`, `Replay the tour` and the return to onboarding after an erase all take effect on the next cold start.

**"Once the first frame has settled" is implemented as a fact rather than as a delay.** The tutorial starts when every one of its five targets has reported a rectangle, which is the thing a delay was standing in for, and a delay guesses differently on a cold start, on a slow device and on a phone with a long area list. Four of the five targets are on the Areas screen, so readiness cannot be true before Areas has laid out. It also handles the zero areas state above with no special case: there is no card to point at, so the tutorial waits until there is one.

**A third check joins them, after both flags. Built, phase 12c**, where phase 10 left the place marked, after both branches and never inside them. When onboarding and the tutorial are both behind the user and the gap since the last recorded open is 14 days or more, cold start routes to the re-entry state in 11.2 instead of to Areas. It runs once, on that open, and the ordinary route resumes afterward. It is checked last so that it can never delay or replace a first run.

**"Instead of to Areas" is a screen that covers Areas rather than a branch that replaces it**, and the difference is deliberate. The two flags are read from DataStore in a frame or two; this one has to ask the log, which is the same load every other reader of a cold start is already waiting for. A gate that held the first frame until the log answered would put a blank screen in front of every launch this app has, for a question that is false on all but one day in a person's whole use of it. So the app arrives when it always did and the re-entry state covers it opaquely when the answer comes back, on item 25's entrance, whose 150ms delay is exactly the guard that stops that reading as a flash. **The tutorial is held while the answer is outstanding**, which costs nothing and closes the one race that would break 14b.4's "it is first and it is alone": the tutorial waits for five targets to report and four of them are on the Areas screen.

### 10.16 The unfiled inbox

**Built in phase 3b.** Addendum 01 4a. Capture must never require a decision. Adding an item does not require choosing an area, and an item with no area is an ordinary item in an unfinished state, not an error and not a problem.

**Where it lives.** The third chip in the Areas header, 10.1, reading `Inbox 4`. The count is the label. It is present only while the inbox holds something.

The obvious answer is a pinned row at the top of the area list carrying a count badge, which is what every inbox in every app does. It is rejected twice over. A badge is forbidden by 14 and by the addendum's own wording. And a pinned row puts the pile of things the user has not dealt with above the one thing they opened the app to see, every single time, which is the exact feeling this app exists to remove. Section 15.

When the inbox is empty there is no entry point, and none is needed: there is nothing in it, and the next unfiled capture brings the chip back.

**The inbox sheet.** Plain rows on the sheet surface, no cards, 10.6, each carrying its title and its first step when it has one, with the oldest last. Each row offers `Move to an area`, which opens an area chooser, plus edit and delete.

**What an unfiled item cannot do.** It cannot be active and cannot be completed until it is filed. Complete, Swap and Focus are **absent** from an unfiled row rather than present and disabled, because a disabled control is a question the user then has to answer.

**Filing.** One tap, one choice, never demanded and never scheduled. There is no review prompt, no weekly filing reminder and no sorting ceremony. An inbox that grows is not a finding: no surface in this document reports its size except the header chip and the Quick Capture widget, 12.2, and no observation is ever authored about it.

**The zero areas case.** With zero areas, 10.15, the inbox can still hold items. The chip still appears, the sheet still opens, and `Move to an area` offers to create one first. The FAB still creates an area while that state holds.

### 10.17 First step and estimate

**Built in phase 3b.** Addendum 01 4b and 4c. Two optional fields on an item, in the add sheet and the edit sheet, blank by default, deletable, never required and never prompted for.

**First step.** One line: the first physical action. The label is `First step` and the placeholder is an example rather than an instruction, because an instruction to break a task down is a second task. It appears on the area card beneath the title, 10.3, in full in the detail sheet, on the First Step widget, 12.2, and nowhere else.

The field earns its place at the moment the app is hardest to use. The title of a task is often the intimidating part of it: `Rewrite the proposal intro` is a wall, and `Open the doc and read what is there` is not.

**Estimate.** Optional minutes, entered as a number. The card does not show it at all. Only the detail sheet does, as plain text. It is never rendered as a countdown against the item, never as a bar filling toward it, and never as a target.

**The one hard rule this document owns:** no surface may draw the difference between an estimate and an actual. Not as a number, not as a bar, not as two marks on one track, not as a color change, not as an arrow. Addendum 01 7a states the same rule for language and puts a veto test behind it. This is the visual half, and it exists because a shape can accuse just as plainly as a sentence.

### 10.18 The focus session controls

**Built, phase 4, with one exception named at the foot of this section.** Addendum 01 4f and 4g.

**Add 10 minutes.** A tertiary control, 10.7, text only in the Focus accent, sitting beneath the End session pill. It extends the running session: it never restarts it, never returns the arc to full and never begins a second session. The arc grows to its new length, 8.2 item 28. No confirmation, no toast, no acknowledgment beyond the ring itself. It may be tapped again, and there is no limit on how many times, because a limit is an argument with someone who is working.

**The transition warning.** Off by default. One switch in Settings under Focus. When it is on, the ring track carries a faint tick at the five minute position **from the moment the session starts**, so the warning is a landmark the user has already seen rather than an event that arrives. When the arc reaches it the tick brightens once, 8.2 item 27, one haptic fires, section 9, and the word `remaining` beneath the numeral becomes `5 minutes left` and stays. The mark is white and 12dp long by 2dp wide, sitting across the track, at 20 percent when it is ahead and 55 percent once it has been reached. A session five minutes long or shorter carries no mark at all, which is this rule expressed as a value the ring cannot draw rather than as a condition each surface has to remember.

The obvious answer is to turn the ring amber or red at five minutes. It is rejected three times over: warnAmber is scoped by 3.1 to the Pulse ready dot and nothing else, section 14 forbids red warning states for normal behavior, and an unannounced color change is precisely the surprise that this switch exists to prevent. A mark that has been sitting there since the start costs nothing and reads as information rather than as an alarm. Section 15.

**Backgrounded.** With the app in the background the same moment is carried by the Live Update, 11.4, which shows the same mark on its track. A full notification is posted only when the app is backgrounded and no Live Update is available.

**The exception, stated rather than left to be discovered.** Every visible part of this section is built: the control, the mark, the tick brightening, the word changing, the point on the Live Update track, the silent notification, and an extension re-arming the moment exactly once. **The haptic is not**, because `ClarityHaptics` does not carry a `transitionWarn` event yet, and the signal the notifications layer publishes for the in app case has nothing collecting it. The switch that turns any of this on is a Settings row and Settings is phase 11, so on the built app the whole feature is off and reachable only by writing the preference by hand.

### 10.19 Text fields

**Appended in phase 12b, because section 10 had no field entry at all.** It covered buttons at 10.7, chips at 10.8, settings rows at 10.11 and sideheads at 10.12 and never a field, so phase 2 filled the silence with four stacked underlined fields carrying a hairline that warmed to actionBlue on focus. That is the most common form treatment in existence, which section 15 makes a reason on its own, and it also reached past the rule that should have decided it: 6.1 puts a hairline **fourth** among separation devices, "only if all three above have genuinely failed", and those four rules were the third and fourth hairlines in the whole app.

**A field is a sidehead and a well.**

- The **label** is a sidehead, 10.12, in inkSecondary, 6dp above the well. A label over a filled shape needs less air than a label over a line, because the shape's own edge already says where the field begins
- The **well** is the rank below the surface the field sits on, which for every field in this app means `raise` on a `card` sheet. Twelve dp radius, the `button` radius in section 6. Fourteen dp of horizontal padding, which is that radius plus two so the first glyph clears the corner rather than sitting inside its curve, and 12dp vertical. Minimum height 48dp, section 13
- **No rule, no border, no box.** The well is the one separation device and 6.1 permits exactly one
- **Focus takes the well one rank further down**, to `canvas`, on the crossfade spec. The caret is the second signal, in actionBlue, which 16.2 excludes from the calm mode transform because it is function rather than atmosphere
- The **placeholder** is inkSecondary, and only where 10.17 asks for one. It is an example, never an instruction
- **Text is `body`**, 5.3, in inkPrimary

**Why down and not up.** A field is a place content is put into, so it reads as a recess rather than as a tile floating on the sheet. 3.1 defines `raise` as "the 3 percent lightness step used *instead of* a border", which is the sentence this whole component rests on. A field placed directly on `canvas` would be a step **up** and would read as a raised tile; the fix then is the ground under it and not the value here, and there is no such call site.

**The placeholder's color is a floor rather than a preference.** It was inkTertiary, which measures 2.40:1 on a light card against section 13's 4.5:1. This is the same contradiction 10.3 resolved for `Add your first item` and it is resolved the same way. What says "not filled in" is that the text vanishes on the first keystroke, not that it was too faint to read.

**The obvious focus signal is a two dp accent rule or an accent border.** Both are a second separation device on an element that already carries one, and both are the treatment every form on every phone already has. Depth speaks louder instead of a new device speaking at all. Section 15.

---

## 11. Surface Art Direction

**`textFaint` named a text color in five places in this section and it names none now.** 3.3 puts the token at 32 percent and 13 says Contemplative text stays at or above 55 percent opacity where it is meant to be read, so the two statements could not both stand. At 32 percent it measures **2.64:1** on `deepBlack`, 2.55 at the center of the Focus gradient and 2.71 on `surfaceRaised`, against 13's floor of 4.5 for text and 3.0 for a graphic that carries meaning. **13 wins, because a floor is a floor**, which is the same resolution 10.3, 10.9, 10.11 and 10.19 each reached against 3.1's `inkTertiary` in the Daylight world. The word `remaining` below, and the Report's controls, day initials, decline and footer in 11.1, all take `textDim`, which measures 5.70 on the same ground. `textFaint` keeps the job 3.3 gives it, an opacity for shapes that are not read, and the phase 13 audit holds it there with a source scan rather than with a number, because a floor cannot notice a screen that quietly starts drawing a token. **What ranks these elements is 5.3's scale, their weight and the space around them**, which is what ranked them all along; the color was saying it a second time and below the line at which it could be read.

**Areas.** The Daylight home. Five areas fill the screen comfortably. Must pass a three second test: what is active everywhere, at a glance.

**Focus. Built, phase 4.** The indigo night. **Six elements only:** area label with dot, item title in bold sans at 26sp, the 240dp ring with the timer numeral, the word `remaining` in textDim, the End session pill, and `Add 10 minutes` beneath it as a tertiary control, 10.18. Nothing else, ever.

**The ring's own numbers, which v3 left open and phase 4 had to choose.** The diameter is 240dp and was always stated; the stroke is **6dp** and the tip is a **10dp** filled circle with the 15dp falloff in 3.3. The obvious answer is a heavy ring, twelve to sixteen dp, which is what an activity ring looks like and what 15.1 warns about under a ring closing toward a daily target. This one is deliberately thin with the weight spent on the tip instead: a fine line of light with a bright point traveling it reads as time passing rather than as a target filling. Section 15, and logged in `DECISIONS.md`.

**The sixth element is a change, recorded rather than assumed.** v3 said five and said nothing else, ever. Addendum 01 4f requires a control that extends a running session, and there is no honest way to have it and keep the count at five: putting it behind a long press or a gesture would hide the one control whose whole purpose is to keep someone in flow, from the audience least likely to go looking for it. The count moves to six. The hierarchy does not: the new control is text only, subordinate, below the End pill, and the surface still reads ring first. **Built, phase 4.** Logged in `DECISIONS.md`.

**Focus complete. Built, phase 4.** Ring replaced by a circle bloom and check. `Session complete` in serif, item title, a small line reading duration and area, then `Mark item complete` in the accent and `Done` beneath. There is no dot and no color on this screen: the area is already in the words, and a 7dp dot here would be an unrequested embellishment on the quietest screen in the app. `Mark item complete` is absent, rather than present and inert, when the item was completed, swapped or deleted while the session ran, and `Done` is still there and still leaves.

**A session ended early reaches this same screen, in the same words.** Addendum 01 4e. The serif line reads `Session complete`, the duration line reads the real duration, and there is no qualifier, no shortfall, no comparison against what was planned and no second, quieter version of the screen for a shorter session. Fourteen minutes is fourteen minutes. The word this rule exists to keep off the screen is named in 13.1. **Built, phase 4.**

**The screen is not told which kind of ending it is drawing**, and that absence is how the rule is kept rather than remembered. The value it is handed carries the duration, the area and the item and carries no field recording whether the planned time ran out, so there is no fact on this surface that a later edit could teach it to render. A session ended under sixty seconds is the one case that does not reach this screen at all, per section 10, because forty seconds is a mis-tap rather than a short session.

**Pulse. Built, phase 6.** The amber night. The observation in readSerif centered, the question in body at textDim, then response pills. After answering, an acknowledgment fades in, then ambient mode: a 14 day rhythm row, today's answered card, and a History entry. Filled amber means answered, a hollow ring means generated but unanswered, faint means a silent day.

**The four numbers v3 left open here, and what phase 6 chose.** The room is 520dp, collapsing to no less than 320dp, with both phases scrolling inside whatever height they are given. The obvious answer is a sheet that wraps its content, about 300dp for the question and about 250dp for ambient mode, which makes the amber night a panel rather than a room and resizes it under the settle in 8.2 item 11. **The response pills are stacked and never set side by side**, identical in width and treatment, because a left position reads as a recommendation and because side by side does not survive the three options of `quietDay` without the layout becoming a signal about which family fired. **The dawn and evening tints reach 45 percent of the height at 55 percent and stop**, since 3.3 asks for a shift felt rather than noticed and a gradient across the whole surface is what a hero background looks like. **The acknowledgment is held for 1,100ms** before the room settles, and that hold does not shorten under reduce motion, for the reason 8.4 keeps the empty state's delay: a hold is not motion. Section 15, and all four logged in `DECISIONS.md`.

**The silent mark in the rhythm row is faint with a floor.** At half strength it measures 3.0 to one against `deepBlack`, which is the ratio 16.7 holds a graphic to, and it is drawn smaller as well as fainter so the three states differ in form and not only in opacity, per 13. Below that floor a mark stops being quiet and starts being absent, and a fortnight of silence would read as a broken row rather than a calm one. **Today carries no ring here.** That is Momentum's treatment and importing it would add a fourth state to a row whose safety property is that it has exactly three. **Built, phase 6.**

### 11.1 The Report

**Built, phase 8.** Every one of the nine items below is on the screen. The closing line's block, item 8, is built and always empty, because a closing line is layer six and layer six is phase 9b; the accept pill, the decline and the settle in 8.2 item 26 are written and waiting for a plan to draw.

**Four treatments and no more than four**, because this is read 52 times a year and anything clever becomes exhausting by the tenth reading.

1. **Controls** in textDim, top right. History, regenerate, copy. A control's glyph, so 13's 3.0 floor is the least it clears.
2. **Eyebrow.** `Clarity Report · Week of July 13` in caption at textDim, centered. One line.
3. **The headline** in displayHero, centered, generous space above and below.
4. **The week ribbon.** The glance layer. Seven vertical marks, one per day, 5dp wide at 2dp radius, height and opacity scaled to that day's activity against the week's busiest. Gold. Day initials beneath at 8sp textDim. Below it one caption line reading the three headline numbers.
   **Constraints.** No axes, no gridlines, no values on the marks, no card around it, no gradient. The only non-text element in the entire report. It repeats at 60 percent scale in the past reports list and nowhere else.
5. **A gold rule**, full width, fading at both ends.
6. **Sections.** Each a sidehead followed by bodySerif prose, 28dp apart. Sentence-case labels: `Your week, honestly`, `What you said`, `Focus`.
7. **The pattern break.** The `Pattern` section only. Bleeds to full screen width, sits on gold at 4.5 percent, inset 30dp horizontally so its measure is visibly narrower, set in Newsreader at opsz 28 rather than 17, bounded top and bottom by full-bleed gold rules. **The one deliberate grid break.**
8. **The closing line.** 34dp of space above. Centered. A caption eyebrow reading `One thing`. The line in closingLine, **roman, never italic**, at textBright. Beneath it two options: an accept pill in gold at 14 percent reading `I'll do that`, and a decline in text only at textDim reading `Not this week`. Both optional, both costless, neither ever mentioned again. **What makes declining costless is that one option is a pill and one is not**, which is a difference in form; it was a difference in opacity as well until phase 13, and an option nobody can read is hidden rather than costless.
9. **Footer.** `Generated on your device`, then the basis line, both in textDim at caption. A footer is quiet because of where it sits and how small it is, 6.1's first two devices, and it goes on being the quietest thing on the page.

The background gradient extends under the status bar to the very top edge.

**The six numbers this section leaves open, and what phase 8 chose.** All six are logged in `DECISIONS.md` under section 15.

**A day with nothing in it keeps a mark.** The heights run from a floor rather than from nought, and the opacities from half strength rather than from nothing. The obvious answer is proportional height from zero, and it draws an empty Tuesday as no mark at all: a row with a hole in it reads as broken rather than as calm, and it turns the absence of activity into the loudest thing on the page, which is the one thing this app never does with a quiet day. Half strength gold measures 3.0 to one against `deepBlack`, which is the ratio 16.7 holds a graphic to, and it is the same floor the Pulse rhythm row arrived at for the same reason. **The scaling is linear and deliberately not curved**, because a square root would make a quiet day look busier than it was, which is a flattering lie rather than an unobvious answer.

**The marks sit at a fixed gap rather than distributed across the measure**, because this section asks the ribbon to repeat at 60 percent scale and a row stretched to whatever width it is given cannot be scaled, only re-flowed. Seven 5dp marks at that gap make the ribbon 143dp across.

**The caption's three numbers are the week's events, its completions and its additions.** This section asks for the three headline numbers and does not say which three. The first is what the ribbon draws and the second and third are the flow most of the report's families are about, so the caption states what the picture shows and what the prose keeps returning to. The obvious three were completions, focus minutes and a percentage, which is what a weekly summary looks like everywhere else: two of those are about effort rather than about the week, and a percentage in a caption invites a comparison against a target this app deliberately does not have. **A total of nought is absent rather than stated**, so `0 completed` cannot occur.

**The pattern break changes the optical size and leaves the point size alone.** This section says "set in Newsreader at opsz 28 rather than 17", and `bodySerif` in 5.3 is "Newsreader 17, opsz 17", where the two numbers happen to be equal, so the instruction reads either as change the axis or as change both and names only the axis. Changing the axis alone invents no number, which is what `CLAUDE.md` requires of a dimension this document does not state. **Worth an owner's glance on the device**, because it is the one number here that reads two ways.

**The three sideheads map to the observations here rather than on the screen.** This section names `Your week, honestly`, `What you said` and `Focus` and does not say which observation is read under which. Two name themselves: the callback families that quote a stored Pulse answer are `What you said`, and the focus families are `Focus`. Everything else is the week, honestly. The obvious answer was one section holding everything, which is what the corpus's own section 2 looks like on the page, and three sideheads are the only structure a page of prose has for a reader to skim.

**The reveal's four start times.** 8.2 item 12 gives the ribbon's 45ms per day, the sections' 90ms stagger and the 1.4 second ceiling, and leaves the start times open. Eyebrow at 0, headline at 140, ribbon at 380, sections at 780, which puts the last block on the longest page at 1,380ms. **The section stagger is capped at five steps and that is what makes the ceiling hold**: eight blocks at 90ms would put the reveal past 1.7 seconds, so past the fifth the delay stops growing and the remainder arrive together. The obvious answer is to stagger everything, and it breaks the one number this entry states as a limit.

**Two things on this surface are absent on purpose and both are recorded rather than forgotten.** There are **no specks of light** here, although 3.3 gives the Contemplative world eight to fourteen of them and both other Contemplative surfaces take them: this section is more specific than 3.3 about this screen, and a field of specks is a fifth treatment and a second non-text element. And **the two centers of light are fixed to the room rather than following the content**, because the page scrolls and a light anchored to a scrolling headline would make the room itself feel like it had moved, which is the reading 3.3 already made for the Focus surface in the same words.

**The gold rule is one component and the page draws three of them**: the one under the ribbon at the body measure, and the two that bound the pattern at full bleed. The only difference between them is the padding the caller gives it, because 3.3 says without qualification that a rule on this surface is a gradient fading at both ends and never a solid line. The sideheads are drawn here rather than by the shared component in 10.12 for exactly that one reason, and everything else about the two is identical.

**TalkBack hears half of section 13's ribbon example and does not hear the other half.** 13 gives "busiest on Wednesday, quiet at the weekend". The busiest day is the argument of a maximum over a query result and is what the tallest mark already says to somebody who can see it, so it is a direct readout and lives in `strings.xml`. "Quiet at the weekend" is a characterization of somebody's week, and a characterization is an observation, which `CLAUDE.md` rule 8 puts in a corpus through the engine. A content description is not an exception to that.

**Momentum. Built, phase 7.** Daylight, the calmest screen in that world. Headline in readSerif, the 14 dot row, area tiles, three stats as pure typography with no cards, then insight modules under sideheads.

**The four things this section leaves open here, and what phase 7 chose.** **The tiles are a three column grid**, not a horizontally scrolling row, which hides areas off the edge of the one screen whose job is to show all of them at once, and not four columns, which shrinks the tile until 3.4's "one place where an area color gets real presence" stops being true. Five areas, which this section calls a comfortable screenful, then fill two rows with the second one short, which is what a mosaic looks like rather than what a table looks like. **The activity readout sits under the dots rather than over them**, because a label above a graphic makes the graphic an illustration of the label, and because it is the arrangement 11.1 already gives the week ribbon. **The three stats are set in Newsreader with their labels in the sans, left aligned rather than centered**, because a heavy sans figure over a small caps label is a dashboard and 15.1 lists stat banners as a tell, and because 5.2 records that Hanken Grotesk ships no `tnum` and Newsreader does, which matters on the one figures in this app that count up through every value between zero and themselves. **The area name sits under the tile rather than inside it**, because 3.4 requires an area label to be verified at 4.5:1 against the surface it is drawn on and names the two surfaces that were measured, and a 60 percent tile is a third one per color per theme that nobody has measured. Section 15, and all four logged in `DECISIONS.md`.

**The idle tile's faint outline is the `hairline` token and never the area's own color.** 3.4 permits the accent in four forms and ends "never as a stripe, bar, edge, border or filled block", so the outline this section asks for cannot be drawn in the color the tile beside it is filled with. The two states then differ in fill, in edge, and in nothing else, which is one separation device each per 6.1. **Built, phase 7.**

**The idle module's sidehead reads `Quiet areas`.** `MASTER_BUILD_PROMPT.md` 12.2 calls the module Idle Areas and asks for it to be gentle, and that word is the reason for the change: `Idle` is what the state is called in the code and on the area card, and set as a heading over a person's own list it reads as a verdict on them rather than a description of a fortnight. `Quiet` is the word the corpus already uses for the same shape. The module is a label change and nothing else. **Built, phase 7**, and logged in `DECISIONS.md`.

**An inactive dot is smaller as well as fainter**, which is section 13's rule that color is never the only signal, answered the same way the Pulse rhythm row answers it. Nothing on the screen relates one dot to the next: there is no run length in the row, none in the value behind it, and section 14's instruction that a gap is rendered as a lighter dot with nothing said about it anywhere is kept by there being nothing that could say it.

**Trail.** Daylight. **A screen title reading `Trail` in displayTitle, left aligned on the same measure as the day headers**, with no glyph beside it. Day headers as bodyStrong with an inline count and a hairline to the trailing edge. Events as **a glyph and a sentence**, the glyph 18dp in inkSecondary inside a 23dp column, standing on the page and carrying no color of its own. Completed events get a mint wash at positiveGreen 8 percent over `card`, on the same 20dp measure as everything else on the screen. Everything on this screen sits on that one measure: the title, the day headers, the sentences and the mint.

**The circle is gone, and phase 12b settled that as an open choice rather than dropping it.** v3 said "a 23dp circle tinted with the event color" and this document defines no event colors anywhere, so phase 3 resolved the phrase to the area's own accent at 12 percent. Two things were wrong with that and only the second closes the question.

The first is redundancy. On a single area app every circle is the same disc, and the icon column carried nothing the sentence beside it did not.

The second is that **no color could have carried it, and that is a proof rather than a preference.** A per event palette is unavailable by construction: 3.1 scopes `positiveGreen` to completion, `warnAmber` to the Pulse ready dot and `deleteMuted` to the delete swipe, one job each, and 3.4 permits an area accent in four forms only, of which the one a 23dp shape may take is a 5 to 14 percent wash. Eight moods at 12 percent over the canvas are not distinguishable from one another at a glance, so the disc could not carry identity either. A container that can hold no information is a container, and section 14 has a name for a glyph in a tonal circle beside a single sentence: a stock Material list row. So the container went and the glyph grew from 14dp to 18dp to hold the column on its own.

**Two things this buys, and one it gives up.** The mint is now the only colored surface in the list rather than one colored thing in a column of faint discs, so a completion reads at a glance for the first time. And section 14's "no stock Material list rows on primary screens" stops being contradicted by this section, which is the contradiction the design audit found. What it gives up is area color on an unfiltered Trail, and the answer to that is where 3.4 already puts identity: the 7dp dot on the filter chips above, at the true accent, on the control whose whole purpose is scoping the screen to one area.

**The mint's breathing room is vertical, 12dp against an ordinary row's 10dp.** Horizontal room would have to come from either pushing the block back outside the measure, which is where it was, or pushing a completed row's sentence 12dp in from every other row's, which is the same defect with the sign flipped.

**The title is an addition, and it reverses a decision phase 3 took deliberately.** v3 gave the Trail day headers and rows and stopped, and phase 3 read that as intentional and reasoned it through: the tab bar already says Trail, so a heading reading Trail repeats a word the user can see at the bottom of the same screen. That reasoning is sound and it is outweighed. Every other surface in this section opens with a headline treatment, so the Trail was the one screen in this document that began with its content, and the design audit priced what that cost: the built Trail contained no serif glyph at all, and Newsreader had five call sites across the whole app of which four were empty states, which had quietly made the signature typeface mean "there is nothing here". The same absence had a second effect the audit named separately, that with no title the loudest element on the Trail was the selected `All` filter chip, so a filter outranked everything it filtered. One serif line fixes both. The title takes the same treatment as the Areas title in 10.1, because two screens that open the same way are one app.

**The day header outranks its rows by size as well as weight**, which 5.3 only made possible in phase 3c. `body` and `bodyStrong` were both 16sp, so this section's instruction to set the header in one and the rows in the other bought a weight change and nothing else, and four events read as a wall at 12 and 16sp. With `body` at 15 and `bodyStrong` at 17 the screen reads 30, 17, 15, 12.

**Settings. Built, phase 11.** Rows on canvas under sideheads. Order: Daily routine, Focus, After completing, Appearance, Your data, Privacy, Help, then the support block, then the version line.

**The one card on the screen is the permission card**, which `MASTER_BUILD_PROMPT.md` 14.1 asks for by name. It is not a row and it is not a container for rows, so 10.11's no card containers rule survives it.

**One thing is not as 10.15 describes it.** Settings is hosted from inside the Areas tab rather than from `ClarityShell`, so it does not cover the floating tab bar the way a pushed screen should; it reserves room at the foot instead. The shell draws that bar as a sibling above the tab content, so covering it means hosting the surface there, and the remedy is one branch beside the Focus surface. `ui/settings/PushedScreen.kt` and `ui/settings/SettingsSurface.kt` both carry the note, and `docs/BUILD_STATE.md` carries it as owed work.

**The `After completing` choice reuses the app's one selection language rather than inventing a second.** The statistically common segmented control of 2026 is a pill track with a thumb that slides between halves. It loses twice here: a sliding thumb is a movement section 8.2 gives no token for, which calm mode would then have to suppress separately, and it is a second way of saying **this one is chosen** beside `ClarityChip`'s ink fill with an inverted label, which the app already uses on every chip. Section 15.

**About. Built, phase 11.** App mark at 62dp, name in displayTitle, version and `by Kamsiob`, one paragraph in bodySerif, a quiet link list under an `Elsewhere` sidehead, then the support block, then license lines. Links findable but subordinate; the support block is the only warm colored element on the screen.

**The mark is drawn at the full 62dp badge size rather than inset**, because the artwork carries its own margin: 4.1 lays it out on a 100 unit square whose content spans 14 to 86, so a 62dp glyph puts the front card at 44.6dp with 8.7dp of air around it, which is the launcher icon's proportion. Insetting it here would inset it twice. The tint blends `SrcIn`, so the 26 and 50 percent fills on the two cards behind survive it and the mark keeps the depth 4.1 calls load bearing.

**The `Elsewhere` rows carry no accent and no chevron.** A chevron in this design means a screen inside the app and every one of these leaves it, and an accent would compete with the one element on the page that is asking for something.

**One sentence this screen will carry is deliberately not on it yet.** `MASTER_BUILD_PROMPT.md` 14.4 and 16.11 require the medical disclaimer verbatim in the app and in the store listing at the same time, and the listing is phase 13, so shipping the sentence now would put it in the app before the thing it has to match.

**Paywall.** Does not exist.

**Onboarding and tutorial. Built, phase 10.** Contemplative. The tutorial uses a 56 percent black radial dim, a cutout with an 8dp feathered edge, a slowly pulsing 2dp white ring at 38 percent, and tooltip cards in surfaceRaised with a step indicator.

**The tutorial is complete and does not yet run.** Four of its five targets do not wear the modifier that reports their bounds, and the mechanism requires all five before it starts. That is the designed failure rather than a silent one: with a target missing the overlay waits and `hasSeenTutorial` is never written, so it runs correctly on the first launch after the four modifiers land in `ui/areas/`. `MASTER_BUILD_PROMPT.md` 13.2 and `docs/BUILD_STATE.md` carry it.

**Onboarding's room is one ground, one glow and eight to fourteen specks**, per 3.3, with the glow the only thing that changes between beats. It crossfades on `easeSlow`, which 8.1 gives to a world transition and which is what moving between beats is. **It does not breathe**: 8.2 item 8 puts a breathing glow on the Focus surface and nowhere else, and a second one here would be an ambient animation on a screen a person reads once. Beat 3 passes no glow at all, because the reveal's light is the Areas screen coming up through the iris and a second center of light would compete with it.

**Onboarding's controls are written in `ui/onboarding/` rather than reused from `ui/components/`.** Every control in that package resolves its colors from the Daylight world, and section 2 makes onboarding Contemplative from the first frame to the last. A Daylight button on `deepBlack` is not a theming bug that shows up in a screenshot: it is `actionBlue` fill and a white label, which looks plausible and is the wrong world. The Pulse surface made the same call for the same reason. What is shared is everything that is not color: the press scale, the focus ring, the click handling and the haptics.

**Beat 2's mood rows are a single row of six rather than 10.9's three by two grid**, which is the one place the beat departs from that section. The grid is right in a sheet that has the screen to itself; inside a beat that also carries chips, a field and a list of selections, a second block of rows pushes the Continue control off the bottom of a small phone. Six squares in a row still clear section 13's 48dp minimum at the narrowest width the app supports. The live preview 10.9 puts above the rows is already on the screen as the mini card that opened them, so it is not drawn twice.

**Two additions to the beats. Built, phase 10.** Addendum 01 8a and 8b.

First, a `Just start` path. Onboarding currently asks for two to four area names and a color for each, which is up to twelve decisions demanded of people whose central difficulty is deciding, before anything has happened. `Just start` creates one area named `Today` at the first default color, 3.4, and goes straight to adding the first item. Areas, names and colors become things discovered later.

It is offered as a genuine equal alternative and not buried. Both paths take the **same** button treatment, 10.7 Secondary, stacked, the shorter one on top, and neither carries a recommended label or a heavier weight: a fork with a styled winner is not a fork. This is the one screen in the app that deliberately has no primary button.

**As built, that is one composable called twice**, with no role parameter that could make either louder, and the area picker became the second screen of one path rather than the beat with an escape hatch attached. `Just start` sits on top: some order has to exist, vertical position is the only weight left once the treatment is identical, and the tie goes to the path that costs nothing, because the person who most needs a zero decision start is the least likely to read past the first option. Section 15.

Second, one line at the end of onboarding announcing Pulse before it ever appears: once a day, one question, one tap, and it can be turned off in Settings. Predictability is worth a line of copy. Interface behavior that arrives unannounced is a real cost for autistic users, and the cheapest possible fix is telling someone first.

**The line brought one motion consequence with it.** Beat 4's last moment holds rather than timing out into the app, because a line that announces a behavior and is then replaced on a timer is the unannounced behavior this addition objects to, one level up. The three paced moments before it spend the twenty to twenty five seconds `MASTER_BUILD_PROMPT.md` 13.1 gives the beat; the fourth waits.

### 11.2 The re-entry state

**Built, phase 12c. Detection phase 3b. The two engine side rules that follow this screen were built in phase 6 and phase 8.** Addendum 01 4d, issue #56. **This is the highest-stakes screen in the app**, and it is the one that has to be right the first time, because the person seeing it already decided once to stop.

**It is also the one screen nobody building or testing this app daily will ever see**, which is why every rule below is checked by a test rather than by looking. `ReEntryLanguageTest` reads the resource file and the screen's own source and holds the language rules; `ReEntryRoutingTest` walks a log a day at a time and holds when it appears; `ReEntryClearingTest` holds what the second choice writes. `ui/reentry/` is the whole surface and it is two files.

This audience leaves and comes back. That is ordinary use, not failure, and the app has no opinion about it.

**When.** The app opens 14 or more days after the last recorded open, 10.15. It replaces the first screen, once, and is reachable no other way.

**What it does not do.** It does not state the length of the gap. It does not count anything: not days away, not items waiting, not what was left active, not what was missed, not what a streak would have been. **No number appears on the screen at all.** It does not ask where the user has been.

**What it offers.** Two options. Keep everything exactly as it is, which is the default and sits first. Or clear active items and start fresh, secondary and text only. That is a hierarchy and it is deliberate: the default has to be the one that costs nothing. There is no third option and no skip control, because the first option already is the skip.

**The world it lives in. Daylight, on canvas, with the Areas structure behind it. Not Contemplative.** The obvious answer is a dark ceremonial welcome, a serif line and a soft glow, which is exactly what this design system is good at and exactly the wrong instrument. Ceremony says the absence was an event. A quiet Daylight screen says the app kept the user's place and is ready when they are. Section 15.

**Type and copy.** One readSerif line, one body line beneath, then the two options. No illustration, no mascot, no exclamation mark, 10.13. Whether that line is a fixed invitation in `strings.xml` or an authored observation was left to the corpus phase, with a simple test: if it says anything at all about what happened while the user was away, it is an observation and it comes from a corpus through the engine like every other sentence about a person's own data.

**Settled, phase 12c: it is fixed copy in `strings.xml`, and the test above is what settles it rather than a shortcut past it.** `MASTER_BUILD_PROMPT.md` 14b.4 forbids stating the gap, counting anything and asking where somebody has been, which leaves an observation with nothing to be about. What is left is an invitation and two labels naming what each choice does, and none of the four says anything whatever about the person's data. The engine is not involved and 11.1 is not bent: there is no sentence here about their own behavior for it to be the source of. The four strings carry the whole reasoning at their call site in `strings.xml`, and `ReEntryLanguageTest` asserts there is no fifth string, no literal in the source and no accessibility label carrying a word the four do not.

**Motion.** Item 25's entrance, and nothing else. No iris, no bloom, no transition into a different world.

**What follows it.** Pulse generates nothing for the first two days back, and the Report suppresses every decline, neglect and gap observation for the first full week back. Those are engine rules and `CLARITY_LOGIC_ENGINE.md` owns the mechanism. They are named here because their entire purpose is that this screen is not followed three hours later by the measurement it just declined to make.

**A returning user is never greeted by a measurement of their absence.** That sentence is the acceptance criterion for the screen.

**What phase 12c chose where this section left a choice open, under section 15.**

- **The block sits at the top of the screen and is left aligned**, where the Areas screen's own title sits, keeping its 20dp screen padding. The statistically common welcome screen centers a short line in the middle of an empty page with its buttons pinned to the bottom edge, which is a dialog, and a dialog is a demand. Sitting where the screen's content sits says the app is carrying on rather than staging a moment, and it makes the screen after this one look like the same screen with the sentences taken out
- **"With the Areas structure behind it" is read as structure, and separately the app genuinely is composed behind it.** The surface is opaque and nothing of the app shows through, but it is drawn over a live shell rather than instead of one, which is what makes 14b.4's "a conflict card waits behind it rather than being dropped" true of something that is still there afterwards. It also means the first frame of a cold start is not held while the log answers, which would put a blank screen in front of every launch this app will ever have for a question that is false on all but one day
- **The second choice is worded as what it does**, `Put active items back in their queues`, rather than as `Start fresh`. 14b.4 requires the wording to say that nothing is deleted and nothing is completed, and the fear at that moment is losing the work. The body line says both, and the label says where the items go
- **There is no back and no handler pretending otherwise.** 10.15's table gives this screen two ways out and neither is back, so back does here what it does on the first screen of the app, which is leave. The offer has not been answered, so it is still standing on the next launch that day. A handler that swallowed back would be a control that looks live and does nothing, which is the defect issue #58 exists to remove

### 11.3 Analog time

**Duration reads as a shape before it reads as a number.** Addendum 01 8d. Three surfaces show a session's remaining time, and all three make the depleting arc the primary carrier with the digits secondary: the focus ring in section 11, the Live Update track in 11.4, and the Focus Countdown widget in 12.2. **The first two are built, phase 4, and the widget is built, phase 12**, drawing its arc from the same persisted end instant so that the three cannot disagree.

The reason is specific to this audience. A number has to be read, subtracted from, and then converted into a feeling about how much room is left. That is three operations, and the third is exactly the one this app's users find expensive. A shrinking arc **is** the feeling, with no arithmetic in between. Time blindness is not an inability to read a clock.

- The arc is never decorative and never absent. There is no digits-only state on any of the three surfaces. A surface too small for both shows the arc and drops the digits, never the reverse
- The numeral never becomes the largest element. In app it is 64sp inside a 240dp ring, 5.3, and it does not grow past its 1.3x cap when the font scale does
- **Minutes only outside the app.** The widget and the Live Update read minutes and never seconds. A seconds counter on a home screen is a pressure device, and it also promises a refresh cadence neither surface can honestly deliver, 12.2
- The arc depletes **clockwise from the top**. Section 15 asks for the unobvious answer and this is the case it exempts: a countdown that runs the other way is a puzzle, and legibility outranks distinctiveness on the one element in this app that has to be read at a glance while doing something else. Recorded because 15 requires the reason to be written down when the obvious answer wins
- In calm mode the arc still depletes. It is information. What stops is the tip blur and the glow behind it, 16.2

**What primary means here, as a number rather than as a preference.** The ring is 240dp across and the numeral is 64sp, a ratio of 3.75 to one, and the numeral is capped at 1.3x the font scale while the ring does not grow with the text, so the closest a person can bring them is 83sp inside 240dp, 2.88 to one. A later session can check that with a ruler and does not have to agree with anybody's taste.

**Contrast runs the other way, and it is stated rather than left as a gap.** The numeral is `textBright` on the indigo ground and the arc is `#8BA4FF` at 6dp, so the digits are the higher contrast element and the arc is the larger one by the ratio above. **Size carries primary on this surface and contrast is spent on legibility**, which is the deliberate answer rather than the tidy one: making the arc the brighter of the two means either dropping the numeral below section 13's floor or brightening a 6dp line until it glares in a dark room, for an audience that runs sessions at night. Section 15 asks for the reason to be written down whenever the obvious symmetry loses. Addendum 01 8d asks for this relationship to be stated, and this paragraph is where it is stated.

### 11.4 The Live Update

**Built, phase 4, extended in phase 12.** Addendum 01 Step 5. A focus session is exactly the user-initiated, start-to-end, time-bound task that Android's Live Updates were designed for. On a Pixel it surfaces as a status bar chip that expands; on Samsung devices it appears in the Now Bar. For an audience with time blindness, a running session visible without opening the app is not a nicety. It is the point.

It is a platform surface taken at step 1 of 17.1: the promoted-notification progress style, not a foreground notification dressed up to look like one. The API, the permission and the availability check belong to `MASTER_BUILD_PROMPT.md`. What it looks like belongs here.

**What it shows.** Three things and no more: the area name, the item title, and the remaining time as a track that depletes, 11.3. The track takes the area color where the platform allows a color and the system accent where it does not. Neither case gets an edge treatment, a second color, or a gradient.

**Segments and points.** One undivided track. The single exception is the transition warning, 10.18: while it is on, one point sits on the track at the five minute mark from the start of the session, and the track reaching that point **is** the state change. With the warning off there is no point and no state change at all. Nothing else is marked, because the feature list of a component is not a reason to use it.

**Built as one segment carrying the area color, with progress set to the time remaining rather than the time spent**, which is what makes the track deplete rather than fill. The point takes no color of its own, because this section allows the track one color and no second one. Logged in `DECISIONS.md`.

**Actions.** Two at most: `Add 10 min` and `End`. Both work without opening the app. The abbreviation is deliberate and is the only place it appears; the in-app control reads `Add 10 minutes`, 10.18, and the system action button is width constrained. Tapping the body opens the focus screen.

**Calm mode**, 16.3. No color transition, nothing pulses. The track still depletes.

**Degradation, required.** Where promoted notifications are unavailable or denied, the surface falls back to the ongoing notification with a chronometer that the session already had. The app is fully usable with no Live Update at all, nothing is gated behind it, and **the user is never told their device is missing out.** No upsell, no explanatory dialog, no `your device does not support` line. A person cannot act on that information and telling them only makes their phone feel worse.

**It is silent**, it is dismissed when the session ends, and it never re-engages.

**A standing constraint, recorded here because Addendum 01 5e asks for it in this document by name.** This is the only Live Update this app will ever post. Live Updates are for user-initiated ongoing tasks, and nothing else in this app is one: not the Pulse reminder, not a generated Report, not a backup reminder, not an inbox that has grown, not a plan that was accepted, not a session that has already ended. A later session reading this document should treat any proposal for a second one as a change requiring the owner's decision, not as a feature. Addendum 01 5e and 9d.

---

## 12. Widgets

**Eight specified, six required in v1. The six are built, phase 12; the two optional ones are not.** Addendum 01 Step 6 supersedes the two-widget plan in `MASTER_BUILD_PROMPT.md` 13.3, and that supersession is logged in `DECISIONS.md`.

**Why widgets, and not more notifications.** A notification is an event: it arrives once, it is swiped away, and for this audience that means it never happened. A widget is persistent and cannot be dismissed. It is still there tomorrow. It works **with** out of sight, out of mind rather than against it. That is the whole argument, and it is why this app spends its home screen budget here while keeping its notification budget to the Pulse reminder and the one Live Update in 11.4.

The goal for every widget: **zero taps to see, one tap to act.**

### 12.1 The shared DNA

One design DNA: 16dp padding, serif for the single large element, sans for everything else, area tints at 3 to 5 percent light and 5 to 7 percent dark, 8dp inner radii, **no borders and no colored edges**. Every widget renders correctly in dark mode, scales text without clipping at the smallest grid size, and shows a sensible state when its area no longer exists.

Added by Addendum 01, and required of all eight:

- **Calm mode**, 16.3. The tint transform applies, nothing pulses, and there is no animated state to suppress in the first place
- **The configured area can vanish.** An area can be archived or deleted while a widget points at it. The widget then shows one plain line naming what happened and offering to be reconfigured. Never an error, never a blank box, never a stale name that lies
- **TalkBack.** Every widget is usable with a screen reader, with content descriptions that read as sentences rather than as labels: `Personal, in focus, seven minutes left`, not `Personal 7`
- **A real preview image** in the widget picker, generated from the real widget with representative data. Never a mockup, never a screenshot at the wrong size, never the empty state
- **One separation device**, 6.1, inside the widget as well as around it. A widget is already a card, so nothing inside one gets a second device

**Data.** Every widget reads the widget snapshot, written on each meaningful change plus the periodic refresh. **Widgets never read a corpus and never run the engine.** A widget that needs a sentence receives it from the snapshot already rendered.

**Taps.** Deep links open the surface named in each entry, with one override: while a focus session is running, any widget tap opens the focus screen.

### 12.2 Required in v1

**Next Up**, small, 2x2. One active item: the 7dp dot, the area name at label, the item title as the single serif element, and beneath it a plain count of what waits behind it. Configurable to a pinned area, or automatic, which shows the least recently touched active area and rotates daily. Tap opens that area.

**First Step**, small, 2x2. The active item's **first step**, 10.17, not its title. With no first step set it shows the title and one quiet line inviting one. Tap starts a focus session on that item.

The hardest moment is starting, and the title of a task is often the intimidating part of it. Putting the smallest possible action on the home screen removes the activation barrier at the exact moment it bites, which is a thing a widget can do and a list cannot.

**Quick Capture**, small, 2x2 or 1x1. One large tap target that opens capture directly into the unfiled inbox, 10.16, keyboard already up, no area to choose. The inbox count sits beneath as plain text and is absent at zero. **Never a badge, never a red dot.**

The principle it embodies: every decision standing between the thought and the record is a place the thought is lost.

**Focus Countdown**, small, 2x2. Live during a session: the depleting arc as the primary carrier with the digits secondary, 11.3, matching the in-app ring. Digits read minutes. With no session running it is a `Start focus` tap target. Tap during a session opens the focus screen.

Glance updates are throttled by the system. The cadence is chosen deliberately and the reasoning recorded, and the arc's granularity never implies a precision the refresh cannot deliver: an arc that jumps four minutes at a time is worse than one that moves in minutes and is honest about it.

**All Areas**, medium, 4x2. Every non-archived area as a row: dot, name, and the active item title or `Idle`. Configurable to all areas or a chosen subset. Color carries area identity here so the list can be parsed without being read, which matters when reading is expensive. Tap a row opens that area. Where more areas exist than fit, the widget shows as many as fit and one final plain line reading `and 4 more`, never a scroll and never a truncated dot row.

**Rhythm**, medium, 4x2. The 14 day dot row exactly as Momentum renders it, without item 13's cascade because widgets do not animate. Beneath it one plain line: `Active 11 of the last 14 days.` Tap opens Momentum.

**This must never become a streak.** No consecutive count, no longest run, no flame, no chain, no comparison with the previous fortnight, no color change for a run of good days. **A gap is a lighter dot and nothing else is said about it.** The reason is in 14, and it is the strongest reason in that section.

**Built, phase 12**, all six, in `widget/`. What they draw is one snapshot, written by the
app on every meaningful change and refreshed every six hours, and nothing in that snapshot
is a key or a family or a stage, so there is no way for a widget to compose a sentence
even if one were asked for.

**The one part of 12.1 that is not built is the preview image.** This section requires it
to be generated from the real widget and never from a mockup, and a hand written preview
layout is that mockup with a different file extension, so the six ship with none and the
picker shows the loading layout until a real one is generated on a device. It is the only
shared rule in 12.1 that phase 12 did not meet, and each widget's resource file says so at
the line the attribute would sit on.

### 12.3 Optional, built if phase 12 has room

**This Week**, small, 2x2. Three numbers from Momentum: completed, focus minutes, reflections. Typographic, no chart, no gauge, and **no ring filling toward a target, because there is no target.**

**One Thing**, medium, 4x2. If the user accepted a plan from the most recent Report, it appears in its first-person committed form. If not, the Report headline appears instead.

**Rule.** This is the only place guidance appears outside the Report, and it appears only because the user chose it. **It never shows an unaccepted plan and never shows a declined one**, and there is no state in which this widget asks the user to accept anything. A plan the user did not accept has no home screen presence at all.

**Neither was built in phase 12, which this section permits.** The snapshot carries a slot
for each, `WidgetWeek` and `WidgetGuidance`, so building them later is a composer change
and a widget rather than a change to the shape the other six read. The rule above is
already enforced at the boundary: the composer reads whether a plan was accepted and
writes nothing otherwise, so an unaccepted plan cannot reach a home screen even by
mistake.

### 12.4 The other launcher surfaces

**Built, phase 12.** Same reasoning as the widgets: fewer steps between intention and action.

**App shortcuts**, on a long press of the app icon. Three static shortcuts: Quick capture, Start focus, Today's Pulse. Icons come from the set in section 7, `add`, `play_arrow` and `graphic_eq`, rendered as the launcher requires. Short sentence-case labels, no counts, no dynamic shortcuts and no recents. This strikes home screen shortcuts from `MASTER_BUILD_PROMPT.md` 18, and that is logged in `DECISIONS.md`.

**A quick settings tile.** One tile that starts or ends a focus session from the shade and reflects live session state in its label and its active state. It shows no count and no area color: a tile has one accent and it belongs to the system, 17.1.

**How the two were built, and the three choices inside them.**

The shortcuts are a static resource and one manifest line, with the `add`, `play_arrow`
and `graphic_eq` glyphs from section 7 on the app icon's own #141A2E disc. **The disc is
not decoration.** A launcher draws a shortcut icon into a popup whose background follows
the phone's theme and it does not tint it, so a bare ink glyph is invisible in one of the
two themes; the disc gives the three their own ground and makes the row read as this app.
The short label and the long label are the same three names, because a launcher shows the
long one where it fits and a more descriptive long label would be the string most people
actually saw, which would quietly replace the three names this section chose.

**`Start focus` opens the Focus surface rather than starting a session**, on the shortcut
and on the tile alike, even where exactly one area has an active item and the choice would
be unambiguous. Section 15's test applies and the obvious answer loses on its merits: a
session is a row in a log that only gains rows, the person has not seen a screen yet, and
the First Step widget already answers the same question the same way, so all three
launcher surfaces say one thing rather than two. With a session already running, the same
tap opens that session, so nothing here can start a second one.

**The tile keeps one icon in both states.** `timer`, from section 7, with the label
reading `Start focus` or `End focus` and the tile's own active state carrying the rest.
Swapping the icon as well would be a second separation device on one element, 6.1, and
the third signal is the one that eventually disagrees with the other two. It carries no
subtitle and no countdown either: the shade is readable over a lock screen and what
somebody is working on is not for a passer by, while a number that only moves when the
shade opens is worse than no number at all.

---

## 13. Accessibility

- Contrast minimum 4.5:1, verified per area color in both modes, **and in calm mode**, with the 16.2 transform applied. Calm mode is the one place where trying to serve an accessibility need could break another one, so it is measured rather than assumed
- Touch targets minimum 48dp
- Content descriptions everywhere, reading sensibly aloud
- TalkBack order verified per screen. Pulse reads observation, question, options. The Report reads eyebrow, headline, **then a spoken summary of the ribbon** ("busiest on Wednesday, quiet at the weekend"), then sections
- **All swipe actions duplicated in a long press context menu and in the detail sheet**
- Font scale to 200 percent without clipping. The timer numeral caps at 1.3x
- Color is never the only signal. Idle versus active differ in text and opacity. Completed Trail events carry the check icon. The Pulse ready state is a dot plus a changed chip label. **The ribbon is never the sole carrier of any claim; the caption beneath states the numbers**
- Contemplative text stays at or above 55 percent opacity where it is meant to be read
- Reduce motion honored globally, 8.3, and calm mode with it, 8.5

**Where the accessibility investment goes. Addendum 01 8f.**

- **Size.** The system font scale to 200 percent is the floor, not the ceiling. An in-app text size control offers the same steps on top of it, for the person who needs larger text in the one app they read prose in rather than everywhere. The combined result is capped so that no surface exceeds the 200 percent condition already required above. **Built, issue #51. It multiplies rather than overrides, and 13.2 records why.**
- **Spacing.** The 4dp grid, the 28dp section spacing and the 11dp card rhythm in section 6 are minimums, not targets to compress when a screen gets busy. Generous spacing is an accessibility feature here and not only a taste: a dense screen is a screen this audience cannot scan. **They are also minimums in the other direction: every vertical gap in the app opens with the text size and never closes below its specified value.** 13.2
- **Contrast**, as above, in every theme
- **No dyslexia-friendly typeface.** The evidence for specialized dyslexia typefaces is thin, and the same effort spent on size, spacing and contrast has evidence behind it and helps more people. This is a deliberate refusal, recorded here so a later session does not mistake it for an oversight and add one. Addendum 01 8f

### 13.1 Language

Two rules that belong in this document rather than in a corpus, because they govern every string the app can put on a screen, fixed interface labels included.

**Plain, concrete, and never a verdict.** Empty states are invitations, 10.13. An error says what went wrong in one sentence. Nothing on any surface tells a person what they should have done. Every sentence about a person's own data comes from a corpus through the engine; this rule covers the strings that are not.

**One forbidden word.** `abandoned` never appears anywhere a user can read it, in any form, on any surface. Not in the Trail, not on the completion screen, not in a content description read aloud, not in an export, and not in a log line an export writes. A session ended early is a completed short session, section 11, and the Trail says `Stopped after 14 minutes`, which is what happened and carries no verdict. Addendum 01 4e.

**The event type was renamed to `FOCUS_ENDED_EARLY`.** No user visible string contained the word, but a raw type name is readable in an unencrypted export and appears in `docs/EVENT_FORMAT.md`, which the future Linux desktop app is built against. The name teaches the next implementer what the concept means, so it had to be the right one. Decided by the owner, recorded as C6 in `DECISIONS.md`.

### 13.2 The text size control: it multiplies, and the combined result stops at 200 percent

**The decision.** The app's own text size setting is a **multiplier on the OS font scale**, not a replacement for it. `Default` is exactly 1.0, so a person who never opens the row gets precisely what their phone asked for, at every phone setting.

**Why, and what was given up.** Overriding is the tidier product. It gives one dial that means one thing, and it respects nothing. Somebody who has set the phone to 200 percent has already said, once, that they cannot read text at 100 percent; an app that overrides opens at 100 percent for them, silently, until they find a setting they have no reason to look for. That is a first-launch accessibility regression traded for a cleaner explanation, and there is no version of it worth taking. What multiplying costs is the ability to say what any one step *means* without knowing the phone: `Large` is 1.15 times whatever the phone is doing, so the same word is a different size on two devices. That cost is paid in one sentence under the control rather than in a screen somebody cannot read.

**The steps are the platform's own:** 0.85, 1.0, 1.15, 1.3, 1.5. Android's font size control uses exactly these, which is what "the same steps on top of it" above has always meant. A bespoke ladder would have been a second vocabulary for a quantity the phone already names, and `Large` in this app would have been a different size from `Large` in Android settings.

**The cap is 200 percent combined, and it is a measurement rather than a round number.** Every clipping analysis in this document is written against the 200 percent condition: this section states it, 5.3 caps the timer numeral at 1.3x of it, and 10.4's floating bar is sized by a measurement that concludes one label plus four icons comes to roughly 290dp of the 314dp there is. A control able to exceed 200 percent would invalidate all three at once, on a device nobody has run yet. So the app's steps spend the headroom below the cap and stop.

**What that costs, stated rather than hidden.** A phone at 200 percent has no headroom left, so the steps above `Default` change nothing for that person. The rows stay tappable, because a choice stored now is true again the moment the phone's setting comes down and because 10.16's rule about disabled controls applies; one line appears under the control saying the phone's own setting is what is deciding. The `Small` step is why the control is not inert for them.

**Two platform steps are deliberately not offered.** 1.8 and 2.0 exist in Android's own control and not in this one. Through this control they would be clamped by the cap in almost every case where the phone is not at 100 percent, so they would be two more rows that usually do nothing. They stay reachable at the phone's setting, which is where a person who needs them has already been.

**Spacing scales with the text, and only on one axis.** A size control that grows the type inside boxes that stay put makes a screen less legible rather than more, which is the failure Addendum 01 8f names and issue #51 repeats. So every vertical gap, every vertical padding and every minimum row height in the app is read through `ClaritySpacing.scaled`, which multiplies by the combined text scale. Three rules govern it:

- **It never goes below 1.0.** The numbers in section 6 are minimums, so a smaller text setting makes the text smaller and leaves the air exactly where section 6 put it. Every dimension in the app is therefore identical to what it was before this control existed whenever the combined scale is at or under 100 percent, which is the default on a default phone
- **It is linear above 1.0.** A line box at twice the size next to a gap at twice the size is the same page read larger. Damping the gaps would tighten the rhythm exactly where 8f wants it opened
- **Horizontal insets do not scale at all.** `screenPadding` and `cardPaddingHorizontal` hold their specified value at every text size. Width is the one axis a phone has no more of, and a margin that grew would take the room from the measure it exists to serve, which is where clipping starts. The measure gets longer instead

**What does not scale, and why.** The 48dp touch minimum, because it is a statement about a fingertip and anything that grows with the text passes it on its own. The 7dp area dot, because 3.4 makes it identity rather than rhythm. The sheet handle, because it is a grip. The floating tab bar's 61dp and its 17dp inset, because the bar's contents are a 24dp glyph that does not scale and one label whose trimmed box reaches about 31dp at the cap, both of which clear the 48dp item inside it with room over; a bar that grew would take the reserved bottom padding of four screens with it.

**The control is five named steps and the screen it sits on is the preview.** Recorded under section 15: the statistically common answer is a slider with a live preview paragraph, and it loses three ways. A slider is a continuous control for a quantity that is discrete on this platform and a ladder in 5.3. A preview paragraph is a specimen in a fixed box, which is the one thing that cannot show the half of this feature that matters, that the spacing moved. And Settings is already real content at real sizes, so the tap re-lays out sideheads, row titles, captions, a paragraph, switches and a card while the person is looking at them. **Every option label is set at the current size and never at its own**, because a `Small` row set in Small is a row the person who needs this control cannot read.

---

## 14. What This Design Never Does

No pure white or pure black backgrounds. **No colored stripe, bar or edge treatment on any element, ever.** No red warning states for normal behavior. **No streaks, no consecutive-day counts, no chains, no badges, no XP, no levels, no confetti and no celebration of any kind.** No emojis. No em or en dashes. **No all-caps section labels.** **No serif italic as a section-level accent.** **No element carrying two separation devices.** **No cards inside cards, and no cards inside sheets except the color picker preview in 10.9.** **No destructive action committed by a full swipe.** No default Material purple. No dynamic color from the wallpaper. No stock Material list rows on primary screens. No loading spinners where a shimmer can stand in. No sparkle or magic-wand iconography. No locked features, upgrade prompts, premium badges or comparison tables. No screen that fails the three second glance test.

Added by Addendum 01: **no red dot and no numeric badge on any surface**, including the unfiled inbox count, 10.16, and every widget in section 12. The app has exactly one status dot, the amber Pulse dot in 10.1, and it carries no number and no count. **No progress ring or bar pointed at a target**, because there is no target. **No rendering of the difference between an estimate and an actual**, 10.17. **Never the word `abandoned`**, 13.1. **No second Live Update**, 11.4. And no dyslexia-friendly typeface, 13.

**Two reasons for the streak rule, not one.** The first has always been that shame mechanics are the opposite of what this app is for. The second is specific and stronger: for people whose capacity fluctuates, **streak loss is a documented abandonment trigger.** A streak is a promise the app makes on the user's behalf and then breaks for them, on a day that was already going badly, and the app then displays the broken promise until they delete it. Addendum 01 9c. The consequences are concrete and testable: the Rhythm widget shows fourteen dots and no run length, 12.2; Momentum's dot row never says how many in a row; and a gap is rendered as a lighter dot with nothing said about it anywhere.

And nothing that is still. An app that never moves is an app that feels broken, and that holds in calm mode too, 16.4.

**Phase 12b narrowed that sentence, because as written it was false and every conventional way of making it true was a tell.** The question it was asked to settle was whether anything in this app moves at rest, and the honest answer is that four things do and none of them is decoration.

**The only thing this app moves on its own is time.** A session's arc depletes and its numeral rolls, 8.2 item 7. An in session card counts down, 10.3. The focus glow breathes, item 8. The Pulse's ground shifts through the day, 3.3, on a timescale a person feels rather than watches. Everything else responds the instant it is touched: a press scales, a swipe tracks the finger, a promotion runs, a snackbar's line depletes, a tab pill travels. **A screen with no time on it does not invent motion to prove it is alive.**

That is a refusal and it is recorded as one. Every conventional way to put motion on a still Daylight screen is on 15.1 or beside it, and 15.3 refuses the pulse, the breathe, the glow loop and the ambient shimmer by name. The alternative to an honest silence is a decoration that says nothing about the person's data, and section 1's first rule is that this app never decorates data. **A recorded absence is worth more here than a tell**, and the sentence this paragraph amends was written before there were surfaces to test it against.

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
- A flame glyph beside a consecutive-day count
- A ring closing toward a daily target
- A red numeric badge as the primary signal that something is waiting
- An expressive motion system adopted at full strength with no setting to turn it down

The last four entries arrive from Addendum 01 rather than from the August 2026 sweep, and their research entries in `docs/DESIGN_RESEARCH.md` are **pending**. They are listed here because all four are already ruled out by sections 14 and 16, and a tell that the design forbids is exactly the kind that comes back in through a component someone reached for without checking.

### 15.2 Release gate

The verification checklist includes an anti-slop pass against the dated list above. **Update the list before each release** rather than trusting the version in this file.

**A sweep needs the outside world, and that is a real precondition rather than a formality.** 15.1 is a record of what the industry currently produces, so it cannot be re-derived from inside this repository or from a model's memory of the year before it was written. Phase 12b could not perform one, and it deliberately did not re-date the list rather than stamping a date on a sweep that did not happen. **The sweep is owed before the release**, and until it happens 15.1 is August 2026 and should be read as such. Nothing phase 12b built appears on it and nothing phase 12b built is refused by 15.3, which was checked entry by entry; that is a narrower claim than a fresh sweep and it is the one that can honestly be made.

### 15.3 The refusals, and the entry that refuses each

**This section exists for the session that reads a design audit and reaches for the nearest premium looking thing.** It was written in phase 3c out of the audit issue #53 commissioned, and every line of it is a fix somebody would reasonably propose for a problem this app actually has, paired with the sentence in this document that already forbids it. A prohibition nobody can find is a prohibition that gets rediscovered the expensive way.

**A section of its own rather than more entries on 15.1, and that is the deliberate choice rather than the obvious one.** 15.1 is a dated record of what the industry currently produces, 15.2 requires it to be re-swept before every release, and it is expected to churn. Nothing below is an industry observation and nothing below expires: each entry is a specific temptation in this specific app, and re-deriving them at every release is the work this section exists to prevent. Merging them would blunt 15.1 as well, because 15.1 is read as a list of tells to check a design against and half of these are not tells at all, only fixes that break a rule. So 15.3 cites 15.1 and does not join it, and the dated list stays dated.

Each entry names the fix first, because that is the form it will arrive in, and the rule second.

- **A colored left edge, accent stripe or bar on the area card, to give it identity.** 15.1, "a colored 3 to 4px border or stripe on one side of a card". Section 14, "no colored stripe, bar or edge treatment on any element, ever". 3.4, "never as a stripe, bar, edge, border or filled block". Three separate prohibitions, and the first of them is this document's stated single most recognizable machine generated tell
- **A hairline border on the card so it reads against the canvas.** 6.1, "never a hairline and a shadow on the same element", and 15.1 lists the pair. A light mode card already carries a shadow, so a border on it is forbidden by construction. When a card does not read, the answer is the ground under it, which is what 3.1 changed in phase 3c
- **A blurred or translucent tab bar, header or sheet.** 15.1, "glassmorphism used as decoration rather than to solve a layering problem", and separately "glassmorphism combined with a neon glow". Content passing under a floating bar is a real layering problem and it does deserve an answer; a fade to the ground color is the permitted form of one, and reaching for the blur because it looks more modern is exactly the move the entry describes. **The fade is built, 6.1, phase 12b**, and the refusal is a test rather than a sentence: nothing in `ui/` may reach for a blur
- **A gradient on a screen title, on the timer numeral, or on any large figure.** 15.1, "a gradient applied to a large number for impact"
- **Swapping Newsreader or Hanken Grotesk for a family that looks more premium.** 15.1 names three of the likely replacements outright: "Inter as the interface typeface", "Instrument Serif, particularly paired with Inter", "Space Grotesk paired with a display serif". The two families in 5.1 and 5.2 were never the problem. Through phase 3b the problem was that seven of the nine sans roles carried no tracking value at all and the serif appeared on one screen in one word, and 5.3 settled both without touching a family
- **Serif italic on day headers, sideheads or eyebrows.** 15.1, "serif italics used as an accent device". Section 14, "no serif italic as a section-level accent". 5.1 permits italic only for genuine emphasis inside a sentence
- **All caps sideheads, to give a sheet or a screen some structure.** 15.1, "all-caps section labels". Section 14 repeats it and 5.3 sets sidehead in sentence case and says so in bold. The structure a section label needs is bought with tracking instead, which is what sidehead's value in 5.3 is for and why it sits deliberately above `label` at the same size
- **A card inside the filing chooser sheet, to give its rows an affordance.** Section 14, "no cards inside cards, and no cards inside sheets except the color picker preview in 10.9", repeated in 10.6, which adds that the exception may not be extended by analogy. The v2 mock does exactly this and the mock is wrong, as is its 30dp filter chip, which fails the 48dp touch minimum in section 13
- **A filled green disc with a white check on the Trail's completed row.** 3.4, "never as a stripe, bar, edge, border or filled block". Section 14, "no confetti and no celebration of any kind". Another v2 mock leftover, and it was never the answer to the question about what the event circle carries, which phase 12b closed by removing the circle. Section 11
- **Any indigo to purple or blue to purple treatment, including on the Focus surface when it arrives.** 15.1 names the family three times: "lavender or indigo-to-purple gradients", "a blue to purple gradient, the single loudest tell of 2026", "a purple to cyan gradient". Section 14 adds "no default Material purple". 3.3's Focus gradient runs indigo into darker indigo over black and it stays on that side of the line
- **A pulse, a breathe, a glow loop or an ambient shimmer, to satisfy section 14's "nothing that is still".** **Settled in phase 12b and the answer is no.** Every conventional answer is on 15.1 or adjacent to it, and none of them says anything about the person's data, which section 1's first rule forbids. Section 14 is narrowed instead: the only thing this app moves on its own is time, four surfaces do it, and a screen with no time on it does not invent motion to prove it is alive. A recorded absence is worth more than a tell
- **A count badge or a red dot on the inbox chip.** Section 14 as amended by Addendum 01, "no red dot and no numeric badge on any surface", naming the unfiled inbox count. 15.1, "a red numeric badge as the primary signal that something is waiting". 10.16 already says what to do instead, which is that the count is the label
- **A semantic palette for the Trail's event glyphs: green completions, amber Pulses, red deletes.** 3.1 scopes `positiveGreen` to completion, `warnAmber` to the Pulse ready dot and `deleteMuted` to the delete swipe, one job each, so a second use of any of them is a color that means two things. 3.4 permits an area accent in four forms and none of them is a per event hue. Section 11 records what the icon column carries instead, which is a glyph and nothing else
- **An accent rule, an accent border or a two dp underline on a focused text field.** 6.1, "every element carries exactly one separation device", and the well is already it. 10.19 takes the well one rank deeper instead, which is the same device speaking louder rather than a second one arriving. This is also the most common form treatment in existence and section 15 makes that a reason by itself
- **Reimplementing the bottom sheet, the text field, the snackbar or the tab bar in order to change how it looks.** 17.4, and 17.2's "not a reason: preferring the look of something drawn by hand". The six components in 17.3 are settled and are not reopened
- **Adopting a Material 3 Expressive default because it is the default.** Section 15 itself. The floating tab bar's selected label only behavior arrived at the platform default with no recorded reason, which is how a screen built faithfully to this document came to read as a stock navigation bar
- **Trying to make the cards pop.** Through phase 3b there was nowhere for them to pop to, because `card` was pure white and the light world had no headroom above it, so every hour spent lifting the card further was an hour not spent on the ground under it. That is the shape of the mistake and not only one instance of it: when an element does not separate, measure whether the ground it sits on has any room left before reaching for the element

**15.1 and 15.3 fail in opposite directions**, which is the last reason they are two lists. A stale entry on 15.1 costs a false alarm about a treatment nobody makes any more. A missing entry here costs the treatment.

---

## 16. Calm mode

**Built in phase 3b, the executive function retrofit.** Addendum 01 8c assigned calm mode to phase 1, which closed in August 2026, and phases 2 and 3 shipped the motion system and the surfaces it had to reach back into. The move to a retrofit phase is recorded in `DECISIONS.md` as conflict C3.

The switch, the transform, the entrance rule and the three audits in 16.6 to 16.8 are in the code.

**The Settings row is built too, phase 11, and this paragraph used to say it was not.** It recorded that the row could not be built because no Settings screen existed to put it on. That screen was built in phase 11, the row went on it under Appearance with the label and caption 16.1 states, and the record is corrected here rather than left to be found by somebody reading a stale sentence. Everything below is true of the running app.

Material 3 Expressive is this app's motion model, adopted in phase 2 and recorded in `docs/DESIGN_RESEARCH.md`. Expressive motion is the right direction for this product, and it is the wrong thing to impose on a person for whom movement and saturation are expensive. Both are true at once, and calm mode is how both stay true: **ship the expressive direction, and ship the exit.**

That is also what makes an expressive direction defensible in the first place. Without an exit, every expressive decision has to be argued down to what the most sensitive user can tolerate, and the result is an app that is dull for everyone and still too much for someone. With an exit, section 8 can be as alive as it wants to be.

**One switch, not a spectrum.** Calm mode is a single setting that turns the whole system down at once. It is never a per-animation preference, never a per-screen preference, and never a list of checkboxes. A person who needs it needs it before they have learned what any of the checkboxes mean.

### 16.1 The switch

One row in Settings under Appearance, 10.11, labeled `Calm mode`, with a caption reading `Less motion, softer color`. A plain two-state switch, `toggle` haptic, section 9.

**Built, phase 11.** The row sits under the appearance tiles and the caption line about the always dark surfaces, which is where the appearance group ends and the one preference that is not a theme begins.

**The default follows the system.** While the user has never touched the switch, calm mode is on whenever the system reduce-motion setting is on and off when it is not, live, with no restart. The first time the user touches it, it takes a value of its own and stops following.

The obvious answer here is a three-state control, On, Off, Follow system, matching the appearance picker in 10.10. It is rejected. A third state is a third decision on a settings screen already full of them, for the audience whose central difficulty is deciding, and the two-state switch already produces the correct behavior for everyone who never opens it. Section 15.

**Once set, it stays set, and there is no way back to following the system.** The alternative, keeping the switch tracking the system whenever the two happen to agree, was rejected: a control that silently changes state because something outside the app changed is a control this audience cannot rely on, and predictability is worth more here than saving somebody one tap. Storing the setting as absent-or-a-value rather than as a boolean is what makes both halves of this true: absence is a state the storage carries and the interface never shows. Section 15, and recorded in `DECISIONS.md`.

**Why the row is not a bare boolean in code.** A `Boolean` defaulting to `false` would mean off for every person who has the system setting on and never opens Settings, which is precisely the person calm mode exists for. The stored value is nullable and the resolution is one function, so the default can be asserted in a test rather than hoped for.

**That is also why the Settings state carries no calm mode field.** The row reads the resolved two-state value out of `LocalCalmMode`, which the theme computes from the stored value and the live system setting. A copy inside the screen's own state would be a second answer to a question that already has one, and it would be the answer that stops following the system.

**Reduce motion always wins on motion.** Calm mode is a superset of 8.3, never an override of it. Turning calm mode **off** while the system asks for reduced motion restores color, not movement. The app never animates against an accessibility setting because a preference inside the app said it could.

### 16.2 What it changes

The color half is one transform, applied in one place. **Chroma is multiplied by 0.6 in a perceptual color space, holding lightness.** Blending toward grey is the obvious implementation and is rejected: it darkens light colors and lightens dark ones, which moves every contrast ratio that section 13 has already verified per area color. Holding lightness means calm mode cannot break a measurement that passed. Section 15.

**Two uses of the area accent and five tokens are excluded by name.** The 7dp area dot and the area label text, because they are how an area is recognized and they are the two places the contrast was measured. actionBlue, positiveGreen, positiveInk, warnAmber and deleteMuted, because 3.1 scopes each of those to exactly one job: they are function, not atmosphere, and a desaturated action color is a less legible action color. `positiveInk` joins the list with the token, phase 13, on the same sentence: it is the foreground half of the completion color and a foreground is the last thing a transform should be allowed to take saturation from.

Everything else that takes an area accent or a surface accent takes it through the transform. At 5 to 7 percent the difference is barely visible, and that is fine: the rule is uniform, so there is no list of elements to keep in sync and nothing drifts back to full saturation later.

| element | ordinary | calm mode |
|---|---|---|
| every animation in 8.2 | as specified | the 8.3 path, one 150ms crossfade |
| entrances, items 4, 11, 12, 13 and 14 | first open per tab per session, 8.4 | do not fire at all. The list renders already settled |
| focus glow breathing, item 8 | 0.85 to 1.0 over 8 seconds | static at 0.92 |
| tutorial ring pulse, item 19 | 0.25 to 0.45 over 2 seconds | static at 0.35 |
| the undo snackbar's depleting line, item 20 | depletes over 5 seconds | still depletes. It is the only readout of a window that is closing, and that is information, not decoration |
| the focus arc, 11.3 | depletes at 1Hz | still depletes, for the same reason |
| cardWash | 5 to 7 percent light, 7 to 9 dark | transformed, and pinned to the low end of its range |
| cardWashActive | 12 to 13 percent light, 15 to 16 dark | transformed, and pinned to the low end: 12 light, 15 dark |
| the 7dp dot, the area label | full accent | **unchanged**, and the label variant is computed from a ground that does not move with the switch, so it is the same color either way |
| Momentum's 60 percent area tile | full accent at 60 percent | transformed. This is where the transform is most visible in the Daylight world |
| Contemplative radial gradients, 3.3 | three-stop radial per surface | transformed, geometry held. A surface with no center of light is not calmer, it is a black rectangle |
| the specks, 3.3 | 8 to 14 dots at 3 to 6 percent | 8 dots at 3 percent, still one fixed seed |
| focus ring stroke and tip | `#8BA4FF` stroke, `#B9C8FF` tip, soft blur | transformed, **and the tip blur removed** |
| the Report's two gold glows, 11.1 | 6 to 8 percent, two centers of light | 4 percent, one, behind the headline only |
| Pulse's time-of-day background shift | dawn, midday and evening blends | held at the midday neutral warm black all day |
| the Pulse pill fill, item 10 | amber fills from the tap point | crossfade |
| widget tints, 12.1 | 3 to 5 percent light, 5 to 7 dark | transformed, 16.3 |
| the Live Update track, 11.4 | area color where the platform allows it | transformed. Still depletes |
| the scroll edge fade, 6.1 | fades content out at both edges | **unchanged.** It carries no color and no motion of its own |
| a text field's well, 10.19 | `raise`, deepening to `canvas` on focus | **unchanged.** Three neutral tokens and one excluded accent, the caret |
| canvas, card, raise, ink, hairline, parchment | as 3.1 and 3.2 | **unchanged.** Calm mode is not a theme |

### 16.3 It reaches the widgets and the Live Update

Calm mode is a device preference, not engine state, so it travels to the widgets in the widget snapshot like everything else a widget reads, 12.1. Glance has no animation system worth suppressing, so the widget half of calm mode is entirely color, plus the absence of any state that would have pulsed. The Live Update honors it the same way: no color transition, nothing pulsing, and a track that still depletes because that is the surface's only content.

### 16.4 What calm mode is not

- **Not a theme.** It changes no canvas, card, raise or ink token, no layout, no spacing and no type size. The same screens, with less atmosphere
- **Not low contrast.** Every ratio required by section 13 must still clear 4.5:1 with the transform applied, verified per area color in light, dark and Contemplative. This is the one place where serving one accessibility need could break another, so it is measured
- **Not still.** Presses still respond, swipes still track the finger, the timer still updates, the arc still depletes. Section 14 still holds: an app that never moves is an app that feels broken, and a calm app is not a dead one
- **Not announced.** No banner, no badge, no chip reading `Calm mode is on`, no line in an empty state explaining why the screen looks different. The user turned it on and does not need to be told
- **Not a simple mode.** It hides nothing, removes no feature and locks no control. It is not a drawer for future reductions, and anything proposing to become one is a different setting with a different name

### 16.5 The obligation on everything else in this document

**Anything this document defines needs a calm mode answer.** Every token in section 3, every curve in 8.1, every entry in 8.2, every component in section 10, every surface in section 11, every widget in section 12. The answer is very often `unchanged`, and `unchanged` is a real answer, but it is stated rather than assumed.

The exit condition for the retrofit is that a reader can take any element in this document and say what it does in calm mode without guessing. Where the answer is not written next to the element, 16.2 is the default and the transform applies.

**Two things enforce the color half rather than one, and the structural one is the better half.** The wash brush cannot be called from outside the file that applies the transform, so a wash that skipped it will not compile. Where a use of an accent cannot be closed off that way, a test enumerates every place in `ui/` where an area's stored color becomes a drawable color, each one labeled atmosphere or identity, and fails when a new one appears: the fix is never to update the count, it is to decide which of the two kinds the new one is and route it. The exclusion list in 16.2 is closed, and this is what keeps it closed.

The motion half needs no test of its own beyond the one that reads the flag, 8.5, and a test pins the flag's expression so that the `or` cannot quietly become something else.

### 16.6 The motion audit: every animation in 8.2

**All twenty eight, by number, including the ones already crossfades and the ones calm mode leaves alone.** 8.2 carried twenty six when Addendum 01 arrived and the addendum added items 27 and 28. `unchanged` means the animation behaves in calm mode exactly as 8.3 already made it behave under reduce motion, which is the point of 8.5: calm mode adds no motion level of its own.

The **crossfade** below is 8.3's one path: a 150ms fade with no travel, no scale and no overshoot.

| # | animation | calm mode | phase |
|---|---|---|---|
| 1 | queue promotion | crossfade. No 8dp travel, no wash brightening. The completed title still appears struck through as it fades, because the card has to keep saying which item completed | 2 |
| 2 | press | still responds. The 0.97 scale runs on the crossfade spec | 2 |
| 3 | long press lift | still responds, crossfade spec | 2 |
| 4 | staggered entrance | **does not fire.** The list renders already settled | 3b |
| 5 | sheet entrance | the scrim fades on the crossfade spec. The sheet's own travel is the platform component's, 16.8 | 2 |
| 6 | world transition | crossfade. No scale | 4 |
| 7 | focus ring depletion | **still depletes at 1Hz.** Information, not decoration | 4 |
| 8 | focus glow breathing | **static at 0.92. Disabled, not slowed** | 4 |
| 9 | focus completion bloom | crossfade to the completed state. No collapse, no expanding circle. The check still appears | 4 |
| 10 | Pulse pill fill | crossfade. The acknowledgment still arrives | 6 |
| 11 | Pulse ambient settle | **does not fire.** The 14 day dot row renders complete | 6 |
| 12 | Report reveal | **does not fire.** The ribbon renders drawn | 8 |
| 13 | Momentum dot cascade | **does not fire.** The row renders complete with today's ring already on it | 7 |
| 14 | Momentum number roll | **does not fire.** The three stats render at their values | 7 |
| 15 | tab pill slide | crossfade. The pill still moves to the current tab, because that is the only thing that says where you are | 2 |
| 16 | FAB press | still responds, crossfade spec | 2 |
| 17 | swatch selection | crossfade. The ring and the preview wash still change; the 1.06 scale runs on the crossfade spec | 2 |
| 18 | onboarding iris | crossfade in place of the circular reveal | 10 |
| 19 | tutorial spotlight | the cutout still moves between targets, crossfade spec, because it is what is being pointed at. **The ring pulse holds at 0.35**, which is calm mode's addition and not 8.3's | 10 |
| 20 | undo snackbar | the rise and fall become a fade with no travel. **The depleting line still depletes over its full five seconds** | 2 |
| 21 | swipe actions | **unchanged.** The card tracks the finger 1:1, the background still fades in with the movement and still deepens past the threshold, and the commit is instant rather than a 180ms slide off. 8.3 already said this and calm mode does not go further | 2 |
| 22 | placeholder shimmer | **holds at 4 percent and the repeating animation is never started.** A moving shimmer is motion; a still 4 percent block is a placeholder | 3 |
| 23 | sheet dismiss | the scrim fades on the crossfade spec and a dragged dismiss still tracks the finger. The sheet's own travel is the platform component's, 16.8 | 2 |
| 24 | tab content transition | 150ms rather than 180ms. **A transition, not an entrance**, so 8.4 leaves it alone and calm mode only shortens it | 2 |
| 25 | empty state entrance | the fade shortens to 150ms and **the 150ms delay is kept**, 8.4 | 2 |
| 26 | accept tap on the closing line | crossfade. The label still changes to the confirmation and still settles at reduced prominence | 9b |
| 27 | transition mark reached | **the tick still brightens**, over 150ms rather than 400ms. It is the signal the person switched on, so it is information | 4 |
| 28 | session extended | the arc still grows to its new length and the numeral still rolls, on the crossfade spec, so the change stays visible rather than jumping | 4 |

**One row is specified here and not yet true in the code.** Item 1's calm path currently snaps both titles rather than crossfading them, which loses the struck-through title and with it the answer to "which one did I just finish". The specification above is the requirement; `ui/areas/AreaCard.kt` is where it lands. Nothing else in this table is aspirational.

Eight of the twenty eight now belong to surfaces that phases 6 and later build, phase 4 having built items 6, 7, 8, 9, 27 and 28. They are audited before they arrive rather than when they do, because the point of 16.5 is that a reader can take any element in this document and say what it does in calm mode without guessing.

### 16.7 The color token audit: every token in section 3

**Stated as a number.** `chroma x 0.6` is the transform in 16.2 and means the color's OKLab `a` and `b` axes are both multiplied by 0.6, holding lightness and hue: a 40 percent reduction in colorfulness. `0` means the token is not touched at all.

**3.1 and 3.2, Daylight light and dark.**

| token | calm mode | number |
|---|---|---|
| canvas | unchanged | 0 |
| card | unchanged | 0 |
| raise | unchanged | 0 |
| cardWash | transformed, and the opacity pinned to the low end of its range | chroma x 0.6; opacity 6 to 5 percent light, 8 to 7 percent dark |
| cardWashActive | transformed, and pinned to the low end | chroma x 0.6; opacity 13 to 12 percent light, 16 to 15 percent dark |
| inkPrimary | unchanged | 0 |
| inkSecondary | unchanged | 0 |
| inkTertiary | unchanged | 0 |
| hairline | unchanged | 0 |
| actionBlue | **excluded by name**, 16.2 | 0 |
| positiveGreen | **excluded by name** | 0 |
| positiveInk | **excluded by name** | 0 |
| warnAmber | **excluded by name** | 0 |
| parchment | unchanged | 0 |
| deleteMuted | **excluded by name** | 0 |

**3.3, Contemplative.** **The Focus rows are built, phase 4, and the Onboarding row is built, phase 10; both are measurements.** Onboarding applies the transform to its beat glow and holds the glow's geometry, because a Contemplative surface with no center of light is not calmer, it is a black rectangle, and it takes the low end of the speck range like the Focus and Pulse backdrops. The Pulse and Report rows below belong to phases 6 and 8, which built those surfaces without revisiting this table.

| token | calm mode | number |
|---|---|---|
| deepBlack, surfaceRaised | unchanged | 0 |
| textBright, textDim, textFaint | unchanged | 0 |
| specks | fewer and fainter, one fixed seed as before | 8 to 14 dots at 3 to 6 percent becomes 8 dots at 3 percent |
| Focus gradient, all three stops | transformed, geometry held | chroma x 0.6 |
| Focus ring track | unchanged, it is white | 0 |
| Focus progress stroke | transformed | `#8BA4FF` becomes `#97A8DE` |
| Focus ring tip | transformed, **and the blur removed** | `#B9C8FF` becomes `#C0C9EA`, blur 0 |
| Pulse accent | transformed | `#E8A15C` becomes `#D4A983` |
| Pulse time-of-day tints | not transformed, **not applied**: held at the midday neutral warm black all day | dawn and evening blends 0 percent |
| Report gold | transformed | `#D4B16A` becomes `#C8B48B` |
| Report body text | unchanged | 0 |
| Report gold glows | transformed, and **two centers of light become one**, behind the headline only | 6 to 8 percent becomes 4 percent, count 2 to 1 |
| Onboarding beat glows | transformed | chroma x 0.6 |

**3.4, the 48 area colors.**

| use | calm mode | number |
|---|---|---|
| the 7dp dot | **excluded by name** | 0 |
| the area label text | **excluded by name** | 0 |
| the wash, 5 to 14 percent | transformed, through the one application point | chroma x 0.6 |
| Momentum's 60 percent tile | transformed | chroma x 0.6 |
| the Trail event circle, 12 percent | transformed | chroma x 0.6 |
| the Swap swipe action's face | **no longer takes an accent at all**, phase 13. Its glyph and label are `actionBlue`, which 16.2 excludes, and its ground is that token at 12 percent. 3.4 and 10.3.1 carry why | 0 |
| the color picker's 48 swatches and its selection ring | **not transformed** | 0 |

The swatches are the one place where an area accent is neither atmosphere nor identity: it is a **choice**, and a person picking a color has to see the color they are picking. A desaturated grid would have someone choose one accent and receive another the moment they turned the switch off. The live preview card beside the grid does take the transform, because it is a miniature of the real card and is showing what the card will look like. Recorded per section 15.

**What the transform measures.** Across all 48 area colors it moves WCAG relative luminance by at most **0.0185** on a zero to one scale, which is what "holding lightness" buys and is why no ratio verified in section 13 can be broken by it. Every measurement below is computed in `CalmModeContrastTest` rather than judged by eye, per 16.4.

| measurement | ordinary | calm mode | floor |
|---|---|---|---|
| area label on every ground it can sit on, worst of 48, both worlds | 4.55 | **4.52** | 4.5 |
| item title, `inkPrimary`, on every wash, worst of 48 | 13.92 | 14.21 | 4.5 |
| Trail event glyph inside the 12 percent circle, worst of 48 | 4.14 light, 5.03 dark | 4.14 light, 5.02 dark | 3.0, it is a graphic |
| largest movement calm mode causes in any measured ratio | | 0.33, and it improves it | |

**Desaturating a wash moves contrast in both directions, and that was checked rather than assumed.** In light mode the calm wash is both shallower and less colored, so every area label improves, the largest by 0.33 of a ratio. In dark mode fifteen of the 48 get very slightly worse, the largest by 0.04. Everything still clears 4.5.

**One failure was found by measuring, and it was not calm mode's.** The area label variant was verified against the bare `card` token rather than against the card as drawn, which carries that area's own wash at up to 13 percent in light and 16 in dark. On an in-session card the worst of the 48 measured **3.83 to one**, and 3.95 at the peak of a promotion. Calm mode neither caused it nor fixed it: with the transform applied the same label reads 4.41, better and still failing. The remedy is the one 3.4 already names, adjusting the label variant and never the dot or the wash, applied against the correct ground. Twenty three of the 48 light labels and five of the dark ones moved as a result.

**One finding is pinned rather than fixed.** `inkSecondary` measures 4.50 on a resting card and **4.29 on an in-session one** in light mode. Nothing draws it there today, and raising it would be a change to an ink token for every screen in the app rather than a calm mode change, so it is measured and held in a test instead: the day a caption lands on an in-session card, the build fails rather than the screen quietly missing the floor.

### 16.8 The component audit: everything in `ui/components/`

Every component built in phases 1, 2 and 3, named, with what calm mode does to it. `unchanged` is a real answer here and is the most common one, because most of these components are built from ink and from the four scoped tokens 16.2 excludes.

| component | calm mode |
|---|---|
| `ClarityButton`, five roles | **unchanged.** Every role is an ink token or an excluded one. The 0.97 press scale runs on the crossfade spec |
| `ClarityFab` | **unchanged.** actionBlue is excluded, and so is the fab's shadow, which is tinted with it |
| `ClarityChip` | **unchanged.** The 7dp area dot on an area chip is excluded by name and keeps its true color; the selection color change runs on the crossfade spec |
| `ClarityIcon` and the icon table | **unchanged.** An icon takes ink or an accent from its caller and decides nothing itself |
| `ClaritySheet` | the 42 percent scrim is black and unchanged. **The sheet's own entrance and dismiss are the platform component's and cannot be reached from an app preference**, see below |
| `ClarityTabBar` | **unchanged.** actionBlue is excluded, and the pill still travels, because it is the only thing that says which tab you are on |
| `Sidehead` | **unchanged.** No motion, no accent |
| `TabularNumber` | **unchanged.** No motion, no accent |
| `MorphShape` and `morphingPressShape` | the press morph **does not run** and the resting shape is returned. Already true under 8.3 |
| `ClarityTextField` | **unchanged.** Phase 12b replaced the rule with a well at `raise` deepening to `canvas`, and 16.2's last table row holds all three of those tokens unchanged, because calm mode is not a theme. The color change runs on the crossfade spec |
| `scrollEdgeFade` | **unchanged.** It removes opacity rather than adding color, so there is no accent in it to transform and nothing that moves on its own. A scroll edge is a layering device, 6.1, not atmosphere |
| `clarityClickable` and `clarityCombinedClickable` | **unchanged.** Presses still respond, 16.4 |
| `clarityFocusRing` | **unchanged.** actionBlue, and it appears on the crossfade spec. A keyboard focus ring is not something an accessibility setting should be able to remove |
| `opticalGlyphNudge` | **unchanged.** A fixed offset, not an animation |
| `ReorderState` and `reorderableItem` | **unchanged.** No animation and no color; the drag follows the finger the way a swipe does |
| `clarityShadow` and `ClarityCard` | **unchanged.** Elevation is a separation device, 6.1, not atmosphere |
| `washBrush` and `Modifier.areaWash` | **the transform's one application point.** Every wash in the app arrives here |
| `SwipeableRow` | **unchanged since phase 13.** All three faces are now an excluded token as a ground and the same token, or `positiveInk`, as the ink on it, so nothing on a swipe face takes the transform. The card still tracks the finger and the commit is instant, item 21 |
| `UndoSnackbar` | the rise and fall become a fade with no travel. The depleting line still depletes, item 20 |

**Phase 4 added five drawn things that are not in `ui/components/`**, because they belong to one surface and nothing else may use them. They are audited here so that 16.5 still holds. The **backdrop** keeps its geometry and loses its chroma, and its specks drop to the low end of both ranges, 16.7; the breathing glow holds at 0.92 and the repeating animation is never started, item 8. The **ring** still depletes, item 7, and loses the falloff behind its tip, 16.7. The **completion bloom** becomes the check appearing with no collapse and no expanding circle, item 9. The **Contemplative pill and text action** take the ring's progress color through the same transform and are otherwise unchanged, their press scale running on the crossfade spec like every other press. The **chooser rows** carry no color but the 7dp area dot, which is excluded by name, 16.2.

Three pieces sit in `ui/theme/` rather than `ui/components/` and complete the picture: `CalmMode.kt` holds the transform and the switch; `ClarityMotion.kt` holds the one flag, which calm mode joins with an `or` rather than adding a level beside; and `ClarityEntrance.kt` holds 8.4's rule and is where entrances are removed rather than shortened.

**One component cannot honor calm mode, and it is recorded rather than hidden.** The bottom sheet is the platform `ModalBottomSheet`, which 17.3 puts at step 1 of the platform-first order. Its entrance and dismiss are driven by the platform's own animation, which honors the system animator scale and therefore honors reduce motion, and which exposes no specification an app preference can reach. So with calm mode on and the system setting off, a sheet still travels. 17.2's fourth reason says a component that cannot honor calm mode is a component that cannot ship, and 17.4 says a polish pass never reimplements a working platform component. Both are true and they point opposite ways, which makes this a decision rather than a defect to fix quietly: it is logged in `DECISIONS.md`, the scrim and everything drawn inside the sheet do honor calm mode, and it is revisited if the platform grows a hook or if the sheet becomes the one thing a calm mode user complains about.

---

## 17. Platform first, and what this app builds by hand

**A standing rule, in force from the day it was recorded rather than at a phase.** Addendum 01 Step 3. `MASTER_BUILD_PROMPT.md` carries it for behavior and dependencies. This section is the visual and interaction half, and it governs every component decision in section 10 and every surface in section 11.

### 17.1 The order

1. The official Material 3 Expressive component, unmodified
2. The official component, themed with the tokens in sections 3, 5 and 6
3. The official component, extended
4. **Custom**

Stop at the first step that fits. **Reaching step 4 is a legitimate and expected outcome**, not a failure and not evidence that the earlier steps were done badly.

The platform comes first because platform components arrive with correct accessibility, RTL, dynamic type, predictive back and motion physics already handled; because they are hardware accelerated and tested at a scale one person cannot match; and because they keep the codebase small enough for one person to maintain. Every one of those is a reason this document's rules survive contact with a device rather than being true only on paper.

### 17.2 The four reasons to go custom

- **No platform equivalent exists.** Six of them, 17.3
- **The platform component carries meaning this app rejects.** Anything implying achievement, scoring, celebration or progress toward a target is wrong here regardless of how well it is built. A determinate progress indicator aimed at a goal is the common case, and section 14 now has two reasons for refusing it
- **The platform component fights a rule in this document.** If using it would put two separation devices on one element, 6.1, a colored edge treatment on anything, section 14, or an all-caps label, 5.3, the rule wins
- **The platform component is worse for this audience.** Motion or saturation that cannot be tamed through theming is a reason to build something calmer. Since section 16 exists, a component that cannot honor calm mode is a component that cannot ship

**Not a reason:** preferring the look of something drawn by hand. That instinct belongs in the polish pass, 17.4.

### 17.3 What this app builds by hand, and why

Six components have no platform equivalent. Reaching step 4 for each of them is the rule working, not a conflict with it, and none of them should be revisited as though the analysis had been skipped.

| component | where | why no platform component fits |
|---|---|---|
| the depleting focus ring | section 11, 11.3 | a determinate progress indicator counts up toward a goal and is styled to say so. This counts down, holds a 64sp numeral inside a 240dp ring, carries the transition mark on its track, and has to read as a shape before it reads as a number |
| the week ribbon | 11.1 | seven marks whose height and opacity encode one day each against the week's busiest, with no axes, no gridlines, no values and no container. Every charting component supplies exactly the parts this one deliberately omits |
| the 14 day rhythm dot row | section 11, 12.2 | a row of independent states, not a progress or step indicator. A stepper implies sequence and completion. These are fourteen days and a gap is not a failure |
| the area color wash | 3.1, 10.3 | a wash pooled toward a corner chosen by hashing the area id is a background treatment, not a container. Nothing in the platform pools color |
| the two-stage color picker | 10.9 | the platform ships no color picker, and this one has a mood strip of six discrete slivers per pill and a live miniature of the real card above it. It is the premium moment of the app |
| the tutorial spotlight | section 11, item 19 | a scrim with an animated feathered cutout that moves between targets. It is neither a dialog nor a tooltip, and building it out of either produces a worse version of both |

**Everything else starts at step 1.** Sheets, buttons, chips, switches, text fields, the snackbar, ripples, predictive back, dynamic type and the system back preview are platform work, themed. Where this document states a dimension for one of them, the dimension is reached through theming, 17.4.

### 17.4 The polish layer sits on top, not instead

After the Material 3 Expressive layer works and has been verified on the device, a separate pass adds the distinctive Kamsiob look through theming, tokens, typography, spacing and considered motion choices.

The distinction that matters: **17.2 governs what to build when the platform has no answer. 17.4 governs what not to rebuild when it does.** A polish pass never reimplements a working platform component in order to change how it looks, because theming almost always gets there. If a polish idea genuinely cannot be reached through theming, it is raised as a decision and recorded, not done quietly.

### 17.5 Custom components inherit the platform's obligations

Anything built by hand handles, to the standard the platform component would have met: content descriptions and a sensible TalkBack order, section 13; RTL mirroring; dynamic type to 200 percent without clipping, section 13; reduce motion, 8.3; and calm mode, section 16. **A custom component that skips these is not a custom component. It is a regression with a nicer shape.**

That work is the real cost of going custom, and it is the reason the platform comes first whenever it fits.

**RTL is stated here because this document has stated it nowhere else.** Mirroring applies to every layout with a leading and a trailing edge: the swipe directions in 10.3.1 mirror, so the action revealed at the start edge stays at the start edge; the sidehead hairline in 10.12 runs to the trailing edge whichever edge that is; the chevron in 10.11 points toward the trailing edge; the week ribbon in 11.1 and the dot row in item 13 fill in reading order, not left to right. The 7dp dot is leading, not left.

### 17.6 Record the decision either way

When step 4 is reached for something not already listed in 17.3, one line goes in `DECISIONS.md`: what was checked, and which of the four reasons in 17.2 applied. One line is enough. This exists so that a later session knows the platform was considered and does not redo the analysis, and not to discourage building what the app needs.

### 17.7 Verify versions at build time

No version named in this document, in the master prompt or in the addendum is to be trusted. Check the current stable release and the current recommended integration path before integrating, and record what was chosen and why. This document deliberately names Material 3 Expressive, Glance, the promoted-notification progress style and the quick settings tile service **by role and not by version**, for exactly that reason.

