# Clarity Now: Visual Direction

**Status.** A direction, not a specification. `design-v3.md` is still the authority.
Section 2 below is a list of **proposed amendments to it**, written for the owner to
accept, reject or modify. Nothing in `design-v3.md` has been edited. When an amendment
is accepted it moves into `design-v3.md` in that document's own voice and this file
records that it did.

**Scope.** Visual and interaction only. Nothing here changes what the app does, what it
stores, what the engine says, or what any sentence means. Every proposal is a token, a
dimension, a curve, a shape or a layout.

**Written from.** Seven parallel research passes against the real captures from a Pixel
8 at v0.11, the whole of `design-v3.md`, and the shipped source. Every measurement below
was recomputed here rather than quoted: `L*` values are CIE lightness, ratios are WCAG
relative luminance, and hex values that are proposed have been checked against section
13's floors before being written down.

---

## 0. The thesis

> **On every screen the loudest thing is the person's own life, and everything the app
> says about itself is quieter than that.**

The app is not flat because it is restrained. It is flat because its emphasis is pointed
at itself. On the flagship screen the largest glyphs are the word `Areas`, which the tab
bar already says, at a size nine points above the string `design-v3.md` 10.3 calls the
most important one on the screen. The most saturated object is the button that adds
work, which outweighs the identity of every area the person has by a factor above twenty.
On Momentum the two largest and most saturated objects encode one boolean each while the
quantities they sit next to are drawn in 5dp dots. The engine's sentence, which is the
one thing in this product no competitor can write, is set in the sans on the one screen
people open daily and in the serif everywhere they do not.

Reverse that ordering and the app does not become busier. It becomes legible. The current
Areas screen carries nineteen readable or tappable marks in six type sizes, five of which
fall inside a nine point band, so nothing wins and the reader resolves the hierarchy by
hand on every single open. **For an audience defined by executive dysfunction, that manual
resolution is the tax. A large object is not the tax; the absence of one is.** Google's
own research on expressive hierarchy is the strongest external evidence available for
exactly this, and the finding that matters is not the preference number: it is that
expressive hierarchy erased the age related gap in fixation time, meaning the people who
were slowest to find things got the largest benefit. That is a cognitive load result,
not a taste result, and it makes this overhaul an accessibility intervention that happens
to look better.

So the direction is **amplitude, not quantity**. Raise the amplitude of what is already on
the screen. Lower the count of things competing with it. Take the hierarchy half of
Material 3 Expressive at full strength, take its animacy half only where a finger is
already on the glass, and refuse its ambience half entirely, which the app is uniquely
safe to do because calm mode and reduce motion already run off one boolean.

The thesis decides arguments. Can the card be bigger? Yes, it holds their words. Can the
FAB be bigger? No, it is the app talking. Should `Areas` be 30sp? No, the tab bar already
said it. Can Momentum's tiles be sized by data? Yes, that is their life carrying the
emphasis. Can we add ambient motion? No, it says nothing about them. Can the ground have
a light? Yes, if it points at the content.

---

## 1. The diagnosis, measured

Six things are true, and they are the whole of it.

**1. The light world is 7.19 `L*` wide.** canvas `#E6E6EC` at 91.45, raise `#F4F3F0` at
95.84, card `#FCFBF9` at 98.64. Every surface in the Daylight light world lives in the
top 8.6 percent of the lightness axis. Card against canvas is **1.202:1**. The strongest
boundary anywhere on a card's perimeter, including its shadow, is about **1.39:1**,
against 16.7's own floor of 3:1 for a graphic that carries meaning. For comparison the
Focus gradient runs 19.54 `L*` at its center to 6.48 at its edge: **13.06 `L*` of light
modeling on one screen, nearly twice the entire light world's range.**

**2. The wash spends the ladder it is drawn on.** `washBrush` runs the accent to zero
over `hypot(w, h) * 0.92`, which across a 371 by 86dp card is so wide it is a flat film.
So the card does not have a pool of color at one corner, it has a uniform tint over its
whole area, and that tint costs it 4 to 6 `L*`. A card at 13 percent measures between
92.0 and 93.7 `L*` depending on the hue. `raise` is 95.84. **The card rank and the chrome
rank are within two points of each other on every card of every active area**, which is
why two areas render as two indistinct pale bars.

**3. The scale contrast is inverted and the optical size axis is driven backwards.**
`displayTitle` is 30sp with `opsz` 48, a 1.6x over drive that thins Newsreader's hairlines
at exactly the size where the screen needs an anchor. `itemTitle` is 21sp. Scale contrast
on Areas is 1.43:1; on the Report, which everybody agrees is the best screen in the app,
it is 2.35:1. Newsreader ships seven weights and the app uses one.

**4. There is no composition layer.** `design-v3.md` runs to 39,103 words. Section 6.1
governs how one element separates from its ground. Section 11 is titled Surface Art
Direction and gives the Report 4,896 words and **Areas twenty three**. There is no rule
anywhere about a page: no anchor, no scale contrast floor, no second alignment position,
no statement of what a screen does when its list is short. Areas was never designed. It
was assembled from correctly specified components with no instruction about the page they
sit on, and that is precisely what cleared space looks like from the outside.

**5. The Expressive layer was built and never called.**
`ui/components/ExpressiveShapes.kt` implements `MorphShape`, `rememberMorph` and
`morphingPressShape`, correctly guarded for reduce motion, with a header naming its
intended call sites. It has **zero call sites in the entire app**.
`androidx.graphics.shapes` ships in the APK to power a file nothing imports. Meanwhile
`ClarityMotion` already carries Expressive's own spring constants: `springStandard` is
`0.8f / 380f`, which is `SpringDefaultSpatial` byte for byte, and `effects()` and
`effectsFast()` are the effects tokens exactly. **The motion physics were never the
problem.** Material component imports across the whole app come to `Text`,
`ModalBottomSheet`, `Switch`, `AlertDialog`, `TextButton` and `MaterialTheme`, and nothing
else. There is no component layer to inherit Expressive behavior from.

**6. Four visible defects cost more perceived quality than any token here.** At the app's
largest text size, `areas_largest.png` draws `Session length` and `25 minutes` straight
through the system clock and battery icon. The floating tab bar occludes live content on
the Report, the Trail and Settings at close to full opacity. The tutorial's `Skip` is
drawn on top of the settings gear. The person who set their phone to maximum text is
exactly the person this app claims to exist for, and the app visibly did not check.

**Are the rules the cause?** Mostly no, and saying so precisely matters. There are
roughly ten ways an interface acquires visual interest. Exactly one is genuinely
prohibited here, ambient motion, and that prohibition is correct. One more, the accent's
area, is constrained. The other eight, scale contrast, weight contrast, optical size,
shape variety, depth and material, ground treatment, containment, and touch response, are
all fully permitted and were never used. **The cause is a missing rule, not a present
one**, plus five specification defects that let the missing rule go unnoticed. Section 2
fixes the five and adds the missing one.

---

## 2. Amendments to `design-v3.md`

Eight amendments and three additions. Each states the rule as it stands, what it was
protecting, why it now costs more than it buys, the replacement, and the test that holds
it.

### A1. 6.1 step 2 has no unit

**As it stands.**

> 2. **A background lightness shift of 3 to 5 percent**, or the area color wash.

**What it protected.** Escalation discipline. Stop at the first device that reads, and do
not reach for a heavier one out of habit. That instinct is right and stays.

**What it now costs.** Three to five percent **of what**. Of the hex value, of `L*`, of
relative luminance, of a contrast ratio? The unit is unstated, so the rule can be
satisfied by a step that measures 1.202:1 and every reading is defensible. That is why
phase 3c's correction was directionally right and an order of magnitude short: it moved
canvas 2.5 `L*` and bought 0.076 of contrast ratio, and nobody could tell from the rule
whether that was enough. A rule with no denominator is a suggestion.

**Replacement.**

> A background lightness step is stated in `L*` and verified as a contrast ratio, and it
> is measured where the surface is darkest rather than on the bare token.
>
> - **Content against ground:** at least **11 `L*`** and at least **1.35:1**, measured on
>   the card's unwashed majority.
> - **Chrome against ground:** at least **5 `L*`** and at least **1.18:1**.
> - **A washed card's pooled corner** never falls below **1.15:1** against the ground it
>   sits on, which is what keeps a wash from spending the ladder it is drawn on.
> - **Adjacent ranks** never sit closer than **4.5 `L*`**.

**The test.** `SurfaceLadderTest` holds all four numbers and fails the build on any one of
them. It computes the pooled corner by compositing each of the 48 area colors at the
deepest permitted opacity over `card`, rather than asserting on the token.

### A2. 6.1's one device rule is a cap on containment being read as a cap on emphasis

**As it stands.**

> **Every element carries exactly one separation device.** [...] Never a hairline and a
> shadow on the same element. [...] A test walks the component set and fails the build on
> any element declaring two.

**What it protected.** The hairline plus diffuse shadow pair, which is genuinely on every
2026 tell list and is genuinely redundant: two devices making the same claim, an edge and
a float, argued at once. That stays banned and the test stays.

**What it now costs.** The rule generalized from **no two devices making the same claim**
to **no two devices**, and those are different statements. It blocks the single cheapest
premium treatment that exists, an **inner top edge light** on a lifted surface, which is
not a second device: it is the same light source described on the other side of the same
edge. Read literally it also forbids a container being both larger and rounder because it
is more important, which is the whole of Material's containment story. And the rule
already concedes the principle in its own next paragraph when it calls the light mode
card's paired shadow "one device expressed as a paired shadow, not two devices."

