# Design research log

Standing research behind the visual and interaction decisions in this app.
`design-v3.md` remains the authority; this file records what was checked, when, and
what changed as a result, so the tell list in `design-v3.md` 15.1 can be updated
against evidence rather than memory.

**Reviewed August 2026.** Re-run before every release. design-v3.md 15.2 makes the
anti-slop pass a release gate and explicitly says to expect the list to change.

---

## 1. The machine-generated look, current tells

Sources checked in August 2026: 925 Studios on AI design tells, SmoothUI on AI
design slop, VibeCodeKit's 2026 fix guide, UXSkill on generic AI-generated UI.

The finding all four agree on is the mechanism rather than the list: **these are not
design choices at all, they are the statistical default a model reaches for when
nobody told it to do anything else.** That is exactly what the open-choice rule in
`design-v3.md` section 15 was written to defend against, and it is why the rule is
phrased as "identify the obvious answer and then deliberately choose something else."

### Tells this design already refuses

Each of these was already banned before this research, and each is confirmed as a
current tell rather than a stale one.

| tell | where it is refused |
|---|---|
| Inter as the interface typeface | 5.2, Hanken Grotesk |
| Indigo to purple gradient | 3.4, no gradient anywhere as decoration |
| A gradient rounded square with a check as the app icon | 4.3, explicitly |
| Three rounded cards in a row with a thin line icon on top | 15.1 |
| Glassmorphism as decoration | 15.1 |
| A hairline border and a diffuse shadow on one element | 6.1, the separation rule |
| Cards nested inside cards | 14 |
| Sparkle or magic wand iconography | section 7, explicitly |
| All caps section labels | 10.12, sentence case sideheads |
| Serif italic as a section accent | 5.1 |

### Tells to add to the list

New in the August 2026 review, folded into `design-v3.md` 15.1:

- **Blue to purple gradient**, named by 925 Studios as the single loudest tell of
  2026. The list already carried indigo to purple; blue to purple is the wider
  family and the more common form
- **Purple to cyan gradient**
- **Glassmorphism combined with a neon glow**, which is the 2026 evolution of plain
  glassmorphism and reads as newer while being more generic
- **Six identical cards in a row**, the same failure as three, at a scale that also
  destroys hierarchy
- **A bounce on every hover or press.** Bounce as a default rather than as emphasis.
  This one matters here: Material 3 Expressive encourages overshoot, and overshoot
  applied to everything is indistinguishable from the tell. Overshoot belongs on
  spatial motion at moments that carry weight, never on opacity and never on every
  press
- **Weightless headline copy**, the "Build faster. Ship smarter." register. Copy that
  could belong to ten thousand products. The corpora already fight this at the
  sentence level; this extends it to fixed interface strings
- **Interchangeable thin line icons** with no relationship to the product. Material
  Symbols Rounded at weight 500 is a deliberate, named choice rather than a default,
  and the mark is drawn rather than borrowed

---

## 2. Craft rules adopted

From Rauno Freiberg's interaction design craft notes, Adam Arant on optical
alignment, and a comparative piece on how Stripe, Linear and Vercel ship premium UI.

### Adopted and implemented

**Cap height trimmed labels.** Text in a button, chip or pill sits optically low
because the line box reserves descender space the label does not use. Trimming to
cap height and baseline centers it properly. Implemented as `ClarityTypography`
carrying trimmed variants used by every contained control.

**Designed keyboard focus.** Stripe, Linear and Vercel all treat focus as a first
class state: six states per interactive element, not three. Default, pressed and
disabled were covered; keyboard focus was not. Implemented as a designed focus ring
rather than a platform default.

**Optical nudge on asymmetric glyphs.** A play triangle centered geometrically looks
left of center because its mass is left of its bounding box. Shifted by 3 to 6
percent of icon width toward the point, which is 1dp at our 13 to 24dp sizes.

**Trailing glyph padding.** Icon viewBoxes carry their own whitespace, so a row with
a trailing chevron reads as over padded on that edge. Two to four dp is subtracted
from the trailing padding where a glyph closes a row.

**Tracking across the scale.** Negative tracking on display sizes, none through body,
positive only on small caps. `design-v3.md` 5.3 already specifies this shape:
displayHero at -0.012em, itemTitle at -0.022em, body at zero. No all caps exists in
this app, so the positive end of the rule never applies. Confirmed rather than
changed.

**Optical sizing.** Driving the `opsz` axis per role, which the type scale already
does, is strictly better than the automatic behavior these sources recommend,
because it is set per role rather than per rendered pixel size.

### Adopted from the interaction craft notes

**Live response before threshold.** An element must move with the finger
immediately, not after a threshold is crossed. The swipe row tracks one to one from
the first pixel and the commit is decided on release.

**Gesture end for destructive actions only.** Lightweight actions may fire during a
gesture; anything destructive waits for a deliberate, completed act. This is the
same conclusion `design-v3.md` 10.3.1 reached independently, and it is why a full
left swipe commits Swap rather than Delete.

**Momentum is preserved, not normalized.** A fling above 1,200dp per second commits
below the distance threshold, so a quick flick is honored rather than ignored.

**Animation is removed from high frequency interactions.** Switching tabs crossfades
in 180ms with no slide. Repeating a movement several times a minute makes the
movement into noise.

### Considered and rejected

**Scaling circles up 4 to 8 percent beside squares.** Correct in general, and the
compensation is real, but the only place this app puts a circle beside a square of
the same nominal size is the settings icon badge, where the sizes already differ by
more than the compensation. Adding it would be cargo cult.

**Hanging punctuation.** Genuinely improves a left edge, and the Report's serif prose
is the one place it would show. Deferred to the Report phase rather than applied
blind, because it needs to be judged against real generated text.

**Judged, and declined.** Counted across `CORPUS_2_REPORT.md`'s 2,357 authored lines:
every one begins with a capital letter or with a slot, and every slot resolves to a
name, a title, a number or a day. There is not one quotation mark in the whole report
corpus, and the two families that quote a person's own answer render `{priorLabel}`
unquoted. So nothing the engine can compose begins with the character this treatment is
for. The one remaining path is a person's own text, since `{areaName}` and `{itemTitle}`
render what they typed, and serving that means a custom paragraph layout on the app's
most read prose for a case that arises only when somebody puts quotation marks in their
own item title. That is the same mistake as the circle scaling above: applying a correct
general rule where the condition it corrects does not occur.

---

## 3. What this app is betting on

The premium sources converge on one thing: **restraint plus one deliberate,
unusual, correct choice.** Stripe's commissioned sans, Linear's cool greys, Vercel's Geist.
None of them is loud; each is specific and each is theirs.

This app's equivalents are the Newsreader and Hanken Grotesk pairing, the queue mark,
the two worlds, the 48 color palette used only as a dot, a wash, a tile and a label,
and a sentence system that would rather say nothing than say something generic.

---

## Sources

- 925 Studios, AI slop design tells, https://www.925studios.co/blog/ai-slop-design-tells
- SmoothUI, AI design slop, https://smoothui.dev/blog/ai-design-slop
- Rauno Freiberg, interaction design craft, https://rauno.me/craft/interaction-design
- Adam Arant, optical alignment in UI, https://adamarant.com/en/blog/optical-alignment-in-ui-7-spacing-fixes-math-gets-wrong
- Mantlr, how Stripe, Linear and Vercel ship premium UI, https://mantlr.com/blog/stripe-linear-vercel-premium-ui
- Material 3 motion, https://m3.material.io/styles/motion/
- Google Design, expressive material design research, https://design.google/library/expressive-material-design-google-research