The cost is not theoretical. In the **dark world 6.1 permits no shadows at all**, so the
lightness ladder is the only device dark has, and dark's card measures **1.150:1** against
its canvas, worse than light's. An edge light is the only depth tool available there, and
the rule as written forbids it.

**Replacement.**

> Every element makes exactly **one separation claim**, and may render that claim with as
> many strokes as the claim requires, provided every stroke describes the same physical
> situation.
>
> A drop shadow and an inner top edge light are one claim, `a lit solid above the page`,
> because they are one light on two sides of one edge. A container's tone, its radius and
> its size are one claim, `containment`, expressed in three properties. A hairline and a
> shadow are **two** claims, an edge and a float, and stay forbidden. A border on a card
> that has a wash is still two claims.
>
> The order of escalation in this section is unchanged. Stop at the first claim that
> reads.

**The test.** The existing component walk changes its vocabulary from device to claim.
Every surface composable declares its claim in source as one of `Whitespace`,
`Containment`, `LitSolid` or `Edge`, and the test fails on any element declaring two, on
any co-occurrence of `Edge` and `LitSolid`, and on any element declaring none.

### A3. Section 14 and 3.4 ban a shape and are being read as banning a size

**As it stands.**

> **No colored stripe, bar or edge treatment on any element, ever.** (14)
>
> Color appears only as: a 7dp dot, a wash between 3 and 16 percent, a 60 percent tile in
> Momentum, and the area label text. **Never as a stripe, bar, edge, border or filled
> block.** (3.4)

**What it protected.** The colored 3 to 4dp border down one side of a card. It is the most
corroborated machine design tell of this era, this document names it as such, and 15.3
refuses it three separate ways. **All of that stays. Nothing below weakens it.**

**What it now costs.** Two things.

First, it generalized from a banned **shape attached to a component's boundary** to a ban
on the accent having any **area**. On the Areas screen the total saturated area belonging
to the person's own areas is two 7dp dots and two 13sp labels, roughly 77 square dp, while
the FAB with its 40 percent blue glow comes to roughly 1,810. The identity of everything
the app is about is more than twenty times quieter than the button that adds one.

Second, and this is the argument that should settle it: **the rule as literally written is
already false of the shipped build, and every exception is among the best things in the
app.** The Report's week ribbon is seven gold vertical bars whose height encodes a value.
The Focus ring is a stroked arc. Momentum's focus strip is seven filled rectangles. Each
survives on the rule's evident intent and dies on its text. The next agent reading the
text will refuse a data mark on it, and the intent is nowhere written down.

**Replacement, two clauses.**

> No colored stripe, bar or edge applied to the boundary of a component. The prohibition
> is on the shape and its attachment, not on the accent's area.
>
> A mark whose **size, length or position encodes a value** is a data mark, not a
> decoration. It is permitted; it stands free of any card, row or header rather than
> attaching to one; and it carries a caption stating what it shows, per 13. The week
> ribbon, the Pulse rhythm row and the focus ring are already this and always were.

**What this legalizes.** Momentum's area tiles get their share as a dimension, and the
`Area balance` percentage list below them collapses into their captions.

**What it does not legalize.** An accent stripe on the area card. The first clause still
refuses it by name and 15.3 still refuses it three ways.

**The test.** A source scan enumerates every place in `ui/` where an area's stored color
becomes a drawable color, which already exists for calm mode, and gains a second label
beside `atmosphere` and `identity`: `datum`. A `datum` use must be able to name the value
its dimension carries, and a new colored fill that cannot is a build failure.

### A4. 15.3's last entry has been discharged and does not know it

**As it stands.**

> **Trying to make the cards pop.** [...] when an element does not separate, measure
> whether the ground it sits on has any room left before reaching for the element

**What it protected.** Hours spent lifting a card when `card` was pure white and there was
nowhere to lift it to. Correct at the time, and the general instruction stays.

**What it now costs.** Its premise is spent. Phase 3c performed exactly the measurement
this entry asks for, moved the ground, and the ladder still fails at 1.202:1. The entry
now blocks the correct next move, which is the ground **again** under A1 plus the element
under A2.

**Replacement, appended rather than substituted.**

> Once the ground has been measured and the ladder still fails the ratios 6.1 states, the
> element is the correct place to work, and the record of that measurement is what
> licenses it. Phase 3c performed it and this direction performs it again. This entry is
> discharged for the Daylight content card and stands everywhere else.

### A5. 6.1's scroll edge criterion names the wrong landmark

**As it stands.**

> **Scroll edges.** [...] fully gone at the screen edge, fully present 12dp past the
> status bar and 16dp above the tab bar's top edge.

**What it protected.** Content ending at a hard line. The mechanism in `ScrollEdge.kt` is
correct and better than painting the ground, because `BlendMode.DstOut` on the content's
own alpha works over the Contemplative gradients too. The mechanism is not what is wrong.

**What it now costs.** The criterion is anchored to the **screen edge**, and the thing
content must not collide with is the **status bar's glyphs**, which sit roughly 10 to 26dp
below that edge, inside the band, where content is still 45 to 80 percent present. So the
specification guarantees the collision. At 1.0x it never shows because content happens to
start lower. At 200 percent a settings row is three lines tall, crosses the band before the
ramp finishes, and draws through the clock. `areas_largest.png` is that, exactly. The same
arithmetic at the other end is why `record yet.` is legible under the tab bar on the
Report and why a Trail row and a day header are legible under it there.

**Replacement.**

> Content is fully removed across **the whole of the status bar inset** and **the whole of
> the tab bar's occupied band**, and returns to full presence over a lead below and above
> each. The removal holds at full for the inset's own height before the ramp begins.
>
> **The lead scales with the combined text size**, because a row three lines tall crosses
> a fixed band before a fixed ramp completes. `ScrollEdge.underTheClock` and
> `ScrollEdge.aboveTheBar` become functions of `spacingScale()`.

**The test.** A screenshot test at 1.0x and at the 200 percent cap asserts that no content
pixel inside the status bar inset or the tab bar band has non zero alpha. This is the one
test in this document that would have caught a shipped defect on its own.

### A6. 15.3's blur refusal rests on a claim that has been falsified

**As it stands.**

> **A blurred or translucent tab bar, header or sheet.** [...] reaching for the blur
> because it looks more modern is exactly the move the entry describes

**What it protected.** Glassmorphism as decoration. But the industry position has moved:
glassmorphism as such has fallen off the current tell lists and the half that survives is
the neon glow that used to travel with it. A refusal resting on a premise that has since
been falsified is exactly the statement this project's own rules say must be corrected in
place rather than left standing.

**What it now costs.** Measured on `focus_end.png`: behind the 42 percent scrim, the
weekly banner headline still reads about **5.6:1** against its ground. Section 13's
readability floor is 4.5:1. **The app is placing a fully readable sentence about the
person's own week directly behind the sheet they are trying to read.** A scrim cannot fix
this and never could: it multiplies ink and ground by the same factor, so the ratio, which
is the whole of legibility, barely moves. To push the background under 3:1 the scrim would
have to pass roughly 70 percent, at which point the sheet has no context behind it, only a
black rectangle. **A scrim can make text dim. Only a blur can stop text being text.** That
is a layering problem, which is the exact case 15.1's own wording carves out.

**Replacement.**

> A blur is refused on any surface over content that is moving, on frame cost, and the
> scroll edge fade is the answer there. A **one shot** blur of a **static** backdrop under
> a modal surface is permitted, because a scrim reduces ink and ground by the same factor
> and therefore cannot suppress reading. Refused as decoration on a bar, a header or a
> card in every case, and never accompanied by a rim light, a specular highlight or a
> gyroscope response.

**Why it is affordable.** The content behind a modal sheet is static for as long as the
sheet is open, so this is one `RenderEffect` pass at open and a texture blit thereafter,
not a per frame resample. `RenderEffect` is API 31 and this app's `minSdk` is 31, so there
is no branch and no dead code path. The project has already accepted an offscreen
composite on every scrolling surface for the scroll edge fade, so it cannot refuse this one
on cost grounds.

**The test.** The existing source gate changes from `nothing in ui/ may reach for a blur`
to `nothing in ui/ may reach for a blur outside ClaritySheet.kt`, and a unit test asserts
the radius reaches 10dp, which is past the collapse threshold for 16sp text at this
density.

### A7. 5.1's serif optical size is stated wrong

**As it stands.**

> **Display role**, `opsz 44 to 68`, weight 400.

**What it now costs.** The `opsz` axis is denominated in the intended rendered size, and
Compose applies optical sizing from the text size automatically unless an explicit value
overrides it, which is what this document told the build to do. Setting 30sp with `opsz`
48 asks for the letterforms of a 48 point headline: thinner hairlines, higher stroke
contrast, a lower x height. That is why `Areas`, `Trail` and `Settings` look spindly and
fragile in every capture, and why the Momentum figures are the largest and faintest glyphs
on their screen. Four of the five serif roles are over driven, at 1.70x, 1.60x, 1.31x and
1.42x.

**Replacement.**

> **`opsz` is set to the role's own sp size.** A deliberate over drive of at most **1.2x**
> is permitted at 34sp and above and must be recorded with its reason. Serif display and
> deck roles carry **weight 450 to 500**; text roles stay at 400. Never below 400, and
> never more than 1.2x, because Newsreader's display cut thins its hairlines and a thinned
> hairline at phone density is a fragile title, not an elegant one.

**The test.** A unit test asserts, for every serif role, that the declared `opticalSizing`
is between 1.0x and 1.2x of the declared `fontSize`.

### A8. 6.1's elevation table and its FAB glow

**As it stands.**

> - Light mode card: `y 1dp blur 3dp black 4%` plus `y 6dp blur 20dp black 5%`
> - FAB: `y 5dp blur 16dp actionBlue 40%`

**What it now costs.** Two problems, both fixable without touching the one claim rule.
Pure black on a cool violet grey ground desaturates into a smudge rather than reading as
shade. And the two layers are not a series: 1dp offset with 3dp blur is too diffuse to
define an edge, and 6dp offset with 20dp blur leaks above the card almost as much as
below, so a card has **no contact edge**, which is most of what the eye reads as a real
object. Separately, the FAB's colored glow is the single loudest thing on the flagship
screen and it is chrome.

**Replacement.** See 3.3 below for the values. In summary: three hue matched layers on a
doubling series with negative spread, plus an inner top edge light under A2, and the FAB's
glow replaced by the same neutral stack one rank deeper.

### Addition N1. A new section 6.2, Composition

This is the largest single gap in the document and it is not an amendment, because there
is nothing to amend.

> **6.2 Composition.**
>
> Every screen names one **anchor**: the element that must be seen first. The anchor is
> always the person's own words or their own data. It is never a screen title, never a
> control, never chrome.
>
> The anchor is at least **1.6x** the size of the largest chrome or commentary text on the
> surface, and at least **1.3x** its own peers.
>
> **A screen title is chrome.** Where the floating tab bar already names the destination,
> the screen does not repeat it at display size. Pushed screens, which the bar does not
> name, keep their titles.
>
> A surface has at most **two horizontal alignment positions**, and both are stated.
>
> **Unlike things sit further apart than like things.** Like to like is the 12dp card
> rhythm. Section to section is 28dp. A header block to the list beneath it is 36dp.
>
> **A column that runs out closes with a stated end** rather than trailing into empty
> ground. The dark Trail already does this and Areas does not.
>
> Anchors, stated: Areas is the topmost card's item title. Momentum is the engine
> sentence. The Report is the headline. Focus is the ring. The Trail is the day header.
> The Pulse is the observation. Settings and About have no anchor, and that is correct,
> because neither is about the person.

### Addition N2. Section 11 gets a floor, and 15.2's gate gets teeth

> **11.0.** Every surface's art direction states four things: its anchor, its scale
> contrast ratio, its ground treatment, and its behavior when its content is short. A
> surface with fewer than four does not ship.
>
> **15.2, appended.** The anti slop pass runs against **screenshots of the built app**, not
> only against proposals. A stat banner shipped on Momentum while `Stat banners` sat on
> 15.1, because the list has only ever been pointed at things that were about to be built.

### Addition N3. Section 15.4, the permitted moves

15.3 is explicitly written for the session that reads a design audit and reaches for the
nearest premium looking thing, and it is a one way ratchet: every entry pairs a proposed
fix with a prohibition, and there is no list of moves that are expected. Over thirteen
phases, a review process that can only say no produced a build that only says no. That is
the aggregate this direction was asked to test, and this is the form the aggregate took.

> **15.4 The permitted moves.** When a surface does not read, these are the answers to
> reach for first, in this order, before proposing anything not on the list.
>
> 1. Raise the anchor's size.
> 2. Raise its weight.
> 3. Correct its optical size.
> 4. Widen the ground's ladder and re-measure everything against it.
> 5. Give the container a rank in shape as well as tone.
> 6. Give the ground a light with a stated center.
> 7. Make an existing mark's dimension carry the value it already prints.
> 8. Wire the press morph that is already written.
>
> None of these appears on 15.1. **A refusal without an alternative from this list is not
> a finished review.**

---

## 3. The system

### 3.1 The two worlds, restated in material terms

Nothing about the world split changes. What changes is that the Daylight world gets the
same **method** the Contemplative world already has, and not its colors.

The Contemplative world works because it has a light with a stated center, a glowing
element whose glow is the falloff of that light, and a ground with texture. `design-v3.md`
3.3 even records why the Focus gradient's center sits at 0.42 down: "because that is where
the ring is." **That is the only sentence in 39,103 words where a visual decision is
justified by where the content is, and it is why the best screen in the app is the best
screen in the app.**

The Daylight world gets a ground with a center of light, a card with a real edge, and a
wash that is a pool rather than a film. It stays light, it stays cool grey under warm
paper, and it takes on no indigo, no gold and no amber.

**Open choice, recorded.** The obvious answer to a flat light ground is a vertical
gradient, top light to bottom dark, which is what almost every app ships. **Chosen
instead: a radial with a stated center**, at 0.5 across and 0.32 down, which is where the
content is. A vertical gradient is a background. A radial with a center is a room, and it
is the app's own construction rather than the industry's.

### 3.2 The surface ladder

Every value below was computed, not estimated. `card` does not move. `canvas` moves in
light, `card` and `raise` move in dark, and `parchment` is retired.

**Daylight, light.**

| token | current | proposed | `L*` | against canvas |
|---|---|---|---|---|
| canvas | `#E6E6EC` | **`#D6D6DB`** | 85.76 | ground |
| raise | `#F4F3F0` | **`#EBEAE6`** | 92.67 | **1.203:1** |
| card | `#FCFBF9` | `#FCFBF9` | 98.64 | **1.400:1** |
| parchment | `#EFEEE2` | **retired** | | |

Ladder span goes from 7.19 to **12.88 `L*`**, and the steps are 6.91 and 5.97, which is
close to even. Card against canvas goes from 1.202:1 to 1.400:1, which is the difference
between a tint and an object.

`inkSecondary` at 64 percent measures **4.59:1** on the new canvas, which clears section
13's floor of 4.5 without moving. `inkPrimary` measures 12.33:1. Those are the two
readings that decide whether this canvas is affordable, and it is. The next step down,
`#D0D0D7`, would take `inkSecondary` to 4.47 and fail, so **`#D6D6DB` is the deepest
ground this ladder supports without moving an ink token**, and that is why it is the value.

**Honest cost, stated as 3.1 already states it.** Changing a background is not a background
change, it is a change to every ratio measured against it. The full 16.7 audit re-runs. The
readings that are expected to move are every token measured against the canvas, and every
one of them moves in the safe direction except `inkSecondary`, which is why it was checked
first.

**Daylight, dark.** Dark is worse than light, not better: card against canvas measures
**1.150:1** today, and 6.1 gives dark no shadows, so the ladder is not one of dark's
devices, it is the only one. Dark cannot be deepened because section 14 bans pure black and
because the Contemplative floor sits only 0.97 `L*` below it. So dark is bought upward.

| token | current | proposed | `L*` | against canvas |
|---|---|---|---|---|
| canvas | `#0E0E13` | `#0E0E13`, held | 4.11 | ground |
| raise | `#18181F` | **`#1C1C24`** | 10.59 | **1.138:1** |
| card | `#1D1D25` | **`#262630`** | 15.55 | **1.285:1** |
| parchment | `#211F16` | **retired** | | |

`inkSecondary` at 62 percent measures 5.91:1 on the new dark card, comfortably clear. Dark
does not reach light's 1.35:1 and cannot, which is why **the inner top edge light in 3.3 is
not optional in dark. It is the second half of dark's only claim.**

**Contemplative.** Unchanged. `deepBlack`, `surfaceRaised`, `textBright`, `textDim`,
`textFaint` and the specks all stay exactly as they are. The three surfaces already work
and this direction takes nothing from them.

**Why `parchment` is retired.** Its only call site is the Areas weekly banner, and 4.1
below dissolves that banner into a deck on the ground. A cream block under a warm serif
title on a cool ground is also the single most current 2026 tell, so the token's only use
was also the app's only instance of it. Retiring it means removing it, not leaving it
defined and unused: a dead token is a token a later session will find a use for.

### 3.3 The light model

**One light, above and slightly behind the reader.** Everything below describes that one
light and nothing contradicts it. Under A2 this is one claim, `LitSolid`.

**The drop shadow, light world.** Three layers, hue matched to the ground, on a doubling
series, with negative spread so the wide layer stops haloing sideways.

| layer | offset y | blur | spread | color | alpha |
|---|---|---|---|---|---|
| contact | 1dp | 2dp | -1dp | `#33333E` | 10 percent |
| form | 4dp | 10dp | -2dp | `#33333E` | 8 percent |
| ambient | 12dp | 28dp | -4dp | `#33333E` | 6 percent |

`#33333E` is the canvas hue taken to `L*` 21 at low chroma. Pure black on a cool ground
reads as dirt; a hue matched shadow reads as shade. `androidx.compose.ui.graphics.shadow.Shadow`
carries `radius`, `spread`, `offset`, `color`, `alpha`, `brush` and `blendMode` in this
project's Compose BOM, verified against the shipped jar, so `ShadowLayer` gains a `spread`
field and `clarityShadow` needs no other change. The **contact layer is the one that
matters**: it is what the eye reads as an object, and it is the layer the current pair does
not have.

**The inner edge light.** Applied after `.background()`, using `Modifier.innerShadow`,
which is present in this project's Compose BOM and verified against the shipped jar.

| world | top edge | bottom edge |
|---|---|---|
| light | white, 85 percent alpha, radius 1.5dp, offset y +1dp | `#33333E`, 10 percent, radius 2dp, offset y -1dp |
| dark | white, **7 percent**, radius 1dp, offset y +1dp | none |

In dark this is the only depth tool available, and it is worth stating what it buys: white
at 7 percent on the new dark card lands the top edge at `L*` 21.7 against the card's 15.55,
a six point highlight that is clearly visible and costs nothing a lightness step costs.
**A dark card then catches light on its top edge the way a real raised surface does.**

**The ground light.** A single static radial on the Daylight canvas, centered at 0.5 across
and **0.32 down**, reaching 0.85 of the surface diagonal, amplitude **2.5 percent lightness**.
In light that is roughly `#DBDBE0` at the center falling to `#D4D4D9` at the edge. In dark it
is proportionally smaller and warmer. One draw call, no per frame work, no accent, no hue
shift, no content dependence.

**This is the fix for the empty half of Areas**, and it is worth being precise about why.
Emptiness on a flat field reads as vacancy. Emptiness on a field with a center reads as
room. The lower third of the screen falling away from a center of light is the oldest
editorial device there is.

**Banding risk, honestly.** A 2.5 percent amplitude stretched over 2400px at 8 bits per
channel can band. Skia already dithers gradients and a radial with a 0.85 diagonal reach is
short ranged, so it probably will not, but **this is the one item in this document that must
be looked at on the real device in a dark room before it is believed.** If it bands, the fix
is a tiled 128 by 128 8 bit alpha noise bitmap at 2 to 3 percent drawn through
`ShaderBrush(ImageShader(..., TileMode.Repeated, TileMode.Repeated))`, one texture read.
**Do not reach for AGSL.** `RuntimeShader` is API 33 and this app's floor is 31, so a shader
needs a branch and a fallback while a bitmap needs neither.

**Grain on Daylight surfaces is refused.** Paper texture on a white card is a current tell
and the noise above, if it is needed at all, is a dither and not a texture.

### 3.4 Color, and how it derives from content

The 48 hue palette, the eight moods, the walk starting at Berry, the four never assigned
automatically, and the calm mode exclusions are all correct and none of them changes.

**What changes is the wash, and only its geometry.**

| | current | proposed |
|---|---|---|
| radius | `hypot(w, h) * 0.92` | **`hypot(w, h) * 0.55`** |
| light, idle | 5 to 7 percent | **10 percent** |
| light, in session | 12 to 13 percent | **13 percent**, unchanged |
| dark, idle | 7 to 9 percent | **12 percent** |
| dark, in session | 15 to 16 percent | **16 percent**, unchanged |

**Total colored area falls. Perceived color rises. The ladder stops being spent.** At 0.92
of the diagonal the wash is a flat film over the whole card, so the entire card sits at the
dipped value and collides with `raise`. At 0.55 it is a pool at one corner with a real
falloff, so the card's far side holds the full 98.64 `L*` and the strongest boundary is the
full 1.400:1. The card acquires an internal light direction, which is the thing it has
never had.

**The in session opacities are deliberately not raised**, and this is the frugal half of
the proposal: 13 percent in light and 16 in dark are the exact grounds 16.7 already measured
all 48 area label variants against, so **the entire label audit stays valid**. The extra
presence is bought from the radius, which costs nothing to verify.

**A composited check, because it is the reading that matters.** At 13 percent over the card,
the pooled corner measures between 1.18:1 and 1.24:1 against the new canvas across the
sampled hues, clearing A1's 1.15 floor on every one. On the current canvas the same corners
measure 1.12 to 1.18. So A1's new floor is met **because** the ground moved, and would not
have been met without it.

**Refused: deriving the Areas ground tint from the active area's accent.** Premium apps do
this and it would look good. It is a fifth form of the accent, 3.4 closes the list at four,
and worse, it makes the ground move when the data moves. 3.3 already gives the reason in the
Focus gradient's own note: a light that moved when the content changed would make the room
feel like it had moved.

**One new permitted form, under A3: the data mark.** Momentum's area tile keeps its 60
percent fill and gains a height that carries the area's share of the fortnight. That is the
third form doing its job rather than a fifth form arriving.

### 3.5 Type

Two families, both variable, both bundled. No family changes. The tracking ramp is correct
work and is carried through unchanged onto the new rungs.

**The full scale.** Every line height is on the 4sp grid, which the current scale misses on
ten of fourteen roles.

| role | family | size | opsz | weight | line height | tracking |
|---|---|---|---|---|---|---|
| displayHero | Newsreader | 40 | 44 | 450 | 44 | -0.012em |
| displayTitle | Newsreader | 30 | 30 | 500 | 36 | 0 |
| readSerif | Newsreader | 26 | 26 | 450 | 36 | 0 |
| closingLine | Newsreader | 24 | 24 | 400 | 32 | 0 |
| **deckSerif** (new) | Newsreader | 20 | 20 | 450 | 28 | 0 |
| bodySerif | Newsreader | 17 | 17 | 400 | 28 | 0 |
| **itemHero** (new) | Hanken | 32 | | 650 | 36 | -0.030em |
| itemTitle | Hanken | **24** | | 650 | 28 | **-0.026em** |
| title | Hanken | **20** | | 700 | 24 | **-0.016em** |
| bodyStrong | Hanken | 17 | | 600 | 24 | -0.006em |
| body | Hanken | 15 | | 400 | **24** | +0.004em |
| label | Hanken | 13 | | 600 | 16 | +0.016em |
| sidehead | Hanken | 13 | | 700 | 16 | +0.024em |
| caption | Hanken | 12 | | 400 | 16 | +0.022em |
| swipeLabel | Hanken | 10.5 | | 700 | 12 | +0.032em |
| timerNumeral | Hanken | 64 | | 250 | 68 | -0.030em |

**What moved and why.**

- **Every serif `opsz` now tracks its size**, per A7. `displayHero` keeps a 1.1x over drive
  at 40sp, which is inside A7's 1.2x allowance and is recorded here as its reason: at 40sp
  on a dark ground the headline can carry a display cut, and at 30sp and below it cannot.
- **Serif display and deck roles gain weight.** Newsreader ships seven weights and the app
  used one. 450 and 500 are the emphasis mechanism Material's own emphasized scale uses:
  more weight at the same size, spent selectively. Nothing about the family changes.
- **`itemTitle` 21 to 24sp.** 10.3 already calls this the most important string on the
  screen. The sans ladder's top step was 1.105, the smallest step in the whole scale and
  below the most restrained modular ratio in common use, which is why the card read as a
  bolded list row rather than as the subject of the card.
- **`itemHero`, new, 32sp**, on the topmost card only. See 3.6 for the taper.
- **`title` 19 to 20sp** and `body`'s line height 22.5 to 24, which lifts the reading line
  from 1.50 to 1.60 and puts it comfortably above rather than exactly on the WCAG 1.4.12
  floor for blocks of text.
- **`deckSerif`, new, 20sp.** The engine's voice, in the serif, on the one screen where it
  currently is not.
- **The tracking ramp gains three rungs and keeps its shape.** It closes as the size rises
  and asymptotes at -0.030: 20sp at -0.016, 24sp at -0.026, 32sp at -0.030. The serif roles
  stay off the ramp, with `opsz` doing that work inside the outlines, exactly as 5.3 says.

**The unwritten rule this makes explicit: a sentence from the Logic Engine is set in
Newsreader.** Momentum's headline, the Pulse observation, the Report's headline, prose and
closing line all already are. The Areas banner is the only engine sentence in the app set in
the sans, and it is on the home screen, which is why it reads as a system notification
rather than as the app speaking.

### 3.6 Spacing, and the two rails

**Everything on the 4dp grid.** Three of section 6's own six constants are not, and the
line heights above fix the rest.

| measure | current | proposed |
|---|---|---|
| screen padding, horizontal | 20dp | 20dp, fixed |
| card padding, horizontal | 18dp | **20dp**, fixed |
| card padding, vertical | 17dp | **20dp**, scales |
| card gap | 11dp | **12dp**, scales |
| section gap | 28dp | 28dp, scales |
| header block to list | not stated | **36dp**, scales |
| sheet content top | 18dp | **20dp**, scales |

**Two horizontal rails, and only two.**

- **Rail A, 20dp.** Screen titles on pushed screens, chips, the deck, captions, sideheads,
  the closing line, the card's own outer edge.
- **Rail B, 56dp.** All card text. The 7dp dot sits in a 16dp leading gutter at the card's
  20dp padding, and both the area label and the item title hang at 36dp inside the card,
  which is 56dp from the screen edge.

That gives the card a hanging structure: a gutter that holds one mark, and a text column.
It is a sixty year old editorial device and it costs 16dp of padding. Today the app has four
ragged left edges on the Areas screen and no second position anywhere.

**The `itemHero` taper.** The hero size is a factor on `itemTitle`, not a fixed size: 1.33
at a combined text scale at or below 1.0, falling linearly to 1.0 at a combined scale of
1.4, and 1.0 above that. **The hero step is bought out of headroom that only exists at small
text sizes, and it gives that headroom back to the person who needs the room for their
words.** The precedent is 5.3's own cap on the timer numeral at 1.3x, which is the same idea
stated once. At 1.0 the hero is 32sp; at 1.4 and above every card's title is the same 24sp,
and the hierarchy is then carried by the card's tone, its radius and its position.

**Anchor arithmetic on Areas, checked.** Anchor 32sp against the deck at 20sp is 1.60. Anchor
against its peers at 24sp is 1.33. Both clear 6.2's floors. Today the same numbers are 1.43
and 1.00.

### 3.7 Shape

**Every radius moves onto Material's ten step corner scale**, so the design can speak the
platform's shape vocabulary when it wants to. The app currently sits between steps at 11, 14
and 18dp, and gains nothing by it.

| element | current | proposed | Material step |
|---|---|---|---|
| content card | 18dp | **28dp** | ExtraLarge |
| idle area card | 18dp | **20dp** | LargeIncreased |
| Settings group container | none | **20dp** | LargeIncreased |
| row | 12dp | 12dp | Medium |
| sheet top | 28dp | 28dp | ExtraLarge |
| button | 12dp | 12dp | Medium |
| pill | `percent = 50` | **`CircleShape`** | Full |
| Momentum tile | 11dp | **12dp** | Medium |
| swatch | 16dp | 16dp | Large |
| mood pill, widget inner | 8dp | 8dp | Small |
| weekly banner | 14dp | **retired** | |
| Settings icon badge | 8dp | **retired** | |

**Shape carries one rank, and only one.** An idle area card takes 20dp; a card with an
active item takes 28dp. That is the state that matters on this screen and the only one that
gets a radius.

**Open choice, recorded.** The obvious answer is that every card is identical. The
over engineered answer is three ranks, adding a 32dp in session card. **Chosen: two ranks.**
Four dp between 28 and 32 is below the threshold at which a person notices a corner, so a
third rank would be a rule nobody could see, and in session is already carried by a doubled
wash, a status line and a countdown. Two ranks, both visible, is the honest number.

### 3.8 Motion

**The constants are already Expressive's own and mostly do not move.** `springStandard` at
`0.8f / 380f` is `SpringDefaultSpatial` byte for byte. `effects()` at `1.0f / 1600f` and
`effectsFast()` at `1.0f / 3800f` are the effects tokens exactly. That is worth stating
plainly because it kills the easiest wrong diagnosis: **motion is not why this app looks
dead.**

| curve | current | proposed | why |
|---|---|---|---|
| springStandard | 0.8 / 380 | unchanged | Expressive's default spatial, exactly |
| springGentle | 0.9 / 200 | unchanged | Expressive's slow spatial stiffness, damped up |
| springSnappy | 0.75 / 600 | **0.6 / 800** | Expressive's fast spatial, exactly |
| easeOut | tween 350 EaseOutCubic | unchanged, **scoped** | staged entrances only |
| easeSlow | tween 600 | unchanged | world transitions |
| effects | 1.0 / 1600 | unchanged | every fade, color and alpha |
| effectsFast | 1.0 / 3800 | unchanged | |

**`springSnappy` is the one constant that moves, and it is the only place in the twelve
Expressive spring tokens where a person can see the difference.** Peak overshoot for a step
response is `exp(-pi * z / sqrt(1 - z^2))`: at 0.9 it is 0.15 percent, at 0.8 it is 1.5
percent, at 0.6 it is **9.5 percent**. Everything else Expressive calls expressive is a
softened default. This is the bounce.

**And 15.1 bans a bounce on every press, so it is scoped by name.** `springSnappy` drives
the swatch selection, chip selection, the FAB press and the completion mark, and nothing
else. **Card presses stay on `springStandard`**, where the overshoot is 1.5 percent and
invisible. The tell is a bounce everywhere; the tool is overshoot reserved for weight, which
is what 15.1's own wording says.

**Two audits, not new curves.**

1. **Every fade, crossfade, color and alpha animation routes through `effects()`.** There
   are 20 `motion.easeOut()` call sites today and 14 through the two effects springs. A
   1600 stiffness critically damped spring settles in roughly 150 to 200ms; a 350ms tween
   is about twice that. `easeOut` keeps only the entrances that genuinely stage content: the
   Report reveal, the staggered list entrance, the empty state guard.
2. **Wire the shape morph that already exists.** `morphingPressShape` on the FAB and on the
   completion mark, resting `CircleShape`, pressed a rounded square at roughly 40 percent
   rounding, on `springSnappy`. This is stock Expressive button behavior, it fires only under
   a finger, the file already returns the resting shape when `motion.reduced` is true, and
   the library already ships in the APK.

**Three additions, all of them transitional and none of them ambient.**

3. **Predictive back on every pushed screen.** `PredictiveBackHandler`, so the back gesture
   scales and translates the outgoing screen under the thumb instead of the screen vanishing
   on release. This is gesture following rather than animation: it renders the finger's own
   movement, which is why 8.3 already keeps swipe tracking under reduce motion, and it is
   the single most premium thing available on Android for free. It is currently absent
   everywhere.
4. **Shared bounds from the area card to Focus, to the Archive and to the pushed settings
   screens**, keyed on the area id, using `sharedBounds` rather than `sharedElement` because
   the content differs on the two sides. **Not to the area detail sheet**, because shared
   element transitions do not work through `ModalBottomSheet`'s dialog window and 17.3
   settles that the platform sheet is not reimplemented. The sheet gets A6's blur instead,
   which is a different and equally good answer to the same feeling.
5. **Press changes weight, not only size.** 8.2 item 2 gives a press `scale 0.97`, which is
   half a press: a real object pressed toward a surface loses its shadow because it is
   closer to the ground. Animate the ambient layer's alpha toward 0 and the contact layer's
   offset from 1dp to 0dp alongside the scale, on `springSnappy` in and `springStandard`
   out. Scale alone reads as an image being resized. This is the mechanism that makes a
   press feel like it happened to a thing.

**Nothing is added that moves at rest.** Not one of Material's twelve Expressive spring
constants describes a loop; every one is an interaction or a transition. **Adopting
Expressive at full strength requires zero ambient motion**, which means 14's narrowed
sentence, "the only thing this app moves on its own is time," survives this entire direction
untouched. That is not a compromise. It is the finding.

### 3.9 Iconography

Material Symbols Rounded at weight 500 is right and stays. Weight 500 is specifically what
keeps this clear of 15.1's interchangeable thin line icons. Three changes.

**Momentum's `arrow_outward` is not a subject and 7.2 already records it as a defect.** An
arrow says direction, and the screen's subject is a fortnight. **Proposed:
`calendar_view_week`.** Refused alternatives and why: `trending_up` and `bar_chart` imply a
target, which 14 forbids twice; `insights` is a spark and is sparkle adjacent; `graphic_eq`
belongs to the Pulse; `more_horiz` says nothing. This is a new drawable, so it is a phase
item rather than a token.

**The Settings icon badges go.** Every row's badge is the same pale lavender blue rounded
square. A badge whose color and shape never vary carries no information, and it puts a
second claim on a row that already has one. This is 15.1's interchangeable icon entry in its
Android form. The glyph stays at `inkSecondary`; grouping moves to a container, 4.5.

**The Trail's icon column gets one register in both worlds.** Today three glyphs draw their
own circle and two do not, so the light column alternates circled and bare with no rule,
while the dark column puts every glyph on a tinted disc and looks clean. **Two themes that
disagree about whether a column has containment is the clearest machine made signature in
the app**, because a designer carries one decision across surfaces and a model re-defaults
per surface. Proposed: **discs in both worlds**, glyphs chosen without their own circles, one
optical width down the column.

**Open choice, recorded.** The obvious answer is to remove the discs, because subtracting is
what this project reaches for. **Chosen: keep them and add them to light**, because the disc
gives the column a fixed optical width and a rhythm, it is already built and already good in
one theme, and removing it would be the subtractive instinct that produced the problem this
document exists to fix.

---

## 4. Screen by screen

### 4.1 Areas

The flagship, the worst screen in the app, and the one that changes most.

**Anchor:** the topmost card's item title. **Scale contrast:** 1.60 against the deck.
**Ground:** canvas with the center of light. **When short:** a stated end line, and the
column does not stretch.

**What changes.**

1. **The serif screen title goes.** The tab bar names this destination with a filled icon
   and a colored pill 1,900px lower on the same screen. Under 6.2 a screen title is chrome
   and the screen does not say it twice. The archive and settings glyphs stay where they are
   in the same row.
2. **The weekly banner dissolves.** The parchment block is removed and its sentence becomes
   a **deck**: `deckSerif` 20sp on the ground, at rail A, with its caption at 12sp beneath.
   Zero separation devices, because whitespace already separates it. This removes a filled
   block, retires the app's only cream slab, gives the engine its voice back on the home
   screen, and removes the app's single instance of the most current 2026 tell in one move.
3. **The cards get their size.** Radius 18 to 28dp, or 20dp when idle. Padding 18 by 17 to 20
   by 20. Gap 11 to 12dp. Card text hangs at rail B with the dot alone in the gutter.
4. **The topmost card's item title is the hero**, at `itemHero` 32sp tapering to 24 as the
   text scale rises. Every other card's title is 24sp, which is a promotion for all of them
   and a demotion for none, exactly as 10.3 requires.
5. **The deck sits 24dp under the chips and the first card sits 36dp under the deck**, so
   the head of the page reads as a block and the list reads as a list.
6. **The area name loses its accent and takes `inkSecondary`.** The dot already carries the
   identity, the dot is where the contrast was measured, and a row that says the same thing
   twice in color and in text is the same redundancy 6.1 forbids in separation. The dot grows
   from 7dp to **10dp** to carry the identity it is now carrying alone. **This is a change to
   3.4's stated dot size and needs the owner's word**, because 3.4 fixes 7dp deliberately and
   `ClaritySpacing.areaDot` names it as identity rather than rhythm.
7. **The column closes with a stated end**, one line at `caption` on rail A, 36dp under the
   last card. The dark Trail already does this with `trail_end_of_history` and it works. Like
   that one, it is a fixed interface label in `strings.xml` rather than a corpus sentence,
   because it states a structural fact about the list and makes no observation about the
   person, which is the line `CLAUDE.md` rule 8 actually draws.
8. **The FAB loses its blue glow** and takes the neutral shadow stack one rank deeper. It
   stays 48dp. It gains the press morph.
9. **The chips gain the ground's new contrast.** They are at `raise`, which goes from
   1.12:1 to 1.203:1 against the canvas, so they stop reading as disabled without a single
   change to the chip.

**Open choice, recorded.** The obvious answer to a half empty screen is to fill it, either
by flexing the cards to the column height or by adding a recents or suggestions block.
**Both are refused.** Filling it breaks the product's actual promise, which is that
everything else waits out of sight, and it is the one thing the audience research is
unanimous about. Flexing produces a tall card with its content top aligned and a void inside
it, which is the same problem one level down. **Chosen: state the end, light the ground, and
let the remainder be room.** The screen will still have empty space at two areas and it
should.

**Open choice, recorded.** Which card is the hero. The obvious answer is that the app picks
the most urgent one, or the one in session. **Chosen: always the topmost card in the
person's own order.** A hero that moves when the app decides is a screen that rearranges
itself for reasons the reader cannot see, which is the precise failure this audience pays
for. The person's own arrangement decides, and they can reorder it.

**What it looks like afterward.** A deck in the serif at the top saying the one true thing
about the week. Under it, on a ground with a soft center of light, a card that is
unmistakably the subject of the screen: their own words at 32sp, hung on a rail, with a
real contact edge and a pool of their own color at one corner. Under that, the rest of their
areas at the same construction one rank down. Under those, a quiet line saying that is
everything, and then room.

### 4.2 Momentum

The most literally indefensible screen in the app, and the cheapest to fix.

**Anchor:** the engine sentence. **Scale contrast:** 26sp against 15sp is 1.73.
**Ground:** canvas with the center of light. **When short:** the mosaic and the rhythm row
both render at any count, and nothing else is drawn.

**What changes.**

1. **The tiles carry their share.** Today two tiles measure 304px and 303px wide for 64
   percent and 21 percent, because `AreaTileCell` takes `Modifier.weight(1f)` and a fixed
   `TILE_HEIGHT`, and fills at 60 percent when `tile.hasActiveItem`. **The two largest, most
   saturated objects on the screen encode one boolean each.** Section 1's first rule is that
   this app never decorates data, and this is the most literal violation of it in the build.
   Under A3 the tile's **height** becomes its share of the fortnight, bottom aligned inside a
   fixed row height so every tile is read against a common baseline, floored at the label's
   line height so a three percent area is still a tile and still touchable. The three column
   mosaic, the 60 percent fill, the hairline idle state and the name beneath are all
   unchanged.
2. **The `Area balance` section deletes itself.** Its percentages move under the tile names
   at `caption`, which keeps 13's requirement that a graphic is never the sole carrier of a
   claim, and removes a whole section.
3. **The `This week` stat banner goes.** Three equal figures in a row with labels underneath
   **is a stat banner, and `Stat banners` is on this app's own tell list at 15.1.** It
   shipped. The three numbers survive as a single line of running type under the sidehead,
   the Report's own idiom, with the figures at 24sp Hanken semibold tabular inline with their
   labels at 15sp. The Report already does exactly this with `11 events, 1 completed, 1
   added` and it reads better than Momentum's three column version does.
4. **The `Focus patterns` strip is removed.** Seven cells, six empty, one dark, captioned
   `2 minutes in the last seven days`. **That is a contribution graph, and this app bans
   streaks precisely because a rendered run of failures is an abandonment trigger for people
   whose capacity fluctuates.** It also breaks the rule stated two sections above it in the
   same document: the idle mark in the rhythm row is drawn smaller as well as fainter so
   opacity is never the only signal, and these seven cells are all the same size. The minutes
   figure survives in the running line at item 3.
5. **The headline gains weight.** `readSerif` at `opsz` 26 weight 450, which takes the
   sentence from being the largest and faintest thing on the screen to the anchor it is
   supposed to be.

**What it looks like afterward.** A serif sentence with real presence. A fortnight of days
under it. A mosaic of the person's own colors where the tall block is the area that took the
fortnight and the short one is the area that did not, with the numbers under the names. One
line of figures. Nothing else.

### 4.3 The Report

**Leave it almost alone. It is the benchmark and this whole direction is an argument that
the other screens should be measured against it.** It has an eyebrow, a 40sp serif headline
with air around it, a data graphic whose geometry is the datum, a rule that is a gradient
rather than a line, prose at a 47 character measure, one deliberate grid break, a closing
line and a quiet footer. Six levels of hierarchy on one screen. Areas has three, two of
which are the same size.

**Three small things.**

1. **`displayHero` to `opsz` 44 weight 450.** It is currently the thinnest large text in the
   app and it is fighting a dark ground, where thin strokes lose more.
2. **The pattern block's `opsz` 28 at 17sp** is A7's over drive in miniature. `opsz` 20 gets
   the intended change of voice without thinning the strokes.
3. **The tab bar stops occluding the closing line.** A5 fixes the fade; 4.9 fixes the bar's
   material. The closing line is the layer six payload and it is currently readable under a
   cream slab.

### 4.4 The Trail

**Anchor:** the day header. **Scale contrast:** 20sp against 15sp is 1.33 within the list
and 1.60 against the caption. **Ground:** canvas with the center of light. **When short:**
`That is everything`, which it already has and which Areas is borrowing.

**What changes.**

1. **The screen title goes**, for the reason Areas' does.
2. **Two registers, not one.** Every row is currently one size, one weight and one color, so
   `Completed Rewrite the proposal introduction`, `A Pulse was shown` and `Created Today` are
   typographically identical on a screen whose entire purpose is chronology and salience. The
   verb is already the first word of every row and already comes from `TrailStrings`: set the
   verb at `bodyStrong` 17sp and the object at `body` 15sp in one paragraph with an inline
   span. The row gains a scannable left edge of verbs, using weight and size to do the job
   that 15.3 correctly forbids color from doing here.
3. **The day header rises to 20sp weight 700** with 28dp above and 12dp below, so the screen
   reads as a set of days rather than as one long list. That framing is worth more to this
   audience than to any other.
4. **The icon column gets discs in both worlds**, 3.9.
5. **The timestamp column gets a fixed width** so its left edge is straight. Right aligned
   times at ragged vertical positions read as noise, and the de-duplication that omits
   repeated times is a good idea currently presented in a way that looks like missing data.

### 4.5 Settings, and the pushed screens

**Anchor:** none, and that is correct. Settings is the one surface that is not about the
person. **Ground:** canvas with the center of light. **When short:** not applicable.

**What changes.**

1. **The screen title stays**, at `displayTitle` 30sp `opsz` 30 weight 500. The tab bar does
   not name this destination, so 6.2's rule does not reach it, and at the corrected optical
   size the word `Settings` stops looking spindly.
2. **Each group becomes one container** at `raise`, 20dp radius, with its sidehead outside
   it. **The hairlines inside the group go**, and so do the icon badges. That is one claim,
   containment, replacing two devices, and it is the Material surface story applied honestly:
   a group of things belongs together because it sits on a distinctly toned container, not
   because a line separates each pair. It is also the change that makes Settings stop looking
   like chrome.
3. **The segmented control's selected half stops being a pure black pill.** It is currently
   the loudest object on the page and it is a preference toggle. It takes `actionBlue` at 10
   percent with an `actionBlue` label, which is the treatment 10.4 already uses for the
   selected tab.
4. **Settings is hosted from the shell**, not from inside the Areas tab. This is the root
   cause already recorded in `SettingsSurface.kt`'s own header, and it is why the floating
   bar draws over the appearance picker. It is an architecture fix, not a design change, and
   it must happen regardless of everything else here.
5. **Screen padding steps from 20dp to 16dp above a combined text scale of 1.5.** At the app's
   largest the Settings paragraph measures roughly 25 characters per line, below the mobile
   floor of 30. This is the one place where the accessibility feature works against
   readability, and 13.2's rule that horizontal insets never scale was written to protect the
   measure, so stepping the inset **down** at the top of the range serves the same sentence
   rather than contradicting it.

### 4.6 Focus

**Leave it entirely alone.** A 240dp ring with a 6dp stroke, deliberately thin so the weight
can go into a 10dp tip with a 15dp radial falloff, on a gradient whose center is stated to
sit where the ring is. It is the second best screen in the app and it is the source of every
method this direction proposes for the Daylight world.

Two notes for the record rather than changes. The one shared bounds transition worth having
is Areas to Focus, because the card becoming the room is the app's most meaningful
navigation. And this is the app's best feature with the thinnest evidence: there are no
controlled trials comparing analog, digital and visual timers in adults with ADHD, so it is
practice based rather than trial based, and it should not be cited as proven.

### 4.7 The Pulse

**Anchor:** the observation. **Scale contrast:** 26sp against 15sp. **Ground:** deepBlack
with the time of day tint. **When short:** ambient mode, which it already has.

The Pulse is Contemplative in name and, in `v010_pulse.png`, nearly flat black in fact. Three
things.

1. **The time of day tint is specified and is not visible.** 3.3 says evening blends `#2E1F14`
   upward from the bottom, and 11 says it reaches 45 percent of the height at 55 percent. On
   the capture there is nothing to see. **Verify it is drawn at all before changing its
   amplitude**, because a missing draw and a too quiet one need different fixes.
2. **The response pills read as barely present.** 10.7's Contemplative primary is the surface
   accent at 14 percent with a bright label; the capture looks closer to 8 to 10. Take them to
   the specified 14 and give the label `textBright`.
3. **Roughly 300px of dead black sits above the headline.** The observation moves up to sit at
   the room's optical center rather than below it.

### 4.8 Onboarding and the tutorial

**Onboarding** is the weakest Contemplative surface: two option cards barely above the ground
and a large dead field above the headline. It is dark but it is not lit. It takes the same
three part treatment the other Contemplative surfaces have: the per beat glow at its
specified amplitude, the option cards at `surfaceRaised` with the inner top edge light from
3.3, and the headline at the optical center.

**The tutorial has two hard defects** visible in `shot6.png`. `Skip` is drawn directly on top
of the settings gear and is unreadable. The spotlight is a white blob bleeding off the screen
corner and over the tab bar. Both are bugs rather than design decisions and both should be
fixed before anything else in this document, because they are what a first run looks like.

### 4.9 The floating tab bar, which belongs to no screen and stands on all of them

This is not a ninth surface but it is the single most cross cutting defect, so it gets its
own heading.

Section 2 of `design-v3.md` says the Contemplative surfaces ignore Light, Dark and System
entirely. **The tab bar does not.** In light mode it draws a `#F4F3F0` cream pill with a blue
inner pill over the near black gold Report and the near black Trail. It is the brightest and
loudest object on the most carefully art directed screen in the app, and it is chrome.

**The bar belongs to the world it is standing on.** Three variants, one rule:

| world | fill | selected pill | selected label |
|---|---|---|---|
| Daylight light | `raise` `#EBEAE6` | `actionBlue` at 10 percent | `actionBlue` |
| Daylight dark | `raise` `#1C1C24` | `actionBlue` at 10 percent | `actionBlue` |
| Contemplative | `surfaceRaised` `#14141C` | that surface's accent at 14 percent | that surface's accent |

Plus the inner top edge light from 3.3, which in the Contemplative case is the only thing
that will separate the bar from `deepBlack` at all, since `surfaceRaised` against `deepBlack`
measures 1.072:1.

The bar keeps exactly one claim, `LitSolid`, in every world.

---

## 5. What this deliberately does not do

**No new visual vocabulary.** Every technique proposed here is either already in the document
(the radial with a stated center, the pooled wash, the paired shadow, the tracking ramp, the
optical size axis) or already in the repository and uncalled (`morphingPressShape`). Nothing
needs a new library. `graphics-shapes` already ships.

**No family change.** Newsreader and Hanken Grotesk are off every autopilot list, the
optical size axis is genuinely driven per role, the tracking ramp is real work, and tabular
figures are set on every updating numeral. **This is the best asset in the design system and
this document is almost entirely about spending it rather than replacing it.** 15.3
anticipates the swap proposal by name and is right.

**No colored stripe, bar or edge on any component's boundary.** A3 narrows the wording to
what the rule always meant and does not weaken the ban. Every gain in this document is
available without touching it.

**No ambient motion.** Not a pulse, not a breathe, not a glow loop, not a shimmer, not an
idle shape morph. Not because Expressive demands the alternative, but because 14 as narrowed
in phase 12b is correct and because none of the twelve Expressive spring constants describes
a loop, so nothing is given up by keeping it.

**No streaks, chains, counts, badges, XP, levels, rings toward a target or celebration.** The
direction goes further: it **removes** the one element in the build that renders the negative
space of a streak.

**No dynamic color.** A wallpaper derived scheme would collide arbitrarily with 48 identity
hues.

**No live blur behind the tab bar.** That content scrolls, which is the case a blur cannot
afford at 120Hz, and the `DstOut` fade is the right answer there including its reasoning
about the Contemplative gradients. A6 narrows the refusal, it does not repeal it.

**No specular highlights, no lensing, no gyroscope driven light, no rim glow.** The blur in
A6 is a suppression tool with one job. Everything that usually travels with it exists to be
noticed, which is the opposite of the reason it is being adopted.

**No gradient on a screen title, on the timer numeral or on any large figure.** The ground
gradient is a ground, which 15.3's own wording scopes it out of.

**No filling of the empty half of Areas.** Refused twice above, in 4.1, for the product's
reason and for the audience's.

**No AGSL.** API 33 against a floor of 31 buys a branch and a dead path for a background.

**No reimplementation of the platform sheet, text field, snackbar or tab bar.** 17.3 settles
those and this direction works within it, which is why the area detail sheet gets a blur
rather than a shared element.

**No change to any behavior, any string's meaning, any engine layer, any event, any schema,
or the order in which anything happens.** The one place this direction comes close is the
Areas closing line, and 4.1 states exactly why that is a fixed interface label rather than a
corpus sentence.

---

## 6. The anti-slop test

15.2 requires a sweep before release and phase 12b honestly recorded that it could not
perform one. **A sweep has now been performed and 15.1 needs updating.** What follows is the
result, then this direction checked against it entry by entry.

### 6.1 Proposed updates to 15.1

**Corroborated this sweep and unchanged:** the colored 3 to 4dp card stripe, which is still
the top card tell; Inter as the interface typeface; the blue to purple gradient, still named
the loudest single tell; purple to cyan; lavender and indigo to purple; a gradient on a large
number; gradients everywhere with no restraint; three or six identical feature cards in a
row; interchangeable thin line icons; weightless headline copy; a bounce on every press;
sparkle iconography; dark mode with low contrast body text; and, newly corroborated, an
expressive motion system adopted at full strength with no setting to turn it down.

**Cleared, and worth stating so nobody spends a phase arguing:** bento grids; glassmorphism
as such, where the surviving half is the neon glow that traveled with it; dark mode itself;
mesh, aurora and blob backgrounds; stock illustration when it is intentional. Rounded
corners, soft shadows and card layouts were never the tell; the pattern was.

**Could not be corroborated this sweep and should be marked unverified rather than defended:**
a badge above a centered headline; numbered 1-2-3 sequences; stat banners; serif italics as
an accent; all caps section labels; the gradient rounded square app icon. **Keep them all as
house rules** where 14 already forbids them, and stop citing them as industry observations.

**Should move from 15.1 to 15.3, because they are audience refusals rather than industry
observations:** the flame glyph beside a day count, the ring closing toward a target, and the
red numeric badge. 15.3's own opening argument applies to all three exactly.

**Seven new entries.**

1. **Cream ground, warm serif, sage accent.** The current co-top tell, with a coinage of its
   own as of July 2026, driven by AI companies pivoting their own product surfaces to serif
   on warm paper. **The Areas parchment banner under a Newsreader title is this, and 4.1
   removes it.** This is the most uncomfortable entry in the sweep for this project.
2. **Lazy minimalism: cleared space presented as restraint.** Elements removed without being
   replaced by intention, so the space is not designed, it is just cleared. **This is the
   entry that describes the current Areas screen**, and 15.1 has nothing that would have
   caught it, because 15.1 is a list of things added and this is a thing not done.
3. **Uniform maximum rounding with no radius scale.** The app already passes and 3.7 keeps a
   six step scale rather than flattening it.
4. **Emoji standing in for icons.** Already banned by 14; listed here so a component author
   finds it.
5. **Permanent dark mode adopted as a reflex.** Note carefully that dark mode itself is
   cleared; the tell is dark with no stated reason. This app passes because 3.3 gives each
   permanently dark surface a reason and the Settings copy says it out loud.
6. **Layout quality tells:** text overflow, content under system chrome, off grid spacing, no
   hierarchy. This class **cannot be scanned for and needs a human eye on a screenshot**,
   which is exactly why N2 changes what 15.2's gate is pointed at.
7. **Untouched framework defaults.** On web that is shadcn and Tailwind; the Android
   translation is untouched Material 3 defaults, which 15.3 already refuses. Promote it to
   15.1 so it is checked and not only refused.

### 6.2 This direction, checked entry by entry

| entry | verdict |
|---|---|
| colored stripe or bar on a card | **clear.** A3 narrows the wording and strengthens the ban; nothing here attaches a mark to a component's boundary |
| Inter, Instrument Serif, Space Grotesk | **clear.** No family changes |
| serif italics as an accent | **clear.** Italic stays scoped to emphasis inside a sentence |
| all caps section labels | **clear.** Sideheads stay sentence case; tracking does the work |
| blue to purple, purple to cyan, lavender to indigo gradients | **clear.** The one ground gradient is one neutral hue at 2.5 percent |
| gradient on a large number | **clear.** Explicitly refused in section 5 |
| glassmorphism as decoration | **close to the line, and answered.** A6 permits one shot blur only, only under a modal sheet, only on a measured legibility failure, and only without the rim light that is the surviving half of this tell |
| hairline plus diffuse shadow on one element | **close to the line, and answered.** A2's distinguishing test is stated in the rule: a hairline is uniform on four sides and lit from nowhere; an edge light is one edge, lit from the same direction as the shadow |
| cards inside cards | **clear.** The Settings group container replaces hairlines rather than wrapping cards |
| three or six identical cards in a row | **clear.** Momentum's mosaic stops being identical the moment its heights carry data |
| stat banners | **clear, and the shipped one is removed.** 4.2 item 3 |
| sparkle iconography | **clear** |
| dark mode with low contrast body text | **clear, and improved.** The dark ladder widens and every ink reading is re-run |
| a bounce on every press | **close to the line, and answered.** `springSnappy` at 0.6 is scoped by name to swatches, chips, the FAB and the completion mark; card presses stay at 0.8, where overshoot is 1.5 percent |
| flame, ring toward a target, red badge | **clear, and enforced harder.** The focus patterns strip is removed |
| expressive motion at full strength with no way to turn it down | **clear, and this is the app's best receipt.** One boolean, and calm mode joins it |
| **new:** cream, warm serif, sage | **clear, and the app's own instance is removed** |
| **new:** lazy minimalism | **this is the entry the whole document answers** |
| **new:** uniform maximum rounding | **clear.** Six radii on Material's own scale |
| **new:** permanent dark with no reason | **clear** |
| **new:** layout quality tells | **three shipped instances named and fixed:** the status bar overlap, the tab bar occlusion, the tutorial's Skip |
| **new:** untouched framework defaults | **close to the line, and stated.** `springSnappy` moves **to** Material's own constant. The reason is not that it is the default; it is that it is the only one of the twelve tokens that produces visible overshoot, and overshoot on small press feedback is the effect being bought. Recorded here so it is not rediscovered as an unreasoned adoption |
| **new:** mesh, aurora, blob backgrounds (cleared) | **adjacent, and stated.** The ground light is one hue, one center, 2.5 percent, static. If a future sweep re-lists this family, this is the element to re-examine first |

**The deeper answer to "will this read as machine generated" is not the checklist.** A model
re-defaults per screen; a designer carries one decision across all of them. The clearest
machine made signature in this build is not any single treatment, it is that the app contains
**two design systems split by tab**: one lavish and art directed, one absent. Every screen in
section 4 is being brought onto the same construction, and the tab bar in 4.9 stops being a
Daylight component standing on a Contemplative page. That consistency is the part a person
actually detects, because they cannot see provenance and they can see two design systems.

---

## 7. The audience test

Every move, scored against somebody who is overwhelmed by screens. **Overwhelm is a function
of competition, not volume.** The current Areas screen has nineteen marks in six sizes with
five inside a nine point band, so the reader resolves the hierarchy manually on every open.
Removing that work is the point of every row below.

| move | helps or taxes | calm mode | reduce motion |
|---|---|---|---|
| Anchor at 1.6x, hero item title | **Helps, most of anything here.** One dominant object resolves the screen in under a second, which is 11's own three second test finally met | unchanged, it is not motion or color | unchanged |
| Drop the serif screen title | **Helps.** One fewer competing mark, and the destination is still named by the bar and still announced to TalkBack | unchanged | unchanged |
| Deck in the serif on the ground | **Helps.** The engine's sentence stops looking like a system notice, and a filled block leaves the screen | unchanged | unchanged |
| Widen the ladder to 12.88 `L*` | **Helps.** A card that reads as an object needs no interpretation | unchanged, calm mode is not a theme | unchanged |
| Three layer shadow plus edge light | **Helps.** Operability is currently unsignaled: the card and the banner above it are the same object class and one of them does nothing | unchanged | unchanged |
| Tighter, deeper wash | **Helps.** Identity becomes glanceable, which is a working memory cost removed on every open | transformed and pinned low, exactly as 16.2 already specifies | unchanged |
| Ground center of light | **Helps.** Converts terminal emptiness into room, which is the difference between calm and unresolved | transformed with its geometry held, per 16.2's rule for gradients | unchanged, it does not move |
| Card radius as a rank | **Helps mildly.** A second non color signal for idle versus active, which 13 asks for | unchanged | unchanged |
| Stated end line on Areas | **Helps, and it is the cheapest fix in the document.** Unlabeled space reads as a possible problem this audience then has to resolve | unchanged | unchanged |
| Settings groups become containers | **Helps.** Structure by containment rather than by fourteen separate lines | unchanged | unchanged |
| Momentum tiles carry share | **Helps.** The loudest object starts carrying the subject, and a whole section disappears | already transformed, 16.2 names the tile explicitly | unchanged |
| Remove the focus patterns strip | **Helps, and it is the most important removal here.** Six empty cells and one filled is a run of failures rendered on a screen that bans them | not applicable | not applicable |
| `springSnappy` to 0.6 / 800 | **Neutral.** It fires only under a finger and only on four elements | crossfade, 8.5 | crossfade, 8.3 |
| FAB and mark press morph | **Neutral to mildly positive.** It answers a touch; it never fires unattended | resting shape | resting shape, already handled in `ExpressiveShapes.kt` |
| Press changes weight | **Helps mildly.** A press that reads as happening to a thing is a clearer acknowledgment than a resize | state change with no interpolation, and the shadow still differs between states, which is the information | same |
| Predictive back | **Helps.** It answers "where am I going" with the thumb's own movement rather than a sentence | **kept.** It is gesture following, not invented motion, exactly as 8.3 keeps swipe tracking | **kept**, same reason |
| Shared bounds, Areas to Focus | **Helps.** Object permanence substitutes for working memory | 150ms crossfade | 150ms crossfade |
| Fades onto `effects()` | **Helps.** The current 350ms tween is roughly twice Material's own; a slow acknowledgment reads as an unresponsive app to the audience least able to tolerate it | crossfade | crossfade |
| One shot sheet blur | **Helps, and it is an accessibility feature.** It removes a fully readable sentence from behind the sheet the person is trying to read | **kept.** Removing it would reintroduce the competing text | **kept.** The radius snaps rather than animating; the blur itself is not motion |

**Two entries deserve the argument spelled out, because they are the ones a reviewer will
challenge.**

**The blur under reduce motion.** The instinct is to strip every effect when somebody asks
for less. That would be safe and wrong. A blur is not motion, and for this population it is
doing the same job the scrim was supposed to do and could not. **What degrades is the
animation of the radius, not the radius.** The degraded version is genuinely good: the room
recedes instantly instead of receding smoothly.

**Predictive back under reduce motion.** Same shape of argument. 8.3 already keeps swipe
tracking because it renders the finger's own movement rather than motion the app invented,
and predictive back is the identical case. The degraded version is a back gesture that still
follows the thumb into a fast crossfade, which is better than a screen that vanishes.

**And the largest audience claim, stated plainly so it can be argued with.** Making the item
title the biggest thing on the screen is not adding expression. It is enforcing a rule this
design already wrote and this build already broke: 10.3 says the item title never shrinks and
is the most important string on the screen, and 11 says Areas must pass a three second test.
**A screen whose largest element is a navigation label does not pass a three second test.**

---

## 8. Build order, and the tests that hold it

Ordered by visible effect per unit of risk. The first four are the overhaul; the rest is the
long tail.

**Before anything else, four defects.** The status bar overlap at large text (A5), the tab
bar occluding the Report, the Trail and Settings (A5 and 4.9), Settings hosted from the shell
rather than from the Areas tab (4.5), and the tutorial's `Skip` over the settings gear (4.8).
These cost the app more perceived quality than any token in this document, and the person who
set their font to maximum is exactly the person this app claims to exist for.

1. **Type.** A7's optical sizes, the new weights, `itemTitle` to 24, `itemHero`, `deckSerif`,
   `title` to 20, the 4sp line heights, the three new tracking rungs. Zero frame cost, zero
   rules engaged, and it is 10.3 being enforced rather than a new idea. Re-shoot at 200
   percent afterward.
2. **The ladder.** A1's rule and 3.2's values in both worlds, then the full 16.7 re-run.
   Expect `inkSecondary` on the light canvas at 4.59:1 and confirm it clears 4.5.
3. **The light model.** A2's claim rule, the three layer shadow, the edge light in both
   worlds, the FAB's glow replaced, the ground's center of light. Profile before and after
   with the GPU rendering profiler; if the ambient layer costs, it is the layer to drop,
   because the contact layer is what makes the object read.
4. **Areas, whole.** 4.1, every item.
5. **Momentum**, 4.2, which needs A3 accepted first.
6. **The tab bar's three worlds**, 4.9.
7. **Settings containers and the Trail's two registers**, 4.5 and 4.4.
8. **Motion:** the `easeOut` audit, `springSnappy`, the press morph, press weight, predictive
   back, shared bounds.
9. **The sheet blur**, A6, last, because it is the one item that needs a device in hand
   before it is believed.
10. **Iconography**, 3.9, which is new drawables rather than tokens.

**The tests this direction adds or changes.**

- `SurfaceLadderTest` gains A1's four numbers, including the composited pooled corner across
  all 48 area colors.
- The component walk changes from device to claim, per A2, and gains the `Edge` plus
  `LitSolid` co-occurrence check.
- A new screenshot test asserts zero content alpha inside the status bar inset and the tab
  bar band, at 1.0x and at the 200 percent cap. **This is the one test that would have caught
  a shipped defect on its own.**
- A new type test asserts every serif role's `opticalSizing` sits between 1.0x and 1.2x of
  its `fontSize`.
- The area color source scan gains a third label, `datum`, per A3.
- The blur source gate narrows from `nothing in ui/` to `nothing in ui/ outside
  ClaritySheet.kt`.
- `ContrastAuditTest`, `TextSizeScaleTest` and `FaintInkTest` need their expectations
  re-derived rather than adjusted, because the ground moved.
- `AreasScreen.kt`'s `CARD_HEIGHT_ESTIMATE`, currently a 96dp constant used for reorder hit
  testing, must become a measured height once the hero card exists.

---

## 9. Open questions for the owner

Five things this document cannot decide alone.

1. **The area dot at 7dp or 10dp.** 4.1 item 6 moves the area name off its accent and asks
   the dot to carry the identity alone, which argues for 10dp. `design-v3.md` 3.4 fixes 7dp
   and `ClaritySpacing` names it as identity rather than rhythm, deliberately. **Both halves
   of that trade are defensible and the owner should pick.** If the dot stays at 7dp, the
   area name keeps its accent and the redundancy stays.
2. **A3, the data mark clause.** It is the only amendment that touches section 14's most
   protected sentence, and although it strengthens the ban in the same breath, it is the one
   an owner should read twice.
3. **A6, the sheet blur.** The measurement is unambiguous and the refusal it amends is real.
4. **The Momentum icon.** `calendar_view_week` is the best of the options I could find that is
   not an arrow, a chart or a target, and it is not obviously right.
5. **Whether Areas keeps any screen title at all.** 6.2 says the tab bar already names it and
   this document takes that seriously enough to remove the largest text on the flagship
   screen. That is the single most visible change here and it is worth the owner seeing it on
   the phone before it is settled.

---

**One sentence, again, because it decides everything above.**

> On every screen the loudest thing is the person's own life, and everything the app says
> about itself is quieter than that.

That it currently is not is the whole of what is wrong, and the clearest single statement of
it is that the engine's sentence about their week is set in the sans, inside a cream box, on
the one screen they open every day.
