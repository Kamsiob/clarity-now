# Decisions

Why this project is the way it is.

Every other document in this repository says what the app does. This one says why it
does that and not the obvious alternative, so that a session arriving with no context
can tell a settled question from an open one, and can see the argument rather than
only the instruction. An unrecorded decision gets re-proposed every few months, and a
session with no record of the losing option will pick it, reasonably, and be wrong.

## What belongs here

- A choice between two defensible options, with the losing option named
- A conflict between two documents, and which one won
- A rule adopted that constrains later work
- A thing deliberately not built. These are the entries that earn their keep. A no
  with no reason attached does not survive contact with the next session
- A component built by hand rather than taken from the platform, per the platform
  first rule in Addendum 01 Step 3. Those go in the standing register at the foot of
  this file, one line each, rather than in a dated entry
- A question the owner has to answer, recorded as open, with the recommendation
  stated and not taken

## What does not belong here

- What a thing is, or how it behaves. That is MASTER_BUILD_PROMPT.md and design-v3.md
- The exact wording of any sentence the app says about a person's own data. That is a
  corpus file, and there is no second path
- Progress. What is built lives in `docs/BUILD_STATE.md`, what remains lives on the
  issue board with acceptance criteria
- A decision that was never in doubt. Writing those down buries the ones that were

## Where this sits in the authority order

Nowhere. This file records reasoning; it has no authority over anything. The order in
CLAUDE.md is unchanged: design-v3.md wins on anything visual or interactive,
MASTER_BUILD_PROMPT.md wins on behavior and data, CLARITY_LOGIC_ENGINE.md wins inside
`domain.engine` and `domain.guidance`, and the corpora win on the exact wording of any
sentence.

If an entry here disagrees with one of those documents, the document is right and the
entry is stale. Fix the entry. If the entry is what the owner actually decided and the
document never caught up, change the document and write a new entry saying so. An
entry is a record of a decision on a date, not a standing instruction, and a session
must never build from this file alone.

## How an entry is written

**Newest first.** The top of the file is the current state of the argument. The one
exception is the standing register at the foot, which is append only and ordered by
the date each row was added, because a dozen one line records spread through a dozen
dated entries would bury everything else in the file.

Each entry carries:

| field | what goes in it |
|---|---|
| heading | the date written out, then a short name for the decision |
| **Decided** | what is now true, in the fewest sentences that leave no ambiguity |
| **Why** | the reasoning and the evidence behind it, long enough to be checkable |
| **Considered and rejected** | the options that lost, each with its reason |
| **Revisit if** | what would have to change for the decision to be reopened |

The last field is not optional and is not decoration. A decision with no revisit
condition is a rule, and a rule belongs in a specification document rather than here.
Where an entry settles several things at once, each section carries its own revisit
line.

**Nothing is deleted.** A reversed decision gets a new entry that names the old one.
The old entry stays where it is, wrong, dated, and useful.

---

## August 28, 2026: the Pulse repeat filter covers yesterday, because that is what it always said it covered

**Decided.** The Pulse repeat filter at step 4 of selection now applies **only where the last
Pulse was generated yesterday**. `Selector.REPEAT_WINDOW_DAYS` is one and it is not a tuning
number; it is the length of the word yesterday. The filter is still keyed on the family alone
and **is not narrowed to `(family, subjectId)`**, which was the option this pass was pointed at
and which the measurement ruled out. **Pulse silence falls from 65.7 percent to 56.0**, 2,067
silent days across eleven persona years to 1,762, and `verifyClarity` is green at 1,025 tests.

**Why.** `CLARITY_LOGIC_ENGINE.md` 7.3 says the cooldown covers `cooldownDays` and is
"separate from the no-repeat rule, which covers only yesterday". Section 12's table calls this
filter "yesterday's family cannot be today's". Step 4 of section 5 names the fact rather than
the rule, `PulseFacts.lastGeneratedFamily`, and that fact is the family of the most recent
Pulse generated **at any point in the past**. The code followed the fact name. Three statements
in the authoritative document say one day and the implementation said forever, so this is a
defect against the document that wins inside `domain.engine` rather than a behavior change,
and it is not the owner's to rule on.

**The unbounded reading is self-reinforcing, and that is the part no reading of the code
shows.** A family blocked at step 4 writes no `PULSE_GENERATED`. `lastGeneratedFamily`
therefore does not advance. The same family is blocked again tomorrow, and the morning after
that, for as long as it stays the only family a life qualifies for. Of the 869 days where this
filter alone emptied the candidate list, **only 169 were a gap of one day**; 287 were two to
six, 121 a week to a month, 78 one to three months and **214 were ninety days or more**.

**`balancedAcrossFour` is the case, and it is worse than the aggregate.** The eighth
measurement recorded it as speaking nine times a year, all between January 5 and January 20,
then filtered on 348 consecutive days, and concluded that no change to a wait could reach it.
That was right about waits and wrong about the cause: the filter is what holds it. Four evenly
used areas qualified `persistence` on a **different item** almost every day and every candidate
was discarded at family level off a Pulse from January 20. It now hears **183 Pulses instead of
9**, its silence is 49 percent instead of 97, and its longest silent run is **one day instead of
348**. The bound makes the loop unreachable rather than unlikely, because a day the Pulse did
not speak is a day this filter cannot apply.

**Considered and rejected.** *Narrowing the filter to `(family, subjectId)`.* This was the
option the pass was directed at, on the argument that the filter's only distinct effect is
blocking a different subject of the same family and that nobody has argued for that. Both
halves were measured and they rule it out. **Of the 869 days, 366 carry a candidate whose
subject differs from the last Pulse's and 359 would speak under a narrowed filter**, which is
41 percent of the cost against the bound's 66. And **every Pulse family declares three days of
cooldown or more**, so on consecutive days step 5 already blocks the exact pair: of the 691
same pair candidates the filter dropped, **not one at a gap of a single day escaped its own
family's cooldown**. Narrowing is therefore an exact no-op on the one day the rule is about.
It does not keep the protection and drop the cross subject blocking; it drops the protection
the specification states and keeps the blocking after a long gap that the specification never
asked for. Cross subject blocking on consecutive days is not a side effect of step 4, it is the
only thing step 4 contributes that nothing else does.

*Removing the filter entirely.* Still the owner's and still not taken. It is worth 120 further
days now rather than the 574 the eighth measurement priced, because most of that 574 was the
missing bound and not the rule.

*Building a permanent per filter counter into `Selector`.* Rejected for the reason the eighth
pass rejected it: mutable accounting inside a layer whose purity is a build rule, for a
question asked twice. The instrumentation was temporary, the file was restored from a copy
taken before it and `git diff` shows only the bound.

*Leaving `HotFamilies` and `docs/CORPUS_ANCHORS.md` at the eighth measurement.* The table is
compared against a live run by `CorpusRenderTest` and 20 rows moved, so it cannot be left. Three
families sit within two firings of the forty firing line and all three crossed it: `freshStart`
in at 42, `consistentRhythm` and `cleanSlate` out. The anchors moved with the table.

**One reading moved the wrong way and it is not rhythm.** The worst family concentration went
from 48 percent to 97: `balancedAcrossFour` takes 178 of its 183 Pulses from `persistence`.
That is the bound making something visible rather than causing it. With four evenly used areas
no area holds seventy percent of a window, so `concentration` cannot qualify, `spread` fires
once, and `persistence` is the only family whose rules that life satisfies. **Nine sentences a
year hid it.** It belongs to the 907 day `NO_RULE_QUALIFIED` column, which is now the largest
single producer of Pulse silence for the first time, and it is a rule coverage finding rather
than a reason to restore the block.

**Revisit if** the owner rules on removing the filter outright, at which point 120 days is the
number to re-measure against; or if a persona is ever seen to hear the same family on two
consecutive days, which the bound must never allow and which `SelectorTest` asserts at one day
and two; or if `NO_RULE_QUALIFIED` is worked, at which point the family concentration reading
is the one that should move and this entry is the record of why it is high.

---

## August 28, 2026: the eighth measurement, and the one line that is three quarters of Pulse silence

**Decided.** Four passes that had each verified themselves alone were run together for the
first time and the integrated tree is green: `verifyClarity` at **1,024 unit tests**, taken
from a full `--rerun-tasks` build rather than an up to date one. The eighth measurement is
that integrated tree and not any one pass, so the column the register pass had already
written into `CLARITY_LOGIC_ENGINE.md` 12 is corrected in place rather than given a ninth
column of its own: variant repeats read **2,411** and not 2,414, band collisions **289** and
not 288, parallel numeric runs **148** and not 138. Two rows were added to the table, lines
the engine cannot say, **86 to 2**, and unbound markers in the corpus, **99 to 0**. Nothing
was committed.

**And the finding the pass exists for.** Pulse silence is **65.7 percent, 2,067 of 3,148
opens, splitting 1,161 filtered, 895 nothing qualified, 11 insufficient** for the third
measurement running, identical to the sixth and the seventh to the day. `Selector.select`
was instrumented temporarily to record which of its five filters emptied the candidate list.
**The Pulse repeat filter produces 878 of the 1,164 filtered days, 75.4 percent.** Cooldown
produces 268, the horizon 18, and family availability and callback resolution produce
**none**, as does the realizer, whose `ALL_CANDIDATES_VETOED` has been zero in every
measurement ever taken. The same run computed the counterfactual: **574 of the 878 would
speak if the filter were removed and 304 would still be caught by the cooldown behind it,
which is silence at 47.4 percent instead of 65.7.** The two filters are not redundant
because they are keyed differently, the repeat filter on the family alone and the cooldown
on the `(family, subject)` pair.

**Why this is the explanation and the corpus never was.** The filter is one line,
`.filterNot { purpose == Purpose.PULSE && it.rule.family == facts.pulse.lastGeneratedFamily }`,
and 366 of its 878 firings are a day where `persistence` was the only family that qualified
and had spoken the day before, 271 are `quietDay` alone and 168 are `concentration` alone.
On those days a stage holding sixty to a hundred lines is discarded whole, at family level,
**before `VariantChoice` is ever asked for a different sentence**. That is the mechanism
under phase 9's result: 3,230 lines could not move a number decided one layer above the
bench. An 18.3 point move is larger than every previous pass together, against 5 points for
the persona repair, 2 for the presence fix and 0 for the corpus.

**Considered and rejected.** *Removing or shortening the Pulse repeat filter in this pass.*
It is a deliberate behavior rule, behavior is `MASTER_BUILD_PROMPT.md`'s authority and not a
measurement's, and letting one family speak two days running is a change a person would feel
on the first screen. The cost of keeping it is now a number rather than a guess, so the
question is recorded as open below and left to the owner. *Building a permanent per filter
counter into `Selector`.* Rejected as the less reversible option: it puts mutable
accounting inside a layer whose purity contract is a build rule, for a question asked once.
The instrumentation was a temporary patch, reverted, and `Selector.kt` checksummed back to
byte identity and confirmed unmodified against HEAD. *Believing the 535 millisecond green
this pass opened with.* Every task was up to date and the corpus files are declared inputs,
so it was probably honest, but it was somebody else's run and phase 9 has already shipped one
cached green over a corpus no test had read. *Giving the integration a ninth column.* It
would imply nine instrument readings where eight were taken, and three of the register pass's
cells were never true of any tree that also carried the bindings pass.

**Where the three corrected cells came from.** `Realizer` drops every variant in
`SlotBindings.EXCLUDED` from the bench, so the bindings pass retiring sixty two lines and
binding twenty two changes which line is picked on days neither pass considered. The report
composer cannot move either rhythm row, because `ClaritySimulator` never calls
`ReportComposer`; the seventeen corpus rewordings cannot move the numeric row, because every
one of those lines already rendered a digit out of a slot.

**Open, for the owner, with the recommendation stated and not taken.** The Pulse repeat
filter costs 574 spoken days a year across eleven personas. When it was written the deepest
Pulse stage held eight lines and refusing yesterday's family was the only thing standing
between a person and the same sentence twice; a stage now holds sixty to a hundred, and 7.6's
ninety day exclusion already guarantees a different line. **The recommendation is that the
filter is now redundant with the machinery below it and should be replaced by nothing rather
than by a shorter wait, and the recommendation is not taken here.** It is worth reading
beside the second half of the problem, which no filter change reaches: `balancedAcrossFour`
speaks nine times, all between January 5 and January 20, and is then filtered on 348
consecutive days. For that life silence is an absorbing state rather than a rate.

**Revisit if** the owner rules on the repeat filter, at which point the 574 is the number to
re-measure against and the ninth measurement is the run that does it; or if the Pulse is ever
measured after 17:00, which is still the one column no measurement has filled, since the
simulator builds only the before 17:00 window and the Pulse's reflective bench has therefore
never been read.

---

## August 28, 2026: the seventh measurement, and what a deeper bench cannot buy

The pass that compiled phase 9. Eight sessions wrote 3,230 corpus lines across three
volumes without running Gradle once. **All of it builds and every gate is
green:** `verifyClarity` passes at 991 tests, including the eight corpus gates, the render
gate over 11,867 harvested fact sets, the anchors test, the totals audit, language hygiene
and the merged manifest check.

**No corpus line was written, added, reworded or removed in this pass.** It measures, it
records, and it produces the review the owner asked for.

### Decided

**Phase 9's authoring is finished and no further authoring is scheduled against silence.**
Every hot bench in all three volumes is inside 11.1's band, the corpus is 4,733 lines
against the 1,503 the last commit records, and the seventh measurement shows that not one
silent day in eleven persona years was caused by a bench.

**`docs/CORPUS_REVIEW.md` is the review artifact**: a full simulated year each for
`queueHoarder`, `abandoning` and `balancedAcrossFour`, annotated with family, stage,
register and variant key, with every `hardStretch` line flagged by name and all five of the
run's `hardStretch` firings listed whether or not they fall inside those three years.
Chosen as the corners rather than as samples: the most talkative year at 37 percent silence,
the hardest year at 88, and the year that hears nine Pulses in three hundred and sixty five
days at 97.

**11.1's tier table is corrected in place** with the measured counts, 36 hot benches and 18
warm rather than the phase 5 estimates of fifteen and thirty, and two gaps in it are named:
eight families fall between the warm ceiling of 20 firings and the hot floor of 40, and six
fire between 335 and 1,308 times a year with no tier above them.

**The register finding is recorded and not acted on.** `RegisterChoice.preference` returns
registers in order and `Realizer.realize` takes the first it can fill, so one register
speaks per surface and a sixty line stage bench offers twenty lines. Changing that is a rule
change and belongs to whoever owns 7.4.

**The simulator's open hour stays at 07:00**, so the seventh measurement is comparable with
the six before it, and the eighth is where the evening Pulse gets measured.

**Every file `CorpusFixture` reads is now a declared input of the test task**: the three
corpus volumes, `docs/CORPUS_ANCHORS.md` and `CLARITY_LOGIC_ENGINE.md`. None of them was,
and the gates open them off the repository root at runtime rather than off the test
classpath, so Gradle could not see the reads: a corpus edit left `testDebugUnitTest`
UP-TO-DATE and **`verifyClarity` returned green in 499 milliseconds over two thousand lines
it had never read.** Proved fixed rather than assumed: one extra byte in
`CORPUS_1_PULSE.md` now makes the task execute, and the file was restored and checked back
to its own hash.

### Why: silence did not move by one day, and that is the finding

2,067 silent Pulse days out of 3,148 opens, splitting 1,161 filtered, 895 nothing qualified
and 11 insufficient. **That is the sixth measurement's split to the day**, after 3,230 new
lines. The prediction on record was that emptying the filtered column would leave silence
near 28.7 percent; the column did not empty by a single day, and the reason is that bench
depth cannot empty it.

The brief that opened phase 9 said the filtered column "is bench depth". It is not, and the
code says so in three places:

- **`VariantChoice.choose` never falls silent on a full bench.** When every line has been
  used inside ninety days it drops the most recently used one and reuses the rest, per 7.6
  step 2. Exhaustion produces a repeat, never a silence, so depth cannot produce silence
- **`Realizer.realize` returns `NotProducible` only when no line at any register can be
  filled from the facts on hand.** That is a question about slot bindings and rule shape. A
  hundred lines carrying an unbound marker are exactly as silent as one
- **The rest is step 6 of `Selector.select`**: family availability, callback resolution, the
  horizon, the Pulse repeat filter and the cooldowns. None of the five reads the corpus

So `ALL_QUALIFIED_RULES_FILTERED` has two producers and neither is sensitive to bench size.
The empirical half of the same statement is that tripling the corpus changed the count by
zero. **The 1,161 can only be moved by a rule, a binding or a cooldown.** The owner's
standing instruction that this is reported and not ground at stands, now with a mechanism
under it rather than an estimate.

### Why: what a deeper bench did buy, and the one thing it cost

Variant repeats inside ninety days fell from **7,370 to 3,898**. Two consecutive report
leads sharing a length band fell from **719 to 277**, which nothing predicted and which is
the gate suite's length band work arriving on a surface. Layer 5 vetoed nothing. The pattern
section's slot allocation is unchanged in every figure.

**Runs of three or more parallel numeric clauses went from 41 to 121**, the one reading that
moved the wrong way. Deeper benches gave the composer more numeric leads to place beside
each other. 9.2's band rule survived the same pressure because `Realizer.choose` honors it
as a preference on every pick, and the numeric rule survived nothing because **nothing
anywhere implements it**. It is the one stated rule of section 9 with no code behind it, and
it was invisible while the benches were too shallow to expose it.

**95 percent of the repeats that remain are the Momentum headline and the areas banner**,
which recompute on every app open rather than once a day: 2,108 and 1,586 of 3,898, against
46 in the Pulse. `weekMixed` fires 1,308 times across the run against a bench of 77 of which
19 are reachable, and `bn.mixed.25` is said 101 times. No bench size holds a ninety day
exclusion against that.

### Considered and rejected

**Writing more lines to move silence.** Rejected on the measurement above: it is not that
more lines would help a little, it is that the mechanism has no path from bench size to a
silent day. Continuing would have been grinding, which the brief forbade in advance.

**Opening the simulator in the evening so the reflective Pulse bench is measured.** The
simulator builds only the before 17:00 window, so every one of its 1,081 Pulses is a morning
one and `RegisterChoice` never offers the reflective register; roughly 312 reflective and
427 observational statement lines of volume 1 are unmeasured. Rejected **here** because
changing the instrument in the same run that changes the corpus makes the seventh
measurement incomparable with all six before it, which is the exact mistake section 12
already argues against over the twelfth persona. It is the eighth measurement's first job.

**Making `RegisterChoice` or `Realizer` choose across registers.** Rejected: a rule change
under 7.4, outside an authoring brief, and it would have moved every reading in the table in
the same run as the corpus did.

**Adding a tier above 40 firings to 11.1's table.** Rejected: the bench size in that row
would be a number nobody has measured, and 11.1 has just finished replacing projected
figures with counted ones. The gap is named instead, with the six families that fall in it.

**Keeping the dump harness as a test.** It was a throwaway JUnit class that wrote each
persona's year into a scratch directory, and it is deleted. The alternatives were a test
that writes 850 kilobytes into `docs/` on every run, or a test that asserts nothing unless a
property is set, and this repository already argues against the second one in
`SimulationChecks`. Regenerating the review means writing the same forty lines again, and
`SimulationDump.of(run)` does all the work.

**Dumping all eleven personas into the review.** Rejected at 3.1 megabytes. Three corners
carry the reading and a file nobody finishes is not a review.

**Fixing `ob.focus.s3` while it was in front of me.** `focusInvestment` is warm at 36
firings, its reachable observational bench is one line because the other one is unbound, and
`highFocus` hears that single sentence thirty five times in a year while it claims a record
its rule does not establish. Rejected because the brief forbids touching a warm bench and
forbids adding a binding, and because a defect this specific deserves its own decision
rather than a drive by fix. It is written up in the review with the three lines of evidence.

### Revisit if

**The silence conclusion** is revisited if a rule, a binding or a cooldown changes, since
those are the only three things the measurement leaves able to move it. Another authoring
pass is not one of them.

**The register decision** is revisited the moment 7.4 is opened for any reason, because the
sizing target in 11.1 and the register order in `RegisterChoice` are one question asked
twice and they should be answered together.

**The simulator open hour** is revisited at the eighth measurement, which should take the
same year twice, once at 07:00 and once after 17:00, and report both columns rather than
replacing one with the other.

**The parallel numeric clause rule** is revisited when somebody implements it. Until then
the reading is a count of a rule nothing enforces, and it will keep rising with every line
added to a numeric bench.

**The test input declaration** is revisited if a corpus file is ever moved out of the
repository root, at which point the declaration and `CorpusFixture.REPOSITORY_ROOT` have to
move together or the gates go quiet again.

---

## August 28, 2026: the surfaces pass, the size control, and the contrast audit

Three passes landed in one tree in one night, and this entry carries the choices from
all three because they argue with each other in places. Phase 12b is the surfaces half
of the polish pass, issue #54. The accessibility pass is issue #51 and Addendum 01 8f.
The contrast remediation is what the audit that pass built then found.

### Decided

- The scroll edge is a **fade that erases**, drawn with `BlendMode.DstOut` over an
  offscreen layer, and never a blur and never a ground colored scrim
- `ClarityElevation.sheet` keeps its value and gets **no call site**, because it is
  unreachable through theming and would be a second separation device anyway
- The Trail's **event circle is removed** and the glyph grows inside the same slot
- **An inactive tab keeps no label**, and a new rule constrains section 7: a
  destination whose glyph cannot be recognized on its own does not get an unlabeled
  state
- A **text field is a well** on `raise`, stepping down the surface ladder, with no
  hairline
- **Nothing in this app moves at rest**, and section 14 is narrowed rather than
  satisfied
- The in app text size control **multiplies** the OS font scale, and the combined
  scale **caps at 2.0**
- The control is a **list of five steps**, not a slider, and every option label is set
  at the **current** size rather than at its own
- The **WCAG large text exemption is declined**. One floor, 4.5, for all text
- **No dyslexia friendly typeface**, per 8f
- Light `actionBlue` moves to `#004BAE`, dark holds, and **a filled action inverts its
  label to `card`** rather than using white
- `positiveGreen` splits into a fill and a new `positiveInk`, and carries no
  foreground anywhere
- The Swap swipe face **takes `actionBlue` rather than the raw area accent**
- `design-v3.md` 3.1 and 3.2's wash ranges **narrow** to exactly the depth 3.4 solves
  the area label against

### Why: the fade erases rather than paints

The obvious implementation is a rectangle in the ground color faded to transparent. It
is wrong on three surfaces in this app, and not marginally: the Report's ground is two
centers of gold light, the Pulse's shifts with the time of day, and Focus is a radial
gradient. A flat scrim would cut a visible band across a gradient at exactly the edge
the treatment exists to soften. Erasing the content's own alpha reveals what is
genuinely behind it, which is also what makes the sentence about the treatment true.

**Revisit if** a surface arrives whose ground cannot be composited against, or if the
offscreen layer proves to cost frames on the Trail's list while scrolling.

### Why: no blur, and the refusal is a test

15.1 lists glassmorphism used as decoration rather than to solve a layering problem.
Content passing under a floating bar is a real layering problem, so the entry does not
forbid an answer; it forbids reaching for the blur because it looks more modern. The
fade is the quieter answer and it is the one that is actually true about what is
behind the bar. `ScrollEdgeTest` now fails the build on `.blur(`, `BlurEffect`,
`RenderEffect` or `BlurMaskFilter` anywhere in `ui/`, because a prohibition that rests
on somebody remembering it has a shelf life.

**Revisit if** the platform grows a backdrop blur that is cheap and that solves a
layering problem this app actually has.

### Why: the sheet shadow is refused, and it costs nothing

`ClarityElevation.sheet` having no call site read as an oversight for eight phases. It
is not reachable: `ModalBottomSheet` takes no shadow parameter, and the caller's
modifier attaches to a node that stays at the top of the window while the content is
placed at an offset inside it, so a shadow drawn there would land at the top of the
screen. That was verified against the shipped artifact rather than assumed.

**And it would have been wrong even if it were reachable.** A sheet sits on a 42
percent scrim, which is a roughly 42 point lightness step under a surface at L* 98.6.
6.1 says stop at the first device that reads, and it reads at device two, so the
shadow was always the third device on an element that already had one.

**Considered and rejected: deleting the token.** It records an analysis worth keeping,
and a later Material release may grow a hook.

**Revisit if** `ModalBottomSheet` gains a shadow parameter or exposes its content
node's offset before the anchors exist.

### Why: the Trail's event circle goes

It was tinted by area, so on a one area app every circle was the same disc and the
icon column carried nothing the sentence did not. Section 11 said "the event color"
and defined it nowhere.

**Considered and rejected: a semantic event palette**, green completions, amber
Pulses, red deletes. It is the statistically common answer and it is unavailable by
construction here: 3.1 scopes `positiveGreen`, `warnAmber` and `deleteMuted` to one
job each, and 3.4 permits an area accent in four forms of which the only one a 23dp
shape may take is a 5 to 14 percent wash. Eight moods at 12 percent over the canvas
are not distinguishable at a glance, so the disc could not carry identity either.

**Considered and rejected: keeping the tint and making it louder.** 3.4 forbids the
depth that would take.

A container that can hold no information is not a container. Removing it also resolves
a contradiction that had stood since phase 3: a glyph in a tonal circle beside one
sentence is a stock Material list row, and section 14 forbids those on primary
screens.

**Revisit if** the Trail ever gains a second axis that genuinely needs a colored
container, rather than one that would merely fill it.

### Why: an inactive tab keeps no label

The shipped behavior was the Material 3 Expressive default, arrived at with no
recorded reason, which is exactly the move 15.3 names. It survives, but as a rule with
a constraint attached.

The bar is the only element in the app with a width it cannot grow out of. Icons do
not scale with `fontScale` and labels do. On a 360dp phone the bar has 314dp. At 200
percent, one label plus four icons is roughly 290dp of that and four labels roughly
570dp. Section 13 requires 200 percent without clipping, so labels-always either clips
a destination name, which is the failure the rule would exist to prevent, or grows the
one piece of chrome present on every screen and takes four screens' content padding
with it. The figures are estimated from font advances rather than measured on a
device, and the conclusion survives being a third wrong in either direction.

**10.15's no hidden navigation is satisfied and is a different claim.** All four are
always on screen, in fixed order, and each announces its name to TalkBack.

**The new rule is the part that binds later work:** a destination whose glyph cannot
be recognized on its own does not get an unlabeled state. `arrow_outward` fails it and
is recorded in 7.2's mapping table.

**Revisit if** the tab bar stops being a floating pill, or if a fifth destination
arrives.

### Why: a text field is a well

The choice was open, 10 never having said. The obvious answers are what shipped, an
underlined field with a colored focus rule, and Material's outlined or filled fields.

Walking 6.1 in the order 6.1 gives reaches neither. Whitespace genuinely fails,
because an empty field with no target is not a target. Device two does not fail. So
the field steps **down** a rank, to `raise` on a `card` sheet, and the hairline goes,
because 6.1 puts a hairline fourth and only if all three above have genuinely failed.
Those four rules were the third and fourth hairlines in the whole app.

Focus takes the well one rank further down to `canvas`, which is the same device
speaking louder rather than a second device arriving, and the caret is the second
signal.

**Revisit if** a field ever has to sit directly on `canvas`, where stepping down has
nowhere to go.

### Why: nothing moves at rest, and the sentence is narrowed instead

Section 14 ended "nothing that is still. An app that never moves is an app that feels
broken." Nothing in this app moves at rest, so that sentence was false.

**Every conventional fix is on the tell list or beside it.** 15.3 refuses the pulse,
breathe, glow loop and the ambient shimmer by name. And none of them says anything
about the person's own data, which section 1's first rule requires of anything that
takes attention.

**But the sentence was not simply false, and that is what changed it.** Four things
already move at rest and all four are time: the focus arc and numeral, an in session
card's countdown, the focus glow's breath, and the Pulse's ground shifting through the
day. Section 14 now says that and adds the amendment: **a screen with no time on it
does not invent motion to prove it is alive.**

A recorded absence is better than a tell.

**Revisit if** a screen arrives that genuinely has time on it and does not show it
moving.

### Why: the size control multiplies rather than overrides

**Overriding fails the person the feature exists for on their first screen.** Somebody
at OS 200 percent has already told their phone they cannot read 100 percent, and an
overriding app opens at 100 percent silently. The default step is exactly 1.0, so a
person who never opens the row gets their phone's figure untouched.

**Considered and rejected: overriding**, which gives one dial that means one thing, at
the cost of ignoring a setting somebody may have set deliberately for every app they
own. The cost falls on precisely the audience Addendum 01 is written for.

**The 2.0 cap is a measurement rather than a round number.** Every clipping analysis in
this project is written against the 200 percent condition: section 13 states it, 5.3
caps the timer numeral at 1.3x of it, and the tab bar's own note measures the pill at
roughly 290dp of the 314dp available. A control able to exceed 2.0 would invalidate all
three at once, on a device nobody has run. The cost is stated rather than hidden: a
phone already at 200 percent has no headroom, so the steps above default change
nothing, they stay tappable because 10.16 forbids the disabled control question, and
one line says the phone's own setting is what is deciding.

**Revisit if** every screen has been walked at 200 percent on a device and found to
have headroom, which is a device task and is in `HANDOFF.md`.

### Why: a list rather than a slider, and labels at the current size

A slider is continuous for a quantity that is discrete on this platform and a ladder in
5.3. A preview paragraph is a specimen in a fixed box, which is the one thing that
cannot show the half that matters, that spacing moved with the type. Settings is
already real content, so **the screen is the preview**: sideheads, row titles,
captions, a paragraph, switches and a card all re-lay out on the tap.

**The second choice matters more than the first.** Every option label is set at the
current size, never at its own. The tempting version shows the ladder at a glance, and
it renders the two smallest rows below the size a person has already told the app they
cannot read, which makes the affordance for a size control fail the need the size
control serves.

**Revisit if** the ladder ever gains enough steps that a list is a scroll.

### Why: one contrast floor, and no dyslexia friendly typeface

WCAG allows 3:1 for large text, and taking it is the common answer: it would put the
21sp item title, the 40sp display hero and the 64sp timer numeral below the audit's
reach for free.

**It is refused for a reason specific to this app rather than for taste.** `sp` is not
`pt`, so the boundary is already approximate on Android; and this pass added an in app
size control on top of the OS font scale, so every size in the app moves by a factor
the audit cannot know. **A floor that depends on a size is a floor that changes when
somebody drags a slider.** Section 13 states one number for text and the audit uses
it, keeping 3.0 for a shape rather than for a large word.

**No dyslexia friendly typeface**, per 8f, whose argument is that the evidence for
them is thin and that the same investment in size and contrast helps every condition
in Addendum 01 and everybody else. The two bundled families stay Newsreader and Hanken
Grotesk.

**Revisit if** the evidence base for a typeface changes, or if the size control is
removed.

### Why: one action color per world, and a filled action inverts its label

The audit found the same token failing in two directions: white on the light fill at
3.81 to one, and the token itself as text at 3.06 on the canvas at five call sites. A
fill has to be dark enough to carry a label and text has to be dark enough to be read.
In the light world both wants point the same way, so one value serves both. **In the
dark world no value does**: text needs a lightness above one threshold and white needs
it below a lower one, and the two do not overlap. So the word white leaves the design
and the label inverts to `card`, which is the inversion 10.8 already used for the
destructive button and the selected chip.

The binding constraint on the light value turned out to be the Swap face, because a
token has to be legible on its own tint.

**Considered and rejected: two blues, a bright fill and a darker text variant.** That
is the statistically common answer and it does not work here.

**Revisit if** a third role for the action color appears that neither value serves.

### Why: `positiveGreen` splits, and the Swap face stops using the accent

`positiveGreen` has to stay a light mint, because section 11 calls the Trail's ground a
mint and 10.7 puts a label on a 13 percent button, and its foreground has to be dark.
One value cannot do both, and that was measured rather than assumed: a single dark
green breaks the surface ladder on the 13 percent fill and still cannot reach 4.5 on
its own Complete face. `positiveInk` is new and retires a hardcoded green.

**The Swap face was the wrong shape, and the evidence is a measurement.** 3.4's remedy
for this exact problem elsewhere is to derive a readable variant of the area accent.
Applied to this ground it would move **44 of 48 colors, a median of 34 percent toward
black, five of them past half**. A color blended past half no longer identifies
anything, so the mechanism does not belong here. The face takes the token its own
ground is tinted from, matching Complete and Delete, and identity stays where 3.4 puts
it: on the card being swiped, 8dp away.

**Revisit if** the swipe faces stop being tinted from a token.

### Why: the wash range narrows rather than the label being re-solved

3.1 and 3.2 permitted a wash one point deeper than 3.4 solves the area label against.
Nothing draws it, because the shipped depth is inside both, so this was **a trapdoor
under a token that looked safe to nudge** rather than a live defect.

Narrowing the stated range moves no shipped pixel. Re-solving the label against the
wider range would have moved every area label in the app. The simpler and more
reversible option wins, and the two functions that computed the two ranges are now
one, so the trapdoor is structurally closed rather than numerically avoided. The test
asserts that one point deeper still fails, so the narrowing stays load bearing rather
than becoming a comment.

**Revisit if** a wash depth outside the narrowed range is ever wanted, in which case
the label variant is re-solved first and this entry is superseded.

---

## August 28, 2026: four calls taken during an unattended run

The owner authorized an unattended run through phase 13 with a standing instruction:
decide anything undecided, prefer the simpler and more reversible option, log it here
with one line of reasoning, and continue. These are the ones that were not covered by
a section above.

### Decided

- **The re-entry screen gets a phase**, 12c, issue #56
- `androidx.benchmark` is pinned to **1.5.0-rc02**, the one prerelease in the catalog
- The baseline profile journey is **cold start plus one fling**, and not a scripted tour
- The **anti-slop sweep is not performed**, and 15.1 is not re-dated

### Why: the re-entry screen gets a phase

14b.4 specifies the screen in full and says assigning it a phase is the owner's call.
Phase 6 built the two engine side consequences that follow it and the engine gaps pass
built the third. Shipping all three suppressions without the screen means the app is
careful not to mention an absence to somebody it then greets with nothing at all,
which is half of a good idea. It is one screen and it is finished when it works.

**Considered and rejected: folding it into phase 13.** Section 19 already makes that
argument about 12b: work buried inside a ship phase is the first thing cut when a date
moves.

**Revisit if** the owner would rather ship without it, which is a scope call and not a
correctness one.

### Why: a release candidate in the version catalog

`MASTER_BUILD_PROMPT.md` 3.3 requires checking the current stable release rather than
trusting a version named in a document. The current stable `androidx.benchmark` is
1.4.1 and **its Gradle plugin refuses to apply to this project**: it fails with
"Module :app is not a supported android module" because it inspects AGP through
interfaces AGP 9 changed. 1.5.0-rc02 applies cleanly.

**Considered and rejected: shipping no baseline profile.** Nothing else generates one,
and a slower cold start on every launch is a worse trade than a release candidate in a
module that produces a build input and ships no code into the APK.

**Revisit if** 1.5.0 final ships, at which point the pin moves and this entry is done.

### Why: the baseline profile journey is short

A baseline profile is compiled ahead of time and costs install time and disk for every
class it names, so its value sits almost entirely in the path from tapping the icon to
the first usable frame. Everything after that is already warm.

**Considered and rejected: a scripted tour** through all four tabs, the Pulse, a
session and the Report, on the theory that more coverage makes a better profile. Every
step that waits on a specific string is a step that silently stops contributing the
day that string moves, which is 3.4's failure mode exactly: a script that looks
thorough and is not.

**Revisit if** a phase that is genuinely expensive and genuinely on the critical path
is added to startup, in which case it is added here and the commit says which frame it
bought.

### Why the anti-slop sweep was not performed

15.2 makes it a release gate and 15.1 is a dated record of what the industry currently
produces. **It cannot be re-derived from inside this repository or from a model's
memory of the year before the list was written.** Phase 12b reached the same
conclusion and deliberately did not re-date the list, and this run agrees rather than
overruling it. Stamping a date on a sweep that did not happen is worse than an honest
gap.

What was honestly done: every entry of 15.1 and 15.3 was checked, one at a time,
against everything phases 3c, 12, 12b, the accessibility pass and 13 built. Nothing
built appears on the list and nothing built is refused by 15.3.

**Revisit if** somebody with current information runs the sweep. It is in `HANDOFF.md`.

---

## August 28, 2026: the sixth measurement, and two tests that had never agreed with the code

The first pass in this chain allowed to run Gradle. Three sessions wrote facts, two gates, a
veto, a cyclical persona and two simulator repairs without compiling any of it. **All of it
compiled on the first attempt and every test any of them wrote passed.** What did not pass
was two tests nobody in this chain had touched.

### The restatement test that had been red since the narrowing landed

`ReportInvariants` restates section 9 by hand so that a report satisfying the production
code and violating the document is a defect in one of the two. It was never given the one
narrowing check 1 has: a rule carrying `ClarityRule.absenceSubject` may name an area with no
events in the window when that area has a real lifetime, is not new, and has a measured
`daysSinceLastEvent`. So it refused every `neglectedArea` and `areaGoneQuiet` sentence in the
catalog while `ReportIntegrity` and `ClarityValidator` passed them both.

**358 violations over ten thousand generated weeks and 112 across the eleven persona years,
none of them a phantom area, and every one of them present at HEAD.** The failure is not new
work; it arrived with the narrowing and was committed red. The presence fix in the entry
below took the persona count from 112 to 36 without touching the cause, which is the only
reason it looked like this session's problem.

**The four conditions are restated there with their literals rather than read from
`FactExtractor`.** `AbsenceSubject` takes the constant, deliberately, because production must
not let the fact and the guard drift apart. A second encoding that reads the first one's
numbers is not a second encoding, so the five is written out and cited. The cost is that a
legitimate change to the floor fails this test, which is what the test is for.

### A gate cannot only remove, at the level of a page

`CapacityGatePersonaTest` asserted that no family speaks under the gate that did not speak
without it, citing 11.4's `never pad a section to reach a minimum`. **That is false and has
to be**: a report shows one headline and at most four observations out of everything that
qualified, so removing a candidate frees a place and the next one takes it. Sixteen lines in
the cyclical year are exactly that, and one of them is `datedFallback`, the rule `ReportRules`
calls the one meant to pass most of the time, whose entire purpose is that a headline slot is
never empty. Reading those as inventions reads 11.4 as a rule against the fallback the
catalog is built around.

**The claim belongs to the selector, and there it is true.** Step 1b is a filter over
`qualified`, so for every purpose the gated ranking is a subsequence of the ungated one: same
pairs, same order, some missing. 260 rankings compared across the year, none out of order.
That assertion fails the moment somebody makes either gate a criterion instead, because
`specificity` is `criteria.size` and a criterion reorders a ranking rather than shortening
it, which is the mistake `FamilyAvailability` spends its class comment arguing against. The
weaker claim was asserting the composer's arithmetic; this one asserts the design.

### The sixth measurement, and which half of it is the instrument

The full table is in `CLARITY_LOGIC_ENGINE.md` 12. **Silence is 65.7 percent against a
ceiling of 25, and the fifth measurement's reading of it survives.** Emptying the filtered
column entirely leaves 28.7 percent against 29.0 at the fifth: the same finding to within a
rounding, so bench depth is necessary and provably not sufficient, and phase 9 is still
authoring to fix silence rather than repeats.

**The two causes were separated rather than argued.** The sixth run is the first carrying
both the repaired instrument and the two gates, so a control ran the same year with
`FamilyAvailability.unavailable` returning null throughout. Its silence table is identical to
the sixth measurement in every cell. That was predictable from the tables alone, since no
Pulse family appears in either gate and `RE_ENTRY_PURPOSES` excludes the Pulse outright, but a
prediction and a reading are different things and only one of them is evidence. So 63 to 65
is the presence fix entirely, and the gates cost no Pulse sentence.

**What the gates cost is priced honestly.** `quietWeek` goes dark as a headline and absences
named on purpose fall from 101 to 38. Both are the mechanism working: sixty three of those
hundred and one were an area falling quiet in the same way it had fallen quiet before. The
family coverage row moves down from 71 of 78 to 69 and should, because a family going dark is
a cost wherever the reason for it lives. `abandonmentPattern` is the other one and it is the
instrument rather than the gate: `abandoning` no longer writes on days it was not there.

### One measurement was nearly reported wrong, and the harness was the reason

The first attempt at the 14b.4 reading drove the persona by hand and did not call
`SimulatorLog.opened`, so no `APP_OPENED` reached the log, `TrailQueries.lastReEntryOnOrBefore`
found no return, and `isJustBackFromAbsence` was false all year. Read at face value it says
the re-entry gate is unreachable in the simulator and the checklist line is vacuous. **The
simulator writes the open on every present day and always has**, and the true reading is that
the gate fires and removes seven sentences. The comment in `CapacityGatePersonaTest` that says
no `APP_OPENED` is written is about that test's own hand rolled loop and is correct there;
read as a statement about the simulator it is what sent the first harness wrong. The rule this
leaves: **a measurement of a surface is taken through the thing that drives the surface, and
a harness that reimplements the driver is measuring the harness.**

### A Gradle failure worth writing down, because it looks like a test failure

Two workflows were building the same checkout at the same time. The second one's
`testDebugUnitTest` clears `app/build/test-results/testDebugUnitTest` while the first is
writing into it, and the symptom is `java.io.EOFException` with a Kryo buffer underflow, or a
`NoSuchFileException` on `in-progress-results-generic.bin`, on a task whose tests all passed.
It cost a wrong diagnosis first: the output of `SimulationAggregate.of` was blamed for
overflowing Gradle's output store, a comment was written into `SimulatorTest` saying so, and
it is not true. **The fix is a separate build directory, not a smaller println**, and the
comment is corrected rather than left standing.

`SimulatorTest` now prints `SimulationAggregate.of(runs)`. Four rows of section 12's table are
quoted from that object and from nowhere else, it has been computed on every run since phase
5, and nothing printed it, so each of the six measurements began with somebody writing a
harness to see numbers the suite already had.

---

## August 28, 2026: the day the app was never opened, and a cyclical life rather than a cycle

Two defects in the same instrument, found in the same reading. The simulator wrote events
onto days no persona had opened the app, and the persona built to prove the capacity gate
was a waveform rather than a life. Both are fixed here, and one document amendment made a
day earlier is withdrawn.

**No corpus line was written, added or edited.** That is phase 9.

### Decided

**A persona is asked what it did only on a day it was there, and the install day is one of
those days.** `SimulationPersona.isPresentOn` is the single gate. The simulator,
`ReportPersonaTest` and `CapacityGatePersonaTest` all apply it and nothing else does.

**A clearing session that lands on a day nobody was there is skipped, never deferred.**

**`SimulationPersona.CYCLICAL` is rebuilt as twelve irregular episodes across a year**, and
`CapacityGatePersonaTest` now asserts 14b.9's sentence with nothing added to it: no decline,
neglect or fading observation in any of the fifty two weekly reports.

**The amendment to `MASTER_BUILD_PROMPT.md` 17 that scoped that claim to the second half of
the year is withdrawn**, along with the entry above this one that argued for it.

### Why: the day the app was never opened

`ClaritySimulator.run` asked `opensOn(day)` before writing `APP_OPENED` and running the
engine, and then called `act(log, day)` **unconditionally**, outside that branch. So
`sporadic`, which opens on 249 days of the 365, and `abandoning`, which opens on 153, wrote
`ITEM_ADDED`, `ITEM_PROMOTED` and `ITEM_COMPLETED` onto days carrying no open marker at all.
**Ninety six days across the two of them**, 52 and 44, each holding captures or completions
the app has no path to write. **The real app cannot produce that log.** Nothing is captured,
promoted, completed or focused on except through a screen, and the shell writes the marker on
the first foreground of each local day.

It is the same class of defect as the persona set in which nobody could finish a backlog,
found in the same place, and it has the same consequence: **every silence and coverage number
this project has recorded was read through an instrument that could represent a person who
was not there.** It is fixed the same way too, in the instrument rather than in each life. A
driver that has to ask two questions in the right order is a driver that will one day ask
one, so there is now one question. `opensOn` stays as the thing a life decides and is never
called by a driver; `isPresentOn` is what a driver calls.

**The install day is inside the gate and that is the half that is easy to miss.** `setUp`
writes `AREA_CREATED`, which is a screen gesture like any other, so an install day the
persona happened not to open would put the same impossible event one line earlier than the
ones the fix removes. Setting an app up is an app session, so the install day answers true
whatever `opensOn` says, and a persona whose own plan had nothing for that day simply does
nothing once the areas exist. That is what installing an app and not adding anything looks
like. It also makes the fix safe for every persona written later, including ones whose
`opensOn` is a hash that may say no on day zero.

*Considered and rejected:* moving the `opensOn` test into each `act`. Eleven copies of one
invariant, and the twelfth persona would forget it. *Also rejected:* leaving `setUp` outside
the gate and documenting that install days must be open days. A documented invariant with
nothing enforcing it is the same defect waiting on a different line.

*Revisit if:* a persona ever needs to represent something the app records without a
foreground, which today is nothing: every payload in `docs/EVENT_FORMAT.md` is written from
a screen, a widget or a tile, and all three are foregrounds.

**Skipped, not deferred, and the reason is `roll`.** Four personas gained a clearing session
in the pass before this one and some of `sporadic`'s land on days it does not open. The
alternative was to carry a pending session to the next day opened. It is rejected because
`roll` is a hash of the persona, the day and a label and of nothing else, which is the
property the whole persona file is built on and the reason a seeded `Random` was refused:
what happens on a day must not depend on how many days came before it. A pending session is
state carried across days. **`sporadic`'s session roll comes up 50 times in the year and 11
of them land on a day it is not there, so it has 39 clearing afternoons rather than 50.**
That is the correction showing and there is nothing to compensate for: an afternoon spent
clearing a list without the app open is an afternoon the app has no record of.

*Revisit if:* a persona is ever wanted whose whole point is that work happens away from the
app and is entered later, which is a real behavior and a different life.

### Why: a life rather than a waveform

The persona built a day earlier was three weeks moving and three weeks still, all year.
**A clean period passes the test and proves nothing**, because the precedent fact is then
matching on a shape no person produces: every fall the same depth, the same length, the same
distance apart, and every one of them a precedent for every other by construction. If the
gate only holds for a regular cycle then the gate is wrong, and a persona with a period in it
is the one instrument guaranteed not to find out.

**What replaced it.** Fifty three weeks of capacity written out one at a time. Twelve
episodes, one to three weeks long, arriving after gaps of two to six good weeks, bottoming
out anywhere from thirteen events in a week down to one. The recoveries are of different
heights and the days inside each week move, so two weeks of the same capacity are the same
size and never the same week.

**What is deliberately not varied, stated rather than hidden.** An episode begins on a week
boundary. A seven day bucket anchored on the window end is the grain every fact in 3.1 is
computed at, and an episode that straddled one would reach the precedent walk as two short
falls where the person had one long one. That is a real shape and a fair thing to test, and
it belongs to a persona of its own, because the reading it exercises is whether the fact
recognizes a fall that is out of phase with the grid rather than whether the gate closes on
one. Also not varied: the depth **band**. Every low week in the year is under half of this
person's normal, because `Precedent` compares two falls by band and a year of dips scattered
across two bands would be a year in which half of them are precedents for nothing. That is
also a fair persona to build and it is also not this one.

### Why the whole year can be silent, which the entry above this one said it could not

The entry above argued that the first season must be allowed to speak, because the first fall
has no precedent and the second arrives before the twelve weeks `Precedent.MIN_HISTORY_WEEKS`
requires. **The premise is right and the conclusion does not follow.** What it missed is that
the two definitions of a bad week are not the same width.

`Precedent`'s low is a week under three quarters of the subject's own normal. **No decline
family asks that question**, which is the argument that settled `CLOSES_THE_GATE` a day
earlier, read in the other direction: `quietWeek` wants a week holding fewer events than it
has days, `decliningActivity` wants three weeks falling strictly, `neglectedArea` wants seven
days of silence in an area with five lifetime events and a fortnight behind it. So a week can
be squarely inside a fall by the fact's reckoning and reach nothing that can be said out loud.

**The first eleven weeks of this persona are made of exactly those weeks.** Two episodes, one
of them three weeks long, every week in them between seven and nine events against a normal
of about twenty five, and none of them arranged so that three weeks fall strictly in a row.
By the twelfth week, which is where a precedent first becomes answerable, this person has a
fall of every length and every depth band the rest of the year contains, and everything after
it is familiar.

`home` carries the same story and needs it more, because an area's quiet weeks are its own
band and only an earlier empty stretch is a precedent for one. It is created on the first day
and not opened until the fourth week, which is ordinary and is also the only window in which
an area can be silent without `neglectedArea` being entitled to speak: under five lifetime
events and under a fortnight old, both of that family's own guards are shut. Every later
quiet stretch of `home` is measured against that fortnight, and none of them runs longer.

*Considered and rejected:* keeping the second half scoping and leaving 14b.9's sentence
qualified in section 17. It is the honest answer only if the sentence cannot be met, and it
can. *Also rejected:* a persona whose first dip arrives after week twelve, so that the app
has history before anything happens. It fails immediately: the first fall then has a full
history behind it and no precedent in it, which is `NONE`, which is the permission.

*Revisit if:* `Precedent.MIN_HISTORY_WEEKS`, the three quarters band or the half band moves.
All three are load bearing for this persona and the second test below says so by name.

### The second assertion, and why one was not enough

`CapacityGatePersonaTest` composes each week twice and the control run, with every precedent
forced to `NONE`, is the finding: **twenty nine gated observations across the year that this
app would have said before 14b.9 existed.**

The silence assertion alone would pass on a year in which a gated family never qualified,
never won its ranking, or was held off by a cooldown, and none of those is the capacity gate.
So there is a second test, and it is an assertion about the persona rather than about the
engine: every gated observation the control run produced sits on a fall whose precedent reads
`PRESENT`. Its failure message names the week, the family and the value it found instead,
which is the difference between a red build somebody can act on and one somebody has to
bisect. `NOT_IN_A_DIP` or `NONE` means the persona has drifted; `INSUFFICIENT` means something
qualified in the first twelve weeks, where no gate can help.

### What ran, and what did not

**No Gradle task and no `adb` command was run in this workflow**, per the instruction. What
was done instead is worth stating exactly, because it is stronger than reading the code and
weaker than running it: **the fact extractor's precedent walk, its bucket arithmetic and the
qualification test of all six gated rules were reimplemented outside the project and the
persona's whole year was run through them**, including a mirror of `StableHash` down to the
signed sixty four bit ordering the day picker sorts on. That model reports fifty two weekly
readings, twenty nine gated firings and zero of them ungated. It is a model and not the
suite: it does not build a report, does not rank, does not run layer five, and could be wrong
about all three. **The first `./gradlew verifyClarity` is the real check**, and the assertion
most likely to move is the second one above, whose message names what to look at.

**Every number in the fifth measurement is now a reading of the previous instrument.** The
presence fix moves `sporadic` and `abandoning` and therefore every aggregate row. The next
run of the year is the sixth measurement, and the tables in `CLARITY_LOGIC_ENGINE.md` 12,
`docs/BUILD_STATE.md` and the entry below this one are labeled current and are not until it
happens. The fix was made deliberately before phase 9 rather than after, because phase 9
authors against those numbers and re-authoring against a moved baseline is the expensive half.

---

## August 28, 2026: the gate, the veto and the criteria that read the three 14b facts

The second half of the work Addendum 01 gave phase 8 and phase 8 did not carry. The pass
before this one built three facts; this one built the things that read them: the capacity
gate of 14b.9 and the family it fires into, the week of withholding after a return of
14b.4, and the estimate delta veto and floor of 14b.8.

**Not one corpus line was written, added or edited.** That is still phase 9, and two
benches are now owed to it rather than one.

### Decided

**Both suppressions are one mechanism, at step 1b of selection, and neither is a
criterion.** `FamilyAvailability` holds two tables and `Selector` applies them between
step 1 and step 2. `WITHHELD_ON_RE_ENTRY` is 14b.4: thirteen families removed for seven
days from a return, on the Report, the Momentum headline and the Areas banner.
`PRECEDENT_GATED` is 14b.9: six families removed when the precedent for their own subject
is `PRESENT`.

**The estimate delta veto is check 11 in `ClarityValidator`**, appended rather than
inserted, with a floor enforced the way the share floor is enforced.

**The second branch of 14b.9 is a new family, `familiarDip`, declared with its three rules
and held out of the catalog** in `FamiliesAwaitingLanguage` until phase 9 authors its
bench.

**The capacity gate closes on `PRESENT` alone.** This is the one reading in 14b.9 that had
to be settled rather than transcribed, and both `CLARITY_LOGIC_ENGINE.md` 3.1 and
`MASTER_BUILD_PROMPT.md` 14b.9 said something else before this entry. See below.

### Why a filter and not a criterion

**Specificity is `criteria.size` and nothing else.** A criterion added to `quietWeek` to
make it check `!isJustBackFromAbsence` would raise that rule's specificity by one and make
it outrank a rule that genuinely required more, which is a thumb on the ranking applied to
exactly the families both sections want demoted. Section 4 also forbids padding a rule with
a trivially true criterion, and `!isJustBackFromAbsence` is true on all but seven days of a
person's life.

Both sections describe a filter in their own words. 14b.4: "every rule in those families is
**unavailable to selection** and the next ranked candidate is taken instead." 14b.9: the
gate must "**gate those families rather than merely re-word them**". A filter removes and
never reorders, which is what both sentences ask for and what the tests assert.

**Step 1b is numbered rather than inserted.** The seven steps of section 5 are cited by
number from three documents and from the tests. `PulseGeneration` already set the precedent
with its own 2b, for the same reason and in the same section.

*Considered and rejected:* a criterion on each rule, which is the obvious answer and is
where a reader expects a condition to live. It loses on specificity, on padding, and on a
third thing that only shows up at scale: fourteen families would need the criterion added
by hand and nothing would fail if one were missed. A table fails a test when a key names
nothing. *Revisit if:* a family ever needs to be withheld for a reason that is genuinely
about its own week rather than about whether it may speak at all.

### Why the capacity gate closes on `PRESENT` alone

`Precedent` has four values. 14b.9 and 3.1 both said `NONE` is the permission, `PRESENT`
is the veto, `INSUFFICIENT` is neither, and "both branches test for their own value, so a
person with too short a history gets neither sentence". Read strictly, that asks a decline
family to require `NONE`, which closes the gate on `INSUFFICIENT` **and on
`NOT_IN_A_DIP`**.

**`NOT_IN_A_DIP` is the argument, and it is not the one the sentence was written about.**
This fact's notion of low is a week under three quarters of the subject's own normal. No
decline family asks that question. `decliningActivity` reads a run of three falling weeks,
which can end on a perfectly ordinary week; `neglectedArea` reads a gap measured in days,
which can open inside a week the area was busy at the start of; `hardStretch` reads four
weeks. Requiring `NONE` would silence a true observation every time the family's trigger
and the fact's definition of a dip came apart, and **a missing sentence is the one defect
nothing on the screen reveals**.

`INSUFFICIENT` went the same way and the reasoning is separate. Closing on it would
withhold every decline observation from every install between its fourth week, where the
families first have a series to read, and its twelfth, where a precedent becomes
answerable. That is eight weeks of an app that has noticed something and decided not to say
it, on exactly the people 14b.10 says are deciding whether to keep it.

**Both documents are corrected in place rather than left standing beside the code**, per the
living documents rule, and the `Precedent` enum's own note with them. The decision is one
line, `FamilyAvailability.CLOSES_THE_GATE`, and adding `Precedent.INSUFFICIENT` to it is the
whole of the other reading; both persona tests would then measure it.

*Considered and rejected:* implementing the sentence as written and recording the
`NOT_IN_A_DIP` problem as an open question. It loses because the loss is invisible: a family
that stops firing produces no error, no veto and no line in a dump, and nobody would find it
without asking. *Revisit if:* the owner reads the two arguments above and prefers the strict
reading, at which point it is one word and two test expectations.

### Which families each gate names, and the rule that chose them

**The precedent gate is the mapping the facts phase declared, and it is not widened.** A
family is gated only where a precedent fact measures **the same quantity its claim is
about**: the activity precedent answers for `decliningActivity`, `quietWeek` and
`hardStretch`, the focus precedent for `focusHabitFading`, and the area precedent for
`neglectedArea` and `areaGoneQuiet`.

`narrowingFocus` is the family that tests the discipline and it is deliberately left out. It
is a decline by any reading, and its claim is about how many areas moved, which no precedent
fact measures. Gating it on the activity precedent would suppress a claim about breadth on
the strength of a finding about volume, and the two come apart on exactly the person this
section protects: somebody whose cycle narrows to one area without their total activity
falling has a real narrowing and no precedent for it in any fact this app holds.

**The re-entry set is derived from a stated rule as well**: a family belongs there when its
trigger is a fall, a silence, or the gap a return came back from, because those are the
three shapes an absence creates in the data.

**The gap families are the half a reader will not expect, and they are the reason the set is
not simply the decline list.** `mo.come.01` is `Back after {ageDays}` and `ob.rev.l01` is
`{areaName} moved again after {ageDays} of nothing`. Both are warm, both are true, and both
state the length of the absence in days on the first screen a returning person sees, which
14b.4 forbids in as many words: not in days, not in weeks, not as a date. `comeback`,
`areaRevival` and `comebackPattern` are in the set for that reason and for no other.

**What is deliberately absent from the re-entry set.** `queuePressure` and `growingQueues`
read a queue that grew, and a queue does not grow while nobody is there: nothing is added
either, so both boundaries are equal. `focusAbandonment` and `abandonmentPattern` need
sessions inside the window, so they describe what somebody did after coming back rather than
the fact that they were gone. Withholding a family an absence cannot trigger costs a true
observation for nothing.

*Revisit if:* a family is added whose trigger is a fall, a silence or a gap, at which point
it belongs in the table and `FamilyAvailabilityTest` will not notice on its own.

### Where the two gates overlap, and why it does not matter

A returning person is exactly the person a decline family fires on, so almost every
precedent gated family is withheld twice. **Re-entry wins**, because it is unconditional and
runs first, and it does not matter which wins, because both remove the same selection.

What would have mattered is the second branch speaking in the withheld family's place. **A
sentence about a familiar stretch of low weeks, said on the first report after a fortnight
away, is the absence measured in a kinder vocabulary.** So `familiarDip` is in the re-entry
set too, and a test asserts it, because the two mechanisms are otherwise one edit away from
fighting.

### The estimate veto, and why it is a check rather than a word

**Check 11, appended.** The ten checks of section 8 are cited by number from this file, from
`MASTER_BUILD_PROMPT.md` and from the tests, so an eleventh in the middle would renumber
them silently.

Two rules. **The language rule** vetoes any delta form anywhere in the sentence, whether or
not the sentence mentions an estimate, because 14b.8's second forbidden line, `You were off
by 140 percent`, never says the word. **The shape rule** vetoes a `Percent` slot in a
sentence that is about an estimate, where about an estimate means the sentence says so or
one of its numbers came from a measure whose id begins `estimate`. 14b.8 makes the reading a
multiple and never a percentage for a stated reason, and this is that reason enforced.

**It is a backstop and the file says so.** The prohibition is kept above this layer by
arithmetic: `TrailQueries.estimateOutcomes` divides the two magnitudes inside its own body,
no quantity of minutes exists anywhere in the fact set, and neither new measure produces
one, so `actual - estimate` is not a subtraction any rule or template can write.

*Considered and rejected:* adding `underestimated` to the banned word list in check 8, which
is two lines instead of a check. It loses twice: a veto detail naming a banned word tells a
reader months later nothing about which section was violated, and the shape rule has nowhere
to live. *Revisit if:* section 8 is ever renumbered wholesale, at which point this becomes
check 11 of eleven rather than an appendix.

**The floor is a mechanism with no subjects yet, and that is what it is for.**
`RuleBuilders.estimateFloor` is the criterion, `CatalogIntegrity.estimateRulesCarryAFloor`
fails the build on any rule whose criteria read an estimate fact without it, and
`estimatedCompletions` is the measure that carries the count to the validator so 11.4's
re-read holds. The measure reports the count truthfully whatever it is, including under
five: a measure that refused would make the ref unreadable and check 3 would veto for
untraceability, which is a true veto with the wrong reason on it.

### The reserved family, and the register that holds it

`familiarDip` is declared in `FamiliesAwaitingLanguage`, which is the mirror of
`RulesAwaitingFacts`: that one holds a family with language and no rule, this one holds a
family with rules and no language.

**The catalog cannot hold a half built family and neither check should be relaxed for it.**
`ReportWalker.finish` throws when the families declared in `EngineFamilies` and the families
found in the corpus file differ in either direction, and `CatalogIntegrity` fails a rule
naming a family the corpus does not carry. A family with a rule and no bench would qualify,
produce no sentence, and look exactly like a family that never happened to fire, which is
the failure `RulesAwaitingFacts` exists to make visible from the other side.

*Considered and rejected:* pointing the second branch at an existing family. `comebackPattern`
is the closest, and it loses on both halves: it requires the area to have moved in the
window, so it cannot speak during the fall it would be describing, and it is a pattern where
the families being relieved are mostly observations. *Also rejected:* leaving the second
branch unbuilt and recording it for phase 9. 14b.9 asks for the gate and the branch
together, and a gate whose other side does not exist is an exclusion rather than a
difference in language. *Revisit if:* phase 9 finds the family cannot be written under the
five constraints recorded with it, in which case the honest outcome is a gate that excludes
and says nothing, recorded as such rather than discovered.

**The name is `familiarDip` rather than `rhythm`.** 14b.9 says "it is a rhythm, not a
decline" in those words, which makes `rhythm` the obvious name, and it collides with
`consistentRhythm`, an existing pattern family meaning something else entirely. A family key
is not private: it is stored on every `REPORT_GENERATED` event, so it is in the export file
and in `docs/EVENT_FORMAT.md`, which is the contract a second implementation is built from.
DECISIONS C6 is the same argument about `FOCUS_ABANDONED`.

### The twelfth persona, and where it does not live

**Superseded in part by the entry above, dated the same day.** The persona's shape is
rebuilt there and is no longer three weeks moving and three weeks still; where it lives is
unchanged and the argument for that is below. The paragraph on its arranged shape and the
subsection after it are the parts that no longer describe the code.

`SimulationPersona.CYCLICAL` is three weeks moving and three weeks still, all year, and it
is **not** in `SimulationPersona.ALL`.

*Why:* `ALL` is section 12's enumeration and every measurement this project has recorded is
quoted against those eleven years: five silence readings, five family coverage readings, the
variant repeat baseline phase 9 is judged by, and the pattern section's concentration. A
twelfth life in that list would move every one of those numbers, and a reader comparing a
sixth measurement against the fifth would be comparing two instruments without being told.
This persona is not a measurement; it is the proof of a gate. *Considered and rejected:*
adding it to `ALL` and restating the five measurements, which is the thorough answer and
costs a run of the year plus five table edits to buy nothing the gate test does not already
prove. *Revisit if:* a sixth measurement is taken for its own reasons, at which point adding
it is one line and the tables are being rewritten anyway.

**One thing about its shape is arranged and it is recorded rather than hidden.** The good
week is the **middle** of the three moving weeks. A person who builds up, peaks and tapers
has no three consecutive weeks that fall strictly, so the only strictly falling run of three
weeks the year contains is one that ends in a still week, which is a week the person is
measurably low in. That matters because `decliningActivity` reads a falling run while the
precedent fact reads how low the newest closed week is, and the two would come apart on
somebody whose smooth three week descent ended at an ordinary week. **That shape is real and
it belongs to a persona of its own**, and it is the same gap `narrowingFocus` sits in.

### What the year is asserted to say, and what it is allowed to say

**The second half of this subsection is reversed by the entry above, dated the same day.**
The first season is not allowed to speak and nothing in the year is: the premise below is
right and the conclusion drawn from it is not, for the reason given there. Composing each
week twice is unchanged.

`CapacityGatePersonaTest` composes each week **twice** from the same facts and the same
firing history, once as the app now speaks and once with every precedent forced to `NONE`,
which is the year this person would have had before 14b.9. **The control run is the
finding.** A test that only asserted silence would pass on a persona nothing ever qualified
for, which is the easiest green test in the world to write.

**The first season is allowed to speak.** The first fall has no precedent because nothing
has seen its like, and the second arrives before this person has the twelve weeks of history
`Precedent.MIN_HISTORY_WEEKS` requires, so the decline families speak in the first season and
fall silent afterward. That is correct rather than a hole in the gate: a cycle needs two
turns before it is a cycle, and an app that recognized one from half of one would be
guessing. Section 17's line said "receives no decline, neglect or fading observation at all",
and it is amended to carry that qualification, because the sentence as written asks for
something no fact can support.

*Revisit if:* the owner wants the first season silent too, which is the `INSUFFICIENT`
reading above and is the same one line.

### What ran, and what did not

**No Gradle task and no `adb` command was run in this workflow**, per the instruction. Brace
and paren balance, the hygiene rules, and every API this pass calls were checked by reading
the code that declares them. **The first `./gradlew verifyClarity` is the real check**, and
one assertion is named in the handoff as the one most likely to need a second look:
`CapacityGatePersonaTest`'s claim that the second half of the cyclical year is silent depends
on the persona's shape rather than on the gate, and its failure message names the family, the
day and the precedent so the next session can tell which of the two moved.

---

## August 28, 2026: the persona defect, and the fifth measurement

The workflow the owner ordered on a finding from the rules pass: that seven of the nine
Pulse families that never fired were dark because of **the simulator's persona set rather
than the catalog**, and that every silence number taken so far had therefore been read
through an instrument that could not represent a person finishing more than they wrote
down.

Two fixes landed on that finding. This entry records what each was, what a fifth run of
the same eleven personas over the same simulated year says, which of the two fixes
actually moved the numbers, and **the determination the owner gated phase 9 on.**

**Not one corpus line was written, added or edited.** That is still phase 9.

### Decided

**Phase 9 is authoring to fix silence, not repeats.** Pulse silence is **63.9 percent** of
opened days against a band of 8 to 25. That is not near band, and the owner's rule reads
the same on a sound instrument as it did on a broken one.

**No further engine pass is proposed**, per the standing instruction. The number is
reported, the cause is named below, and the work moves on.

### The defect the instrument had

Every persona reached the log through `SimulationPersona.work`, which takes a capture
count and a completion count as adjacent parameters, and **every call site in all eleven
personas passed a completion count no greater than its capture count.** Nobody chose that.
It is what two adjacent numeric parameters invite. The result was a set of eleven
synthetic lives in which `additions >= completions` held on every single day and therefore
on every single week, and in which per area daily completions could not exceed two.

That one property made `throughput`, `netOutflow` and `intakeVsOutput` stage 3
mathematically impossible, and starved `burst`, which needs three completions in one area
in one day, and `queueDrain`, which needs three promotions. **A person who clears a
backlog on a Sunday is completely ordinary, and no persona in section 12 could do it.**

### The two fixes

**1. `SimulationPersona.clearOut`, the act `work` could not express.** A sitting down that
finishes and captures nothing, whose size comes from what had piled up rather than from a
literal beside a capture count. It works through the queue plus whatever is active, up to
a bound stated at every call site. Four personas have one, each shaped to that life:
`sporadic` has an afternoon roughly every ninth day, `longDormantRevival` has the first
week back on a list nobody kept, `queueHoarder` has a monthly household purge that never
catches up with the intake, and `fastCompleter` has a Saturday errand run. **The one that
must never have one is `acceptsEveryPlan`**, whose entire value is that it never completes,
and it does not have one.

No count anywhere is a literal, and no session was sized to a threshold. `burst` wants
three in an area in a day; the sessions are sized to what their person gets through in one
sitting, and what that produces is the measurement rather than an arrangement.

**2. `AreaFacts.queueDrainedFrom`, the fact 3.1 owed the three drain families.** The height
an area's queue fell from, in one uninterrupted fall to nothing that has held to the window
end, read backwards from the window end so that any arrival ends the fall. Null when no
such fall happened. It replaces `queueLengthAtWindowStart` in `pulse.queueDrain`,
`report.observation.queueDrained` and `report.headline.clearing`, all of which describe a
transition and all of which were reading a difference of two boundaries. A queue built on
Tuesday and finished on Saturday read as no drain at all. `RuleBuilders.drainedByFinishing`
now counts completions against the fall rather than against the boundary, because a queue
built inside the window and emptied inside it holds nothing at the boundary and the old
reading made that guard `completions >= 0` on exactly the shape the fact was declared to
reach.

### Which fix moved the numbers, measured rather than argued

The two landed in one commit, so an entry that credited the drain fact with lighting
`throughput` would have been wrong in a way nobody could later catch. **Three runs of the
year were made instead of one**, and they separate the two completely.

**Run A, the control: the current engine, drain fact included, against the pre-fix persona
set.** It is **byte for byte identical to the fourth measurement**. Not one family, not one
day, not one repeat, not one collision. 68.8 percent silence, 8 of 11 Pulse families, the
same 1,185 / 971 / 11 split, and `queueDrain`, `queueDrained` and `clearing` all still
dark.

**Run B, the probe: the fixed persona set, with `queueDrainedFrom` computed the old way**,
as the boundary queue when the queue is now empty. Everything else is current, so this
isolates the fact itself rather than the rules that read it.

**Run C is the fifth measurement**, both fixes as committed.

| reading | A: fact only | B: personas only | C: both |
|---|---|---|---|
| Pulse silence | 68.8 percent | **63.9 percent** | 63.9 percent |
| the silent day split | 1,185 / 971 / 11 | **1,099 / 903 / 11** | 1,099 / 903 / 11 |
| Pulse families that fired | 8 of 11 | **11 of 11** | 11 of 11 |
| every declared family | 65 of 78 | **71 of 78** | 71 of 78 |
| `pulse.queueDrain` | 0 | **6** | 6 |
| `report.observation.queueDrained` | 0 | 5 | **11** |
| `report.headline.clearing` | 0 | 0 | **2** |
| `report.headline.risingActivity` | 3 | 1 | **0** |
| variant repeats | 7,376 | 7,415 | 7,418 |

**Every silence number in this entry is the persona fix and none of it is the drain fact.**
A persona set that never drives a queue to nothing gives the fact nothing to read, and the
fact changed nothing at all on its own.

**The drain fact's whole measured effect is two families it lights and three it displaces,
all on the Report, and it moved no silence number even with the fixed personas present.**
It doubled `queueDrained`, from 5 to 11, and it is solely responsible for
`report.headline.clearing`, which stays dark at 0 under boundary anchoring and fires twice
with the fact. It did not move `pulse.queueDrain` by one firing, because a Pulse window is
one day long and the queue an afternoon clears was already sitting at that day's opening
boundary. The two `clearing` headlines it bought displaced exactly two others, one
`comeback` and the last surviving `risingActivity`. The six extra `queueDrained`
observations added seven observation slots and gained `firstMilestone` one, 14 to 15.
Beyond those five families, variant repeats moved from 7,415 to 7,418 and length band
collisions from 714 to 719, and every other reading in the run is identical.

**So the fact is justified on correctness and not on coverage, which is what its entry
claimed.** Without it `report.headline.clearing` cannot see a week's clearing that begins
and ends between two boundaries, and `drainedByFinishing` degenerates into
`completions >= 0` on precisely the shape the fixed personas now produce: a queue built
inside the window and emptied inside it holds nothing at either end.

### The fifth reading, beside all four before it

Eleven personas, a full simulated year each. 3,148 opens, 15,626 events, 451 reports, 419
pattern slots, 11,907 engine invocations. The four earlier columns are the tables in the
four entries below this one and none of them is changed.

| gate, `CLARITY_LOGIC_ENGINE.md` 12 | target | phase 5 | facts | bindings | rules pass | personas and the drain fact |
|---|---|---|---|---|---|---|
| Pulse silence, every persona together | 8 to 25 percent of opened days | 76 percent | 73 percent | 68 percent | 68 percent | **63 percent, 63.9 exact** |
| Pulse silence, per persona | the same band | 43 to 98 | 42 to 98 | 40 to 97 | 40 to 97 | **37 to 97, none in band** |
| Pulse families that ever fired | 11 of 11 | 6 of 11 | 7 of 11 | 8 of 11 | 8 of 11 | **11 of 11** |
| every family the corpus declares fires | 78 of 78 | not measured | 58 of 78 | 60 of 78 | 65 of 78 | **71 of 78** |
| every stage of every hot family fires | all | 29 hot, one gap | 31 hot, two gaps | 33 hot, two gaps | 35 hot, two gaps | **36 hot, one gap** |
| no variant repeats inside ninety days | none | 7,384 | 7,430 | 7,445 | 7,376 | **7,418, tightest after 1 day** |
| no family over a fifth of a year's Pulses | 20 percent | 27 to 60 | 25 to 57 | 25 to 51 | 25 to 51 | **25 to 48** |
| no two consecutive report leads share a band | none | 715 | 725 | 716 | 712 | **719** |
| no three consecutive parallel numeric clauses | none | 27 runs | 37 runs | 36 runs | 41 runs | **41 runs** |
| layer 5 vetoes across the run | none | not reported | not reported | 107, every one check 1 | 0, and 92 absences on purpose | **0, and 85 absences on purpose** |
| pattern slots, and their concentration | no family holds a section | not reported | not reported | 416 of 419, 8 families, top three 402 | 401 of 419, 12 families, top three 296 | **399 of 419, 13 families, top three 295** |
| layer 6 silence | at least 15 percent of reports | not measurable | not measurable | not measurable | not measurable | **not measurable, layer 6 is phase 9b** |

The silent day split, which is the reading the determination turns on:

| silent Pulse days, out of 3,148 opens | bindings | rules pass | now |
|---|---|---|---|
| a rule qualified and every candidate was filtered | 1,185 | 1,185 | **1,099** |
| nothing qualified at all | 971 | 971 | **903** |
| too little data to describe anything | 11 | 11 | **11** |
| total | 2,167 | 2,167 | **2,013** |
| the floor authoring alone could reach | 31.2 percent | 31.2 percent | **29.0 percent** |
| personas in band at that floor | 5 of 11 | 5 of 11 | **5 of 11** |

**Pulse silence per persona:** `queueHoarder` 37, `brandNew` 42, `sporadic` 51,
`heavySingleArea` 61, `highFocus` 61, `fastCompleter` 61, `lowFocus` 63,
`acceptsEveryPlan` 63, `abandoning` 75, `longDormantRevival` 75, `balancedAcrossFour` 97.
Not one is in the band. **Exactly four moved, and they are exactly the four that gained a
clearing session:** `fastCompleter` 92 to 61, `sporadic` 62 to 51, `longDormantRevival` 78
to 75, `queueHoarder` 40 to 37. The other seven are unchanged to the day, which is the
cleanest confirmation available that the movement is the fix and not drift.

**The four enforced checks still pass**, and one of them is worth naming: the
plan-accepting, plan-ignoring persona read 1,386 invocations and produced zero references
to a plan, a commitment, an intention or a failure to act.

### The nine dark families, measured against what the rules pass predicted

The rules pass diagnosed every one of them at the rule that carries it and moved no
threshold. It named `throughput`, `burst`, `netOutflow` and `intakeVsOutput` stage 3 as the
persona set, and `queueDrain`, `queueDrained` and `clearing` as a single anchoring error.
**Every family it named lit, the stage it named closed, and one it did not name lit
alongside them.** Nothing it diagnosed as needing something else moved.

| family | fourth measurement | now | what lit it |
|---|---|---|---|
| `pulse.throughput` | 0 | **35** | the persona fix |
| `pulse.burst` | 0 | **12** | the persona fix |
| `pulse.queueDrain` | 0 | **6** | the persona fix alone, measured |
| `report.observation.queueDrained` | 0 | **11** | the persona fix for 5, the drain fact for 6 |
| `report.headline.clearing` | 0 | **2** | the drain fact, measured, and dark without it |
| `report.headline.netOutflow` | 0 | **1** | the persona fix |
| `report.pattern.improvingThroughput` | 0 | **1** | the persona fix, and not predicted |
| `report.headline.fragmented` | 0 | **0** | needs a persona that both hoards and switches |
| `report.headline.queuePressure` | 0 | **0** | still never qualified |

`intakeVsOutput` reached stage 3 for the first time, which closes one of the two short hot
family stages. **`accumulation` stage 2 is the last one and it did not close.**
`weekendShift` and `shiftingFocus` are still dark, and `weekendShift` still has the reason
the rules pass gave it: no persona in section 12 knows what day of the week it is, so a
weekend carries the same load as a Tuesday.

**One family went dark that had been firing: `report.headline.risingActivity`, 3 to 1 to
0**, and the three runs say exactly how. Its criterion is a strictly rising three week run
of total events, which is a fragile shape, and four of the eleven lives now have an
irregular clearing session in them that makes a monotone three week run rarer: that took it
from 3 to 1. The last one was not lost but outranked, by a `clearing` headline on the same
week. It is recorded rather than chased.

### What this says about every earlier reading

**Every reading of family coverage taken before this run was a reading of the instrument.**
Six of eleven, then seven, then eight: that sequence was never measuring corpus depth or
rule thresholds. It was measuring a persona set in which nobody ever finished a backlog.
The number is 11 of 11 the first time a life in the set can, and no rule and no corpus line
changed to get there.

**Every reading of Pulse silence taken before this run was overstated by about five
points, and no more than that.** 68.8 becomes 63.9. That is the honest size of the defect
on this particular number, and it is worth stating plainly because the temptation on
finding a broken instrument is to assume the reading was broken by as much as the
instrument was. It was not. Silence was 68 percent for reasons that had almost nothing to
do with completions, and it is 63 percent now for the same reasons.

**The floor moved by two points, from 31.2 to 29.0 percent.** The floor is the days where
nothing qualified plus the days with too little data, and it is what authoring alone could
ever reach. Five of eleven personas reach the band at that floor, the same five as before,
and six do not.

**So the conclusion the fourth measurement reached survives its own instrument being
repaired.** That is the strongest thing that can be said about it. A finding that holds
after the tool that produced it is fixed is a finding about the thing measured.

### The determination

> If silence lands near band, phase 9 is authoring to fix repeats. If it does not, phase 9
> is authoring to fix silence. Those are different jobs. Say which one you are doing.

**63.9 percent against a ceiling of 25 is not near band. Phase 9 is authoring to fix
silence.**

> If after the rules pass and the hot family growth silence is still outside the band, do
> not keep grinding. Report the number, state the cause, and move on. An app that ships
> with 30 percent silence is better than one that does not ship.

The number is **63.9 percent**. The floor authoring can reach is **29.0 percent**. The
cause is that 903 of the 2,013 silent days are days on which no rule qualified at all,
which is a fact about the eleven lives and the catalog's trigger windows rather than about
bench depth, and bench depth is the only thing phase 9 moves. **Bench depth is necessary
and provably not sufficient, and no further engine pass is proposed.**

*Considered and rejected:* another rules pass aimed at the 903. It loses on the owner's
standing instruction, and it loses on its own terms as well: every remaining Pulse stage
threshold is a corpus stage header parsed by `StageRangeTest`, so moving one makes the
engine say a sentence about a day that did not happen. *Revisit if:* a real person's log
produces a silence rate materially different from 63 percent, which is the only evidence
that could reopen this, and it cannot exist before the app ships.

### Two decisions inside this workflow

**1. `clearOut` takes its size from the queue and not from a literal.** *Why:* the entire
defect was literals. A completion count written beside a capture count invites the two to
be written together, and eleven personas did exactly that without anybody deciding to. A
session that reads what piled up cannot be quietly re-tuned into agreement with its own
capture count. *Considered and rejected:* passing a completion count to `work` greater
than its capture count at four call sites, which is a smaller change and loses because it
leaves the shape that caused the defect in place and reintroduces the literal.
*Revisit if:* a persona ever needs a session whose size is genuinely fixed rather than
determined by the backlog, at which point it is a different method and not a parameter on
this one.

**2. The two fixes were separated by two extra runs rather than attributed by reasoning.**
*Why:* they landed in one commit and both had a written prediction attached, so any
attribution written from the diagnosis would have been unfalsifiable. Two extra runs cost
two recompiles and about ten minutes, and one of them returned a byte identical dump, which
is a stronger answer than any argument and reversed what the diagnosis would have credited
the drain fact with. *Considered and rejected:* reasoning from the rules and the persona
shapes, which is what the fourth measurement had to do and which would have credited the
drain fact with `pulse.queueDrain`, a family it does not move by a single firing.
*Revisit if:* two changes ever land together with no cheap way to build one without the
other, in which case the honest entry says the attribution was not separated.

### Two deferrals the owner authorized, carried forward

**Warm and long tail families stay at their current depth for v1.** 11.1 grows hot
families from four to eight lines per stage to sixty to a hundred; the warm and long tail
tiers are not grown with them. *Why:* a family that fires five times a year cannot repeat
itself inside ninety days no matter how thin its bench is, so lines added there buy variety
nobody encounters, while the same effort on a hot family moves the one column authoring can
move. The fifth reading does not change that: of the seven families that lit, five fire in
single figures across a whole simulated year. *Revisit if:* a real person's firing counts
turn out to differ from the simulated ones enough to make a warm family hot.

**Variant repeats at roughly 7,400 are the baseline phase 9 moves, not a defect.** 7,418
in this run, against 7,376, 7,445, 7,430 and 7,384 in the four before it. The number has
sat in a band 70 wide across five measurements and two of the five moved it up. *Why:*
7.6 excludes a variant for ninety days and the benches are the size phase 5 found them,
four to eight lines per stage. A bench of `n` lines firing every `d` days holds out for
`n * d` days and no longer, and the hot families fire most days. **The number is what
phase 9 moves and its movement is how phase 9 is measured.** A session that finds it high
before phase 9 has found the baseline rather than a regression. *Revisit if:* it rises
after phase 9 rather than falling, which would mean the bench grew and the selector
stopped spreading across it.

### The two contradiction fixes from the rules pass, and how they read now

Both were settled in the entry below this one and both are confirmed by this run rather
than changed by it, so they are recorded here only as readings.

**Check 1 was narrowed rather than the three silence families loosened.**
`neglectedArea`, `areaGoneQuiet` and `areaRevival` have absence as their subject, so every
candidate they produced named an area with no events in the window and layer 5 vetoed all
107 of them. `ClarityRule.absenceSubject` and `AbsenceSubject` now hold the one exception,
gated on a real lifetime, not being new, and a measured `daysSinceLastEvent`. **This run
records 0 vetoes and 85 absences named on purpose**, against 92 in the fourth. The drop is
the persona fix: an area that gets cleared out is an area that had events, and four lives
have fewer silent areas than they did.

**`insufficientData` left the engine.** Its rule required `weeksOfData < 3` and the
composer only asks for a pattern at `weeksOfData >= 3`, so the two were complements and the
family's four authored lines were unreachable. `ReportRules.RENDERED_DIRECTLY` records it,
`ReportComposer` renders it through `ReportLanguage`, and layer 5 still validates the line.
It is the reason `REPORT_PATTERN` reads 13 of 16 rather than 13 of 15: the family is
counted and will never fire, and `SimulationChecks` says so on its own failure line.

### The pattern cooldown, and what three weeks actually bought

`Selector.PATTERN_COOLDOWN_DAYS` is 21, applied with `maxOf` against whatever the family
declares and only to `Purpose.REPORT_PATTERN`. Measured across two runs now:

| pattern section | before the cooldown | after it | now |
|---|---|---|---|
| slots filled, of 419 | 416 | 401 | **399** |
| families that ever held one | 8 | 12 | **13** |
| share taken by the top three | 402 of 416, 97 percent | 296 of 401, 74 percent | **295 of 399, 74 percent** |

It did what was predicted and no more. Three weeks rotates two pairs at the head, which is
why the top three fell to roughly three quarters and stopped there; reaching the four
families still starved would take eight weeks, and that number is the owner's. It cost
fill exactly as predicted, and the persona fix cost two slots more. *Revisit if:* the
owner wants the remaining pattern families to hold a slot, at which point the number is
eight weeks and the price is more empty slots.

### What ran, and what did not

**No Gradle task and no `adb` command was run in this workflow.** The readings were
produced the way the second, third and fourth measurements produced theirs: by compiling
`domain`, `data.event` and `devtools` with a driver out of tree against the committed
corpus files and running the year. **Three runs of the full year were made**: the fifth
measurement, the control on the pre-fix persona set, and the boundary anchored probe. All
three compiled clean with warnings as errors, and neither the control nor the probe touched
a file in the repository. **The unit suite has not been run on this work**, and the closing
build settles whether `verifyClarity` is green.

---

## August 27, 2026: the rules pass, and the fourth measurement

The pass the owner ordered after reading the third measurement. The finding it was
ordered on was that Pulse silence at 68 percent was **mostly trigger windows and two
specification contradictions rather than corpus depth**, and that if authoring emptied
the filtered column entirely, silence would still be 31 percent and six of eleven
personas would still be outside the band.

This entry records what the pass changed, what the fourth run of the same eleven
personas over the same simulated year then said, and the determination the owner asked
for: **which job phase 9 is.**

**Not one corpus line was written, added or edited.** That is phase 9 and it comes
after this.

### The readings, beside the three earlier baselines

Eleven personas, a full simulated year each. 3,148 opens, 15,281 events, 451 reports,
419 pattern slots, 11,901 engine invocations. The three earlier columns are the tables
in the three entries below this one and none of them is changed.

| gate, `CLARITY_LOGIC_ENGINE.md` 12 | target | phase 5 | facts | bindings | after the rules pass |
|---|---|---|---|---|---|
| Pulse silence, every persona together | 8 to 25 percent of opened days | 76 percent | 73 percent | 68 percent | **68 percent, unchanged** |
| Pulse silence, per persona | the same band | 43 to 98 | 42 to 98 | 40 to 97 | **40 to 97, unchanged** |
| Pulse families that ever fired | 11 of 11 | 6 of 11 | 7 of 11 | 8 of 11 | **8 of 11, unchanged** |
| every family the corpus declares fires | 78 of 78 | not measured | 58 of 78 | 60 of 78 | **65 of 78** |
| every stage of every hot family fires | all | 29 hot, one gap | 31 hot, two gaps | 33 hot, two gaps | **35 hot, the same two gaps** |
| no variant repeats inside ninety days | none | 7,384 | 7,430 | 7,445 | **7,376, tightest after 1 day** |
| no family over a fifth of a year's Pulses | 20 percent | 27 to 60 | 25 to 57 | 25 to 51 | **25 to 51, unchanged** |
| no two consecutive report leads share a band | none | 715 | 725 | 716 | **712** |
| no three consecutive parallel numeric clauses | none | 27 runs | 37 runs | 36 runs | **41 runs** |
| layer 6 silence | at least 15 percent of reports | not measurable | not measurable | not measurable | **not measurable, layer 6 is phase 9b** |

Three readings the earlier tables did not carry, because the rules pass is the first
one that could move them:

| reading | before | after |
|---|---|---|
| layer 5 vetoes across the whole run | 107, every one of them check 1 | **0** |
| pattern slots, and how concentrated they are | 416 of 419 filled, 8 families ever held one, the top three took 402 of 416 | **401 of 419 filled, 12 families held one, the top three took 296 of 401** |
| areas named with no events in the window, on purpose | 0, because all 107 were vetoed | **92** |

The four enforced checks still pass.

**Family coverage in full:** PULSE 8 of 11, REPORT_HEADLINE 13 of 17,
REPORT_OBSERVATION 20 of 21, REPORT_PATTERN 12 of 16, MOMENTUM_HEADLINE 8 of 8,
AREAS_BANNER 4 of 5. The five families that gained a voice are all on the Report:
`neglectedArea` 71 firings, and the pattern families `growingQueues` 55,
`areaGoneQuiet` 21, `focusHabitForming` 19 and `broadeningFocus` 1. **One of the
thirteen still counted quiet left the engine on purpose** and cannot ever fire here:
`insufficientData` is rendered by the Report itself, and `SimulationChecks` now says so
on its own failure line rather than leaving a reader to chase it.

**Pulse silence per persona**, unchanged to the day: `queueHoarder` 40, `brandNew` 42,
`heavySingleArea` 61, `highFocus` 61, `sporadic` 62, `lowFocus` 63, `acceptsEveryPlan`
63, `abandoning` 75, `longDormantRevival` 78, `fastCompleter` 92, `balancedAcrossFour`
97. Not one is in the band.

### The reading the owner asked for, and it decides phase 9

**Silence is 68 percent, and it did not move by a single day.** 2,167 silent Pulse days
out of 3,148 opens, splitting into **1,185 where a rule qualified and every candidate
was filtered, 971 where nothing qualified at all, and 11 with too little data.** Those
are the same three numbers the third measurement produced, in the same order, from the
same personas.

**The cause is not a failure of the pass. It is that the pass moved the Report and the
Pulse was never in its reach.** Of the three instruments built here, two are Report
instruments by construction: the pattern cooldown applies to `Purpose.REPORT_PATTERN`
selections and the check 1 narrowing is consumed by `ClarityValidator` and
`ReportIntegrity` on families that only exist on the Report. The third, the
`drainedByFinishing` guard, is the only one that touches a Pulse rule, and it touches
`queueDrain`, which fired zero times before the pass and zero times after. **A pass with
no Pulse instrument in it cannot move a Pulse number**, and it did not.

**So the determination is silence, not repeats.**

> If silence lands near band, phase 9 is authoring to fix repeats. If it does not,
> phase 9 is authoring to fix silence.

68 against a ceiling of 25 is not near band. **Phase 9 is authoring to fix silence.**

**What authoring alone could reach from here, measured rather than estimated.** If
phase 9 emptied the filtered column completely, which is the most a deeper bench can
ever do, 982 silent days would remain out of 3,148, which is **31 percent**, still six
points above the ceiling. Per persona at that floor: `queueHoarder` 8, `sporadic` 9,
`brandNew` 14, `highFocus` 16, `balancedAcrossFour` 21, `abandoning` 26,
`lowFocus` 42, `heavySingleArea` 44, `acceptsEveryPlan` 45, `fastCompleter` 47,
`longDormantRevival` 50. **Five of eleven personas reach the band and six do not**, and
the six are not the worst six today: `balancedAcrossFour` is the worst persona in the
run at 97 percent and lands in band at the floor, while `lowFocus` sits at 63 today and
would still be at 42.

**That is the same floor the third measurement reported, and it is worth saying why it
did not improve either.** The floor is the days where nothing qualified plus the days
with too little data, and moving it means making a rule qualify on a day it does not
qualify on now. The rules pass deliberately moved no threshold, for the reason the next
section gives, so the floor could not move.

**The owner's standing instruction applies and this entry stops here.**

> If after the rules pass and the hot family growth silence is still outside the band,
> do not keep grinding. Report the number, state the cause, and move on. An app that
> ships with 30 percent silence is better than one that does not ship.

The number is 68 percent, the floor is 31 percent, the cause is stated above, and no
further rules pass is proposed.

### What the pass decided, and every one of them is recorded here

Six decisions were made by the builders in this pass. Each is a decision rather than a
repair, and each is stated with what lost.

**1. The pattern section waits three weeks, and the floor sits on the selection rather
than on the family.** `Selector.PATTERN_COOLDOWN_DAYS` is 21, applied with `maxOf`
against whatever the family declares, and only to `Purpose.REPORT_PATTERN`. *Why:* a
report is recorded against its week start key and selected against its week end, so two
consecutive reports are 14 days apart on the only clock `FiringHistory` keeps, and the
flat Report 14 holds nobody out of anything. That is what 416 of 419 slots filled with
three families taking 402 of them looks like from the other end. *Considered and
rejected:* raising the number on the declaration in `EngineFamilies`, which loses
because `decliningActivity` is a headline family and a pattern family at once and the
two share one `(family, subjectId)` key, so a longer declaration would have held the
headline back as well and only the pattern section was ordered to wait. *What it did
not buy, stated in advance and confirmed by the run:* three weeks rotates two pairs at
the head, so it raised the holders from 8 to 12 and dropped the top three from 96
percent of filled slots to 73, and it did not reach all seven starved families, which
would take eight weeks. It also cost fill, exactly as predicted: the pattern section
went from 3 empty slots to 18. *Revisit if:* the owner wants the remaining four pattern
families to hold a slot, at which point the number is eight weeks and the cost is more
empty slots.

**2. Check 1 is narrowed rather than the three silence families loosened.**
`ClarityRule.absenceSubject` marks a rule whose subject is an area's silence, and
`AbsenceSubject` in `domain.engine.validate` holds the one exception both
implementations of check 1 consult. A flagged rule may name an area with no events in
the window only when the area has at least `FactExtractor.NEGLECT_LIFETIME_EVENTS`
lifetime events, is not new, and has a measured `daysSinceLastEvent` rather than the
never sentinel. *Why:* this was the specification conflict the third measurement
recorded as open and refused to settle. `neglectedArea`, `areaGoneQuiet` and
`areaRevival` exist to say an area has been still, so every candidate they ever
produced named an area with no events and every one was vetoed, 107 times across a
year, all of them check 1. The rule's precondition and the validator's were exact
opposites. The owner ruled that the check was right and the writing was wrong: check 1
exists to stop a phantom, meaning an area that never had activity being named as though
it had, and a family whose subject **is** the absence has to name an area with no
events. *Considered and rejected:* dropping check 1 for area subject rules, which loses
because a brand new empty area would then be nameable and that is the failure a person
cannot recover from. *Result:* vetoes went from 107 to 0, 92 absences are named on
purpose across the run, and no phantom appeared. *Revisit if:* a fourth family wants the
flag. A rule that wants it because its candidates are being vetoed is a rule with the
wrong criteria.

**3. `AbsenceSubjectRules` is a static key set rather than a catalog threaded into the
validator.** *Why:* `ClarityValidator` is constructed with a zone and nothing else at
five call sites, and layer 5 is handed a `Candidate` carrying a rule key rather than a
rule. Threading a catalog through all five to read one boolean would create a place
where somebody could hand the validator a catalog whose flags disagreed with the one
the engine selected from, which is a worse failure than the one it solves. An unknown
key answers false, so a hand built test candidate with an invented key is not quietly
granted the exception. *Revisit if:* `absenceSubject` ever stops being decidable from
the rule key alone.

**4. A queue that emptied has to have been finished, not deleted.**
`drainedByFinishing()` requires `completionsInWindow >= queueDrainedFrom` and is
carried by `pulse.queueDrain` at both stages and by `report.observation.queueDrained`.
**It read `queueLengthAtWindowStart` when this entry was written and it does not now**: the
entry below this one replaced the boundary pair with `AreaFacts.queueDrainedFrom`, the
height of the fall itself, and moved the guard onto it. The correction is not cosmetic. A
queue built inside the window and emptied inside it holds nothing at either boundary, so
the old reading made this guard `completions >= 0` on exactly the shape the drain fact was
declared to reach: a person who added five things and deleted all five would have been told
they cleared them.
*Why:* `RollupFacts.queueDrainedAreaIds` never asks how the items left,
and both benches claim somebody finished something.
The realizer may select any line on a bench, so one false line is enough to require the
guard. It is stricter than the truth by one item in exactly one case, an area that
began the window holding a queue with nothing active, and one item of slack toward
silence is the right direction for a family that claims a completion. *Alongside it,
`drained.hadAQueue` was deleted as padding*: membership in `queueDrainedAreaIds` already
carries a starting queue of three or more, so restating it could never separate one fact
set from another and bought a free point of specificity, which is the one number
`ClarityRule` says must never be authored. *Revisit if:* the drain facts ever record how
an item left a queue, at which point the guard becomes exact instead of conservative.

**5. `insufficientData` leaves the engine.** `ReportRules.RENDERED_DIRECTLY` names it,
`ReportComposer` renders it through `ReportLanguage.insufficientData`, and
`CatalogIntegrity.everyFamilyHasARule` reads the new register. *Why:* the rule was
unreachable by construction. The composer asks the engine for a pattern only when
`weeksOfData >= 3` and the rule required `weeksOfData < 3`, so the two conditions were
complements and the family had four authored lines that could not be spoken. It is also
not a pattern: its four lines say there is not yet enough history to see a shape, with
no subject, no number, no escalation and no claim about the person. **This is an owner
authorized exception to the rule that every sentence about a person's own data comes
through the engine, and it is narrow: an empty state makes no claim about anybody's
data.** *What is not skipped is layer 5.* The line is still chosen with `VariantChoice`,
rendered with `SlotRenderer` and validated with `ClarityValidator`, exactly like the
three benches the Report already rendered itself. What is skipped is rule selection,
which is the layer that decides whether something is worth saying, and here there is
nothing to decide. *Considered and rejected:* making the composer ask for a pattern it
has already decided it does not want so that a rule can answer that there is none,
which is machinery in the shape of a section header. *Revisit if:* anybody proposes a
second entry in `RENDERED_DIRECTLY`. Add one only for a bench that makes no claim about
a person, and never to park a rule that is inconvenient to write.

**6. Not one threshold was moved, and the nine dark families were diagnosed instead.**
This is the decision that most needs its reason recorded, because the pass was ordered
on the finding that trigger windows were the problem and the obvious response was to
move them. *Why not:* every stage threshold in a Pulse ladder is a corpus stage header,
parsed out of the corpus files and asserted against by `StageRangeTest`, so lowering one
would make the engine say a sentence about a day that did not happen. The diagnoses, all
of them now written at the rules that carry them:

- **`throughput`, `burst`, `netOutflow`, `accumulation` stage 2 and `intakeVsOutput`
  stage 3 are the persona set, not the rule.** Every persona reaches the log through
  `SimulationPersona.work` and every call site passes completions no greater than
  captures, so `additions >= completions` on every simulated day and therefore every
  simulated week. No persona completes three things in one area in one day either.
  Three completions in one area in one day is an ordinary Saturday and no simulated
  life has one.
- **`queueDrain`, `queueDrained` and `clearing` are a genuine anchoring error, and the
  only one among the nine.** `queueLengthAtWindowStart` reads the queue at the boundary
  the window opened on, and every line of those families describes a transition. A queue
  built on Tuesday and finished on Saturday reads as no drain at all. The fix is a fact
  and not a criterion: the mirror of `AreaFacts.dormantDaysBeforeReturn`, meaning the
  queue the area was holding immediately before the promotion or deletion that took it to
  zero, null when it did not reach zero in the window. It is not declared in 3.1 and no
  criterion can approximate it from what is.
- **`fragmented` needs a persona, not a threshold.** Its switching criterion held ten
  times in the run and never beside the other two, because no simulated life both hoards
  and switches. `QueueHoarder` clears the first two criteria most weeks and never swaps
  once all year; `HeavySingleArea` is the only persona that swaps and it finishes three
  or four things a week.
- **`weekendShift` is a persona blindness too.** No persona in section 12 knows what day
  of the week it is: every one acts off a day index, so weekend days carry the same load
  as weekdays and four consecutive empty weekends can only happen by coincidence, which
  is what its single occurrence was. Its bench is also wider than its rule, and splitting
  the bench is authoring work.

*Revisit if:* the personas change. Five of these nine are a statement about eleven
synthetic lives rather than about the rules, and the honest reading is that the
simulator cannot currently tell a wrong threshold from a life nobody in section 12
lives.

**7. `CLEAR_FLOW_MARGIN` keeps its number and loses its reasoning.** Three, still, and
the argument that chose it is replaced rather than left standing. *Why:* the original
was that two is where the Pulse `accumulation` and `throughput` ladders begin and a
headline should not fire on the margin a daily note uses, which compares a week to a day.
A weekly margin set one above a daily one is a weaker claim per day, not a stronger one,
so the number was being justified by an argument for a different number. It stays on its
own weekly terms: it is the floor for both directions, and `netInflow` at this margin
took 34 of 451 report windows, which is a headline that means something rather than one
that is always there. *Revisit if:* somebody wants `netOutflow` to fire. Lowering this
will not do it, for the reason in decision 6.

### Two deferrals the owner authorized, recorded so they are not rediscovered as defects

**Warm and long tail families stay at their current depth for v1.** 11.1 grows hot
families from four to eight lines per stage to sixty to a hundred; the warm and long
tail tiers are not grown with them. *Why:* the readings above say where depth buys
something. A family that fires five times a year cannot repeat itself inside ninety days
no matter how thin its bench is, so lines added there buy variety nobody encounters,
while the same effort on a hot family moves the one column authoring can move. *Revisit
if:* a real person's firing counts turn out to differ from the simulated ones enough to
make a warm family hot.

**Variant repeats at roughly 7,400 are the honest baseline, not a defect.** 7,376 in
this run, against 7,445, 7,430 and 7,384 in the three before it. *Why:* 7.6 excludes a
variant for ninety days and the benches are the size phase 5 found them, four to eight
lines per stage. A bench of `n` lines firing every `d` days holds out for `n * d` days
and no longer, and the hot families fire most days. **The number is what phase 9 moves,
and its movement is how phase 9 is measured.** It is expected to stay high until then,
and a session that finds it high has found the baseline rather than a regression.
*Revisit if:* it rises after phase 9 rather than falling, which would mean the bench
grew and the selector stopped spreading across it.

### What the fourth measurement did not settle

- **Whether 68 percent is what a person would see.** Every diagnosis in decision 6 that
  blames the persona set is a statement about eleven synthetic lives. The simulator is
  the only instrument this project has and it cannot distinguish a threshold that is
  wrong from a life nobody in section 12 lives.
- **The two hot family stages that are short.** `accumulation` never reaches stage 2 and
  `intakeVsOutput` never reaches stage 3, both for the reason in decision 6, and both
  unchanged across four measurements.
- **Three parallel numeric clauses went from 36 runs to 41.** More observation slots
  filled, `neglectedArea` among them, and more of the leads that filled them are numeric.
  It is a corpus and ordering property and it is phase 9's, along with the 712 length
  band collisions.

### What ran, and what did not

**No Gradle task and no `adb` command was run in this pass.** The readings were produced
the way the second and third measurements produced theirs: by compiling `domain`,
`data.event` and `devtools` with a driver out of tree against the committed corpus files
and running the year. The devtools sources compile clean with warnings as errors. **The
unit suite has not been run on this work**, and the closing build settles whether
`verifyClarity` is green and whether `SimulatorTest`, `PatternCooldownTest`,
`DarkFamilyRulesTest` and the changed validator and report tests all pass.

---

## August 27, 2026: the slot bindings, and what a third measurement says

The bindings the facts phase deliberately left out, and the third run of the same
eleven personas over the same simulated year. `SlotBindingsTest` had already stated
what the gap cost: **a family with a rule, markers in its lines and no binding can
only ever speak through the lines that happen to have no marker in them.** Nine
families were in that state. Each has a binding now, and this entry is what the year
says afterward.

**Not one corpus line was written, added or edited.** The owner's condition stands:
none is until Pulse silence is inside the band and every family fires. Neither is
true yet, and this entry says by how much and why.

### The readings, beside the two earlier baselines

Eleven personas, a full simulated year each. 3,148 opens, 3,148 Pulse and banner
invocations, 451 reports, 419 pattern slots, 11,833 engine invocations. The phase 5
column is the table in the phase 5 entry and the facts column is the table in the
entry below this one; both are unchanged.

| gate, `CLARITY_LOGIC_ENGINE.md` 12 | target | phase 5 | facts phase | after the bindings |
|---|---|---|---|---|
| Pulse silence, every persona together | 8 to 25 percent of opened days | 76 percent | 73 percent | **68 percent** |
| Pulse silence, per persona | the same band | 43 to 98 | 42 to 98 | **40 to 97** |
| Pulse families that ever fired | 11 of 11 | 6 of 11 | 7 of 11 | **8 of 11** |
| every family the corpus declares fires | 78 of 78 | not measured | 58 of 78 | **60 of 78** |
| every stage of every hot family fires | all | 29 hot, one gap | 31 hot, two gaps | **33 hot, the same two gaps** |
| no variant repeats inside ninety days | none | 7,384, tightest after 1 day | 7,430 | **7,445, tightest after 1 day** |
| no family over a fifth of a year's Pulses | 20 percent | 27 to 60 | 25 to 57 | **25 to 51** |
| no two consecutive report leads share a band | none | 715 across 451 reports | 725 | **716** |
| no three consecutive parallel numeric clauses | none | 27 runs | 37 runs | **36 runs** |
| layer 6 silence | at least 15 percent of reports | not measurable | not measurable | not measurable, layer 6 is phase 9b |

The four enforced checks still pass: no banned word, dash, emoji or character above
ASCII in any sentence of any persona's year, no sentence naming an area with no
events in its window, no visible slot marker, and nothing in the plan accepting
persona's 1,386 invocations referencing a plan, a commitment, an intention or a
failure to act.

**Pulse silence per persona**, as a share of the days that persona opened the app:
`queueHoarder` 40, `brandNew` 42, `heavySingleArea` 61, `highFocus` 61, `sporadic`
62, `lowFocus` 63, `acceptsEveryPlan` 63, `abandoning` 75, `longDormantRevival` 78,
`fastCompleter` 92, `balancedAcrossFour` 97. Not one is in the band. The best is
fifteen points above its ceiling.

**Per family firing counts across the whole run.** This is the number that replaces
"only six of eleven families ever fired" as a sentence somebody remembered.

| purpose | fired | never fired, and each has a rule |
|---|---|---|
| PULSE, 8 of 11 | `persistence` 268, `quietDay` 243, `concentration` 170, `accumulation` 143, `rebalance` 111, `freshStart` 25, `switching` 16, `spread` 5 | `throughput` (4 rules), `burst` (2), `queueDrain` (2) |
| REPORT_HEADLINE, 13 of 17 | `comeback` 96, `personalBest` 84, `mostActiveSince` 76, `balanced` 60, `netInflow` 34, `steadyPace` 29, `decliningActivity` 21, `focusProtected` 16, `singleFocus` 11, `datedFallback` 9, `firstWeek` 8, `quietWeek` 3, `risingActivity` 3 | `clearing`, `fragmented`, `netOutflow`, `queuePressure` |
| REPORT_OBSERVATION, 19 of 21 | `intakeVsOutput` 255, `areaRevival` 230, `queuePressure` 177, `areaBalance` 114, `timeOfDay` 113, `persistentItem` 101, `personalBest` 84, `mostActiveSince` 83, `completionSplit` 79, `selfReportVsData` 51, `steadyPace` 47, `singleFocus` 46, `focusInvestment` 36, `dayShape` 35, `focusAbandonment` 17, `quietWeek` 16, `firstMilestone` 13, `switchingBehavior` 10, `hardStretch` 8 | `neglectedArea` (2 rules), `queueDrained` |
| REPORT_PATTERN, 8 of 16 | `reportedVsActual` 233, `comebackPattern` 98, `consistentRhythm` 71, `narrowingFocus` 5, `abandonmentPattern` 4, `decliningActivity` 2, `queueEquilibrium` 2, `focusHabitFading` 1 | `areaGoneQuiet`, `broadeningFocus`, `focusHabitForming`, `growingQueues`, `improvingThroughput`, `insufficientData`, `shiftingFocus`, `weekendShift` |
| MOMENTUM_HEADLINE, 8 of 8 | `singleAreaWeek` 1,083, `balancedWeek` 780, `comeback` 682, `steadyStretch` 355, `firstDays` 139, `quietStretch` 71, `cleanSlate` 11, `strongPace` 6 | none |
| AREAS_BANNER, 4 of 5 | `weekMixed` 1,316, `weekStarting` 817, `weekBuilding` 306, `weekStrong` 33 | `weekQuiet` |

**The bindings did what they were predicted to do.** `switching` fired 16 times where
it had fired zero, and `rebalance` went from 20 firings at stage 2 only to 111 across
stages 1 and 2. Both were named in the facts phase entry as the two thirds of a
working family whose missing third was one map entry, and both now speak. The Report
pattern purpose gained one family. Every stage of every hot family fires except the
two that were already short, and they are short for a reason that has nothing to do
with bindings: `accumulation` stage 2 requires additions to exceed completions by
four to seven in a one day window, which held **zero** times in 3,148 windows, and
`intakeVsOutput` stage 3 requires output to exceed intake by five or more in a week,
which held **zero** times in 451.

**The variant repeat figure went up, and that is expected.** 7,445 repeats inside
ninety days, tightest after one day, against 7,430 before. Speaking more with the
same bench repeats more. A year of eleven lives used **272 distinct variants of the
1,056 the catalog holds**, which is the size of the authoring job 11.1 describes and
is not a defect in anything built.

### The reading: improved, and still nowhere near the band

**Situation two of the three the owner named.** Silence improved and is still far
above 25 percent. Eight points below the phase 5 baseline, five below the facts
phase, and 68 against a ceiling of 25.

**The remaining gap is bench depth and rule coverage together, and the split can be
stated as arithmetic rather than as a judgment.** The 2,167 silent Pulse days divide
into **1,185 where a rule qualified and every candidate was filtered, 971 where
nothing qualified at all, and 11 with too little data**, against 1,348, 944 and 11
after the facts phase. The bindings moved 163 days out of the first column, which is
precisely the shape they were expected to move.

Now the arithmetic that settles the question. **Bench depth is the only fix for the
first column and cannot reach the band on its own.** If phase 9 grew every bench
until not one qualifying day was ever filtered again, silence would fall to 982 days
of 3,148, which is **31 percent, still above the ceiling**. Per persona, a perfect
bench puts five of the eleven in band, `queueHoarder` 8, `sporadic` 10, `brandNew`
14, `highFocus` 16 and `balancedAcrossFour` 22, and leaves six outside it,
`abandoning` 26, `lowFocus` 42, `heavySingleArea` 45, `acceptsEveryPlan` 45,
`fastCompleter` 48 and `longDormantRevival` 50. Writing sentences is necessary and it
is provably not sufficient, and that is the single most useful number on this page.

### The eighteen families that are still dark, and what each one needs

Grouped by what would actually change them, because the three groups are three
different pieces of work and only one of them is authoring. The counts below come
from evaluating each rule's criteria one at a time over the same eleven simulated
years, which is how a family that never qualified is told apart from one that
qualified and never got a slot.

**Group one, nine families and two stages that never qualified once.** A threshold or
a window, not a bench. Nothing here is fixed by a corpus line.

- **PULSE `burst`.** Needs one area to complete three or four things (stage 1) or
  five or more (stage 2) **inside the one day Pulse window**. That criterion held 0
  times in 3,148 windows, while the areas themselves cleared the event floor 2,171
  times. The thresholds read like a week and the window is a day.
- **PULSE `queueDrain`.** Needs an area's queue to go from three or more to zero
  inside one day. `queueLength == 0` held 2,205 times and a starting queue of three
  or four held 176, and they never coincided.
- **PULSE `throughput`.** All four rules require completions to exceed additions in
  the one day window; that criterion held **0** times all year. The family cannot
  fire on any life the simulator models, at any stage.
- **PULSE `accumulation` stage 2** and **REPORT_OBSERVATION `intakeVsOutput` stage 3**,
  the two hot family stage gaps, for the same reason: a gap of four to seven in a day
  and an output surplus of five in a week, both held 0.
- **REPORT_HEADLINE `clearing`** and **REPORT_OBSERVATION `queueDrained`.** Both need
  an area's queue to have emptied during the week. Held 0 of 451.
- **REPORT_HEADLINE `netOutflow`.** Completions exceeding additions by a clear margin
  over a week, held 0 of 451. The same shape as `throughput` at the Report grain.
- **REPORT_HEADLINE `fragmented`.** Three criteria; the binding one is the active
  item changing more than once in a week, which held 10 times, never alongside the
  other two.
- **REPORT_PATTERN `weekendShift`.** Needs four consecutive weeks with no Saturday or
  Sunday event at all, which held once.
- **REPORT_PATTERN `insufficientData`.** This one is not a threshold and should be
  read first. Its rule requires **fewer than three weeks of snapshots**, and 6.3 only
  asks for a pattern **once there are three or more**. The gate that admits a pattern
  excludes the only condition this family fires on, so it is unreachable by
  construction rather than by data. It is a catalog defect, it is one line of
  reasoning to settle, and it is not phase 9 authoring work.

**Group two, two families that qualify and are then vetoed by layer 5, and the veto
is right.** `REPORT_OBSERVATION neglectedArea` qualified 124 times and
`REPORT_PATTERN areaGoneQuiet` 34. **These two are the only families vetoed anywhere
in the entire run: 107 vetoes, every one of them check 1**, "names an area which had
0 events in this window". Both families exist to observe an area that has stopped:
their rules require seven, fourteen or twenty one days of silence. Check 1 and 1.1
prohibition 3 forbid naming an area with no events in the window being described.
**The rule's precondition and the validator's precondition are exact opposites**, so
these two families can never speak as they are written. This is a genuine conflict
between section 8 check 1 and two families in `CORPUS_2_REPORT.md`, it is not a
builder's to settle, and it is recorded as open below.

**Group three, seven families that qualify and lose a slot that only exists once.**
Bench depth does not help these either, because the constraint is the number of slots
and not the number of lines.

- **`REPORT_PATTERN` holds one slot per report.** 419 existed across the run and 416
  were filled, so the section is not starved. Three families took 402 of them:
  `reportedVsActual` 233, `comebackPattern` 98, `consistentRhythm` 71. Sixteen
  pattern families are competing for 52 slots a person a year, and
  `growingQueues`, `focusHabitForming`, `improvingThroughput`, `shiftingFocus` and
  `broadeningFocus` are the ones that never win one.
- **`REPORT_HEADLINE queuePressure`** qualified on 269 of the 451 report windows and
  was never the highest ranked qualifying rule on any of them. `personalBest`,
  `steadyPace` and `comeback` are the three that took the slot in front of it, the
  first two on specificity and the third on the tie break.
- **`AREAS_BANNER weekQuiet`** never spoke across 3,148 banner windows, of which
  2,472 spoke and 551 had no rule qualify at all. Its trigger is fewer events than
  days so far this week. **A hypothesis phase 9 should test rather than a finding:**
  answering a Pulse is itself user activity, so the app speaking makes the week
  measurably less quiet, and this criterion sits close enough to the boundary for
  that feedback to matter. The criterion evaluation that found it qualifying is a
  replay without the engine's own writes, so it does not settle this one.

### What this means for phase 9

Phase 9 is **not** bench depth only. It is three pieces of work in this order, and
only the third is authoring:

1. **One catalog defect and one specification conflict**, both cheap. `insufficientData`
   cannot be reached by the gate that admits it, and `neglectedArea` and
   `areaGoneQuiet` are vetoed by the check that exists to stop phantom areas.
2. **Thresholds and windows on nine families and two stages that never qualified once.** Either the
   thresholds are wrong against the window they read, which `throughput`, `burst` and
   `accumulation` stage 2 all look like, or no simulated life lives that way, and the
   two are distinguishable only by reading the rules beside the personas. This work
   is what moves the 971 days where nothing qualified.
3. **Bench depth**, which moves the 1,185 days where something qualified and every
   candidate was filtered, and which fixes the repeat figure and the length band
   collisions along with them. It is the largest single column and it still leaves
   silence at 31 percent on its own.

**Open, and not a builder's to settle.** `neglectedArea` and `areaGoneQuiet` need
either an exception to check 1 for a family whose subject is an area's silence, or
those thirty seven lines withdrawn. **The recommendation, stated and not taken:** a
narrow exception, because check 1 exists to stop the engine inventing an area and
these two families name an area the person has and can see, with a number that is
true of it. Widening check 1 to permit an area with zero events in any sentence would
be the wrong fix and is not what is recommended.

**Revisit if** either coverage reading changes. Both are printed by the build on
every run now, so the next measurement is a comparison rather than a rediscovery.

---

## August 27, 2026: two runs of absence, a scoped exception to the streak ban

The facts phase, approved by the owner outside the issue board and run before phase 9.
This entry is the one that matters most in it, because it is the only place in the
project where a prohibition in `CLARITY_LOGIC_ENGINE.md` 1.1 has been given an
exception, and because the next person who wants a run fact will cite it.

**Decided.** `HistoryFacts` carries two new facts, `currentQuietRunDays` and
`currentSingleAreaRunDays`, with `currentSingleAreaRunAreaId` beside the second so a
run has a subject. They are a **scoped exception** to the streak ban in 1.1, approved
by the owner, and the ban itself is unchanged: there is still no `currentStreak`, no
`longestStreak`, no `daysInARow`, and `TrailQueries` still answers nothing about how
long anything has been kept up.

The exception is a shape rather than a permission, and the shape is the approval:

- **Current run only**, ending with the last day the window describes. Not a longest
  run, not a best run, not a past run
- **Capped at 30**, `HistoryFacts.MAX_RUN_DAYS`. A value at the cap says at least
  thirty and nothing more, and no rule may render it or compare it for equality
- **No per day series is stored and none is exposed**, on the facade or in any fact
  set. A single capped current value cannot be used to reconstruct an active run
- **Three criteria may read them**, named in `StreakExceptionAudit.PERMITTED`:
  `quietDay.run.2to3`, `quietDay.run.4plus`, `concentration.run.4plus`

**Why, and the reasoning is the owner's.** A run counted here is a run of **absence**.
There is nothing to accumulate and nothing to break, so the loss aversion the ban
exists to prevent cannot occur: nobody protects a quiet week, and nobody can be told
they lost one. The ban is correct and stays; it was blocking the wrong thing.

What the block had cost is measurable and it was not small. `quietDay` stages 2 and 3
have authored, approved language and had no rule, so **the app could observe one quiet
day and never a quiet week**, which is exactly backwards for this audience and more so
since Addendum 01, whose whole subject is the person who has lost several days and
does not know it. `concentration` stage 3's second branch, `four or more consecutive
days`, was blocked by the same prohibition. Three rungs of two ladders were unreachable
and phase 9 would have been asked to write more sentences for them.

**The guards that keep it scoped, named by test.** The exception is enforced
mechanically, not by review, because every way of widening it is something a careful
person would do next and none of them looks wrong at the call site.

| guard | what it forbids | test |
|---|---|---|
| `StreakExceptionAudit.undeclaredReaders` | any criterion outside the declared three reading a run, **found by probing rather than by name** | `exactly the declared criteria read a run, and they are found by probing rather than by name` |
| `firesOnAZeroRun` | a criterion that is true when its run is zero, which is a rule reading absence as evidence of activity | `a criterion reading a run in the positive direction is caught` |
| `distinguishesTheCap` | a criterion that changes its answer between 10 days and the cap, which treats `at least thirty` as `exactly thirty` | `a criterion with a threshold up at the cap is caught` |
| `runsWithoutASubject` | a single area rule that does not require the run to belong to its own subject | `a single area run rule that does not name its own area is caught` |
| the rendering check | any `Measure` whose value moves when only a run moves, which is the whole distance between a fact and a number on a screen | `no measure's value moves when only a run moves`, and `the rendering check would catch a measure that read a run` |
| the series check | any fact set member holding one entry per day, because a per day series plus a current run is a streak | `no member of a fact set is a per day series`, and `the fact type walk reaches the types it is supposed to` |
| the facts themselves | thirteen cases including the cap, the archived subject, and the two runs never being open at once | `RunFactsTest` |

`the real catalog passes every check the exception is scoped by` runs all four audits
over the shipped rules, and each of the four has a test that hands it a deliberately
wrong rule and watches it fail, because a check whose failure branch has never run is a
check nobody has verified.

**Considered and rejected.**

- **Leave the ban absolute and leave the three rungs unbuilt.** The honest reading of
  1.1 as written, and the reason the phase 5 builder recorded the gap in
  `RulesAwaitingFacts` instead of writing a rule. Rejected by the owner: it protects a
  person from a sentence about a quiet week they would benefit from reading, and it
  does so by leaving approved language permanently dead
- **A `longestQuietRun`, or a personal best of quiet days.** Rejected without argument.
  The moment a run has a record beside it there is something to protect, and the record
  is the inversion the ban is about, whether the days are full or empty
- **An uncapped current run.** Rejected. An uncapped number is a number a person can
  watch go up, and the cap costs nothing: every stage boundary the corpus draws on
  either run is at four days or fewer
- **Exposing the per day activity the runs are computed from**, which would have been
  the natural way to write the extractor. Rejected, and it is the check that would be
  hardest to add later: a capped current run plus a per day series is a streak, because
  the series says which days had something in them and the run says where the current
  stretch begins. Every series layer one hands out is weekly and at most twelve entries
  long, so seven days collapse into one number and no day can be resolved out of it
- **Bounding `quietDay` stage 1 with `currentQuietRunDays <= 1`** so the three rungs
  exclude each other, which is how every other Pulse ladder in the file separates its
  stages. Rejected, and `firesOnAZeroRun` now forbids it: a criterion that fires when a
  run of absence is **short** is reading the fact as evidence that something has been
  kept up. The escalation is carried by rule priority instead, which says stage 3
  outranks stage 2 when both describe the same day and says nothing about anybody's week
- **A convention instead of an audit**, a naming rule, a comment, a line in this file.
  Rejected because the audit finds readers by varying the fact and watching which
  criteria change their answer, so a criterion named `concentration.window.share` that
  quietly reads a run is caught exactly as one that announces itself

**Revisit if** 1.1 is rewritten; or the corpus authors a stage boundary above
`StreakExceptionAudit.SATURATION_FLOOR` on either run, which would mean the cap has
started costing a family something it can say; or a Report family is proposed as a
reader, which is deliberately absent today because a run ends with the window and the
Report speaks about a week that has closed.

**Do not widen this, and here is what to say to the person who tries.** They will cite
this entry, and their fact will be current only, capped, and backed by no series,
because those are the three properties this entry names. **Those properties are what
make an absence run safe. They are not what makes it permissible.** The permission
comes from there being nothing to lose: a run of days somebody **did** something is the
thing the ban is about, whatever it is called and however it is shaped. A capped
`currentActiveRunDays` can still go down because a person did not do something today,
and the day it goes down is the day the app has taken something from them. If the
`claims` sentence required for a fourth entry in `PERMITTED` cannot be written as an
absence, the entry is not permitted, and no audit will catch that for you because the
audit checks the shape and this is the part that is a judgment. A widening the owner
approves is a **new dated entry here that names this one**, never an edit to it.

---

## August 27, 2026: the facts phase, and what the simulator measured after it

The facts phase, approved by the owner and run before phase 9. Twelve families and
stages had authored language and **no rule at all**, because the fact their trigger
named was not declared in `CLARITY_LOGIC_ENGINE.md` 3.1. Writing them more sentences
would have changed nothing, which is why this ran first. The scoped exception to the
streak ban is the entry above; this one records everything else it settled and the
readings it produced.

### Twelve gaps, twelve facts, fourteen rules

**Decided.** Every fact in `RulesAwaitingFacts` is declared and every rule it was
holding is written. The register is now empty, and the empty list is load bearing
rather than tidy: `CatalogIntegrity.everyFamilyHasARule` reads it, so a family cannot
go quiet again without somebody deciding it should and writing the entry.

The rule count went from **92 across 78 families to 106**. Seven of the fourteen are
Pulse rules, `switching` at two stages, `rebalance` at two, `quietDay` at stages 2 and
3, and the days branch of `concentration` stage 3, which 7.3 requires to be a second
rule pointing at the same stage rather than a disjunction inside one. Seven are the
Report patterns that had language and nothing behind it.

**Every number still comes from `TrailQueries`**, per `MASTER_BUILD_PROMPT.md` 9. The
facade gained exactly one function, `eventsPerAreaByDay`, which answers the per day and
per area fold that three of the new facts each wanted at a different grain. Everything
else was answered by functions that were already there: `swapsPerArea`,
`firstEventForArea`, `lastEventForArea`, `focusSessionCounts` and `eventsPerDay`.

**Nothing was approximated, and that was the whole risk.** Every one of the twelve had
a nearly right fact sitting next to it: `window.activeDays` for a run of consecutive
days, `window.swaps` for a per area swap count, `AreaFacts.daysSinceLastEvent` for the
dormancy an area returned from. Each would have fired its family on a shape it does not
describe, and the sentence that came out would have been arithmetic nobody could fault
and a claim about a person's week that was not true.

**Revisit if** a thirteenth entry ever appears in `RulesAwaitingFacts`. It is a rule
being lost rather than a gap being recorded, now that the list has been emptied once.

### The corpus totals are corrected, and an audit now watches them

**Decided.** Phase 5 counted the corpus files, found three stated totals had drifted,
and left them as a finding on the reasoning that **a total is not a builder's to
change**. The facts phase recounted every stated total and corrected the files instead.
`CORPUS_3_MOMENTUM.md` stated 112 Momentum headlines in two places and carries 96, so
its volume total is 146 rather than 162 and the grand total is 1,503 rather than 1,519.
Inside `CORPUS_2_REPORT.md`, section 1's prose stated 176 headlines against its own
table's 158 and section 3's stated 128 patterns against 111; the tables were right and
the prose was stale.

A fourth figure was wrong and phase 5 did not catch it, because phase 5 counted lines
and this one is a product. Volume 1's surface column sums to 10,569 and its total row
stated 10,557, which is the eleven families with the twelve acknowledgment lines left
out, although the same row's line count includes them. Nothing in the file states that
exception, so the total now sums its own column.

**No corpus line was added or removed to reach any of it.** Correcting a count is not
authoring, and the owner's condition that no line is written until the readings below
improve is untouched.

**`CorpusTotalsAuditTest` now recounts every stated total** in the three corpus files
and in 11.1 against the keyed lines beneath them, and fails naming the file, the stated
figure and the counted one. That is the part that matters more than the four
corrections: the drift was found by a person reading carefully once, and the next drift
will be found by the build.

**Considered and rejected.** Leaving the totals as phase 5 left them, on the reasoning
that phase 9 might grow a file to match its stated total. Rejected: three of the four
were arithmetic mistakes rather than aspirations, a total nobody can reproduce from the
rows above it is unusable as a target, and the sizing targets in 11.1 are the thing
phase 9 actually grows toward. Also rejected: reading an acknowledgment as something
other than a Pulse surface, which would have made the stated 10,557 correct. It is
defensible and it needs a rule that is nowhere written down, which is the same defect
in a different place.

**Revisit if** phase 9 grows the corpus, which changes every one of these numbers. The
audit is what makes that a build failure with the new count in the message rather than
a discovery years later.

### The readings, beside the phase 5 baseline

The same eleven personas, a full simulated year each, run again against the rules and
the facts as this phase left them. 3,148 opens, 451 reports, 11,843 engine invocations.
The phase 5 column is the table in the phase 5 entry, unchanged.

| gate, `CLARITY_LOGIC_ENGINE.md` 12 | target | phase 5 | after the facts phase |
|---|---|---|---|
| Pulse silence, every persona together | 8 to 25 percent of opened days | 76 percent | **73 percent** |
| Pulse silence, per persona | the same band | 43 to 98 percent | **42 to 98 percent** |
| Pulse families that ever fired | 11 of 11 | **6 of 11** | **7 of 11** |
| every family the corpus declares fires | 78 of 78 | not measured | **58 of 78** |
| every stage of every hot family fires | all | 29 hot, one gap | **31 hot, two gaps** |
| no variant repeats inside ninety days | none | 7,384 repeats, tightest after 1 day | 7,430 repeats, tightest after 1 day |
| no family over a fifth of a year's Pulses | 20 percent | 27 to 60 percent | 25 to 57 percent |
| no two consecutive report leads share a band | none | 715 collisions across 451 reports | 725 collisions across 451 reports |
| no three consecutive parallel numeric clauses | none | 27 runs of three or more | 37 runs of three or more |
| layer 6 silence | at least 15 percent of reports | not measurable | not measurable, layer 6 is phase 9b |

The four enforced checks still pass: no banned word, dash, emoji or non ASCII character
in any sentence of any persona's year, no sentence naming an area with no events in its
window, no visible slot marker, and nothing in the plan accepting persona's 1,387
invocations referencing a plan, a commitment, an intention or a failure to act.

Two rows are new. **`every family the corpus declares fires` did not exist**, and its
absence is why "six of eleven Pulse families ever fired" spent a phase as a sentence
somebody remembered instead of a number something watched: a family that never fires
leaves no trace in a year of output, so every other check is blind to it. The full
reading is PULSE 7 of 11, REPORT_HEADLINE 13 of 17, REPORT_OBSERVATION 19 of 21,
REPORT_PATTERN 7 of 16, MOMENTUM_HEADLINE 8 of 8, AREAS_BANNER 4 of 5. Not one of the
twenty quiet families lacks a rule, which is `RulesAwaitingFacts` being empty seen from
the other end.

**The stage row changed what it measures**, so the two columns are not comparable and
the second is the honest one. Phase 5 read a family's ladder off the stages that fired,
which meant the highest stage anybody reached was the highest stage the check knew
about: a family whose stage 3 never fired passed by never being asked. The denominator
is now the stages that have a rule. `accumulation` never reaching stage 2 is the phase
5 gap, still open; `intakeVsOutput` never reaching stage 3 was always true and is newly
visible.

### What the readings say, and it is not what a bigger corpus fixes

**Silence moved three points and the band is 8 to 25.** 2,383 silent days became 2,303
out of the same 3,148 opens. Of the fourteen new rules,
seven ever fired, for 87 firings: `concentration` stage 3 on the days branch 48 times,
`rebalance` stage 2 twenty, `quietDay` stage 2 eight and stage 3 twice, and three of
the seven new Report patterns nine times between them. Pulse silence fell by 80 days
and the new Pulse rules fired 78 times, which is close enough to say plainly that the
phase delivered exactly what it said it would and that the shortfall is somewhere else.

Where it is, in the numbers rather than in an opinion. The silent days split
**1,348 days where something qualified and every candidate was filtered, 944 where
nothing qualified at all, and 11 with too little data**, against 1,238, 1,134 and 11 at
phase 5. **The facts phase moved 190 days out of the second column and roughly half of
them landed in the first**, which is the shape of a run where more rules qualify and
the selector then declines to speak. That is a cooldown and anti repetition problem and
it is the one thing on this page a bigger bench genuinely helps: 7.6 excludes a variant
for ninety days, and a family with four lines per stage runs out.

Three causes are now separated, and phase 9 should treat them as three pieces of work:

1. **A bench whose slots have no binding.** `switching` has two rules and fired zero
   times. All eighteen of its statements name an area and `SlotBindings` has no entry
   for the family, so every line is dropped by the realizer for an unfilled marker.
   The fact and the rule are two thirds of a working family and the third is one map
   entry. `rebalance` is the same shape and fired only at stage 2, where some lines
   carry no marker
2. **A rule no simulated life satisfies.** `throughput`, `burst` and `queueDrain` have
   ten rules between them and fired nothing across eleven years, exactly as at phase 5.
   Either the thresholds are wrong or no persona lives that way, and the two are
   distinguishable only by looking at the rules beside the personas
3. **A selector that declines to speak on a qualifying day**, 1,348 times. This is the
   one bench depth fixes

**Revisit if** either of the two coverage readings changes. The owner's condition on
phase 9 is that no corpus line is written until silence is inside the band and every
family fires, and both of those are now printed by the build on every run rather than
counted by hand from a dump.

---

## August 27, 2026: the open choices in the Report and the checkpoint

Phase 8, issue #6. `MASTER_BUILD_PROMPT.md` 6.4, 11.3 and 12.3, `design-v3.md` 8.2, 8.3,
8.4, 11.1 and 16.7, `CLARITY_LOGIC_ENGINE.md` 8 and 9, and `CORPUS_2_REPORT.md` 5, 6 and
7. Fifteen things those documents leave to the builder, settled here under `design-v3.md`
15, plus two that are not a builder's to settle and are recorded as open.

**This screen is read 52 times a year and it only has to be caught being wrong once.**
12.3 calls data integrity the prime directive and says why it does not degrade gracefully:
one fabricated area name or one off by one number permanently destroys the credibility of
everything else the app says, and the person reading it has no way to verify anything
afterwards. Several entries below therefore choose the more expensive option, and the
reason is always the same one. Slow is a fine outcome. Wrong is not.

### The window is the seven completed days before today

**Decided.** `[startOfDay(today - 7), startOfDay(today))`. Today is not in the report it
appears on. Implemented in `domain/report/ReportSchedule.kt`.

**Why.** 12.3 says "trailing 7 days ending today" and both readings are defensible. The
other one includes today so far, and it costs something a reader can see: 11.1 draws the
week as seven marks whose height is that day's activity against the busiest, and a day
three hours old drawn beside six whole ones is a claim about a day that is not over. It is
also the shape the Pulse already uses before 17:00, where the reflection period is
yesterday, whole, and it is the window the simulator has generated a year of reports
against since phase 5, so choosing the other reading would have quietly invalidated that
baseline.

**Considered and rejected: today so far, marked as partial on the ribbon**, which is a
seventh treatment on a page allowed four and a legend on a graphic that 11.1 forbids
values on.

**Revisit if** a person reads the report on a Sunday evening and finds the day they just
spent absent from it. The answer then is the cadence, not the window.

### The cadence is asked of the log, and it asks about the calendar rather than the window

**Decided.** A report is due when no `REPORT_GENERATED` event has been written since local
midnight on the Sunday that begins this week. No stored flag, no field on the payload.
Implemented in `domain/report/ReportSchedule.kt` and `ui/report/ReportCoordinator.kt`.

**Why.** "Generated on first open in a new week" is a question about the calendar and "the
trailing seven days" is a question about the window, and on any day but Sunday they name
different spans. Keying the cadence on the window's first day is the obvious shortcut and
it gives a person who opened the app on Wednesday and again on Friday two reports in one
week, because the trailing seven days had moved. Asking the log means the answer cannot
disagree with itself, survives a merge, and needs no extra state, which `CLAUDE.md` rule 6
would have made a hazard.

**Considered and rejected: a `lastReportWeek` key in DataStore**, which is engine state
living outside the log and would let two devices holding one log disagree about whether a
week had been reported.

**Revisit if** the report ever needs to be generated on a day the app is not opened. That
is a scheduled job and a different question.

### The caption beneath the ribbon states events, completions and additions

**Decided.** Those three, in that order, each read through a `FactRef` out of the report's
own consistency map rather than counted again. A total of nought is absent rather than
stated. Implemented in `domain/report/ReportLanguage.kt` and `ui/report/ReportScreen.kt`.

**Why.** 11.1 asks for one caption line reading the three headline numbers, so that the
ribbon is never the sole carrier of a claim, and does not say which three. The first is
what the ribbon draws and the second and third are the flow most of the report's own
families are about, so the caption states what the picture shows and what the prose keeps
returning to.

**Considered and rejected: completions, focus minutes and a percentage**, which is what a
weekly summary looks like everywhere else and which 15.1 warns about by name. Two of those
three are about effort rather than about the week, and a percentage in a caption invites
the reader to compare it against a target this app deliberately does not have.

**Revisit if** the ribbon's caption and the prose above it start reading as the same
sentence twice. The map already prevents them disagreeing; it does not prevent them
repeating.

### Three sideheads, and the mapping is made once at composition

**Decided.** `What you said` holds the two families that quote a stored Pulse answer,
`Focus` holds the focus families, and `Your week, honestly` holds everything else. The
section is decided by the composer, not by the screen. Implemented in
`domain/report/ClarityReport.kt`.

**Why.** 11.1 names the three labels and does not say which observation is read under
which. Two name themselves. The obvious answer was one section holding everything, which
is what the corpus's own section 2 looks like on the page, and it loses because the report
is a page of prose read 52 times a year: three sideheads are the only structure it has for
a reader to skim, and the two that are not the general one are exactly the two a person
would look for. The mapping is at composition rather than on the screen because it decides
the reading order, and the reading order is what the length band rule and the parallel
clause cap are applied against.

**Considered and rejected: letting the screen group them**, which would let a screen
re-sort a list the composer had already spread to keep two mentions of one area apart.

**Revisit if** a fourth kind of observation arrives that is neither a callback nor about
focus and is clearly not "your week". Then it is a fourth sidehead, not a reshuffle.

### A quiet day keeps its mark, at a floor, and the scale is linear

**Decided.** Ribbon mark heights run from a floor to 44dp and opacities from half strength
to full, both linearly, at a fixed gap rather than distributed across the measure.
Implemented in `ui/report/WeekRibbon.kt`.

**Why.** 11.1 gives the mark's width and radius and says height and opacity scale against
the busiest day. It does not say what a day with nothing in it looks like, and the obvious
answer is nought, which draws an empty Tuesday as no mark at all. A row with a hole in it
reads as broken rather than as calm, and it makes the absence of activity the loudest thing
on the page, which is the one thing this app never does with a quiet day. Half strength
gold measures 3.0 to one against `deepBlack`, which is 16.7's floor for a graphic and the
same floor the Pulse rhythm row arrived at. The gap is fixed because 11.1 asks the ribbon
to repeat at 60 percent scale, and a row stretched to whatever width it is given cannot be
scaled, only re-flowed.

**Considered and rejected: a square root scale**, which would make a quiet day look busier
than it was. That is a flattering lie rather than an unobvious answer, and 15 asks for the
second thing and not the first.

**Revisit if** a week with one busy day and six quiet ones reads as six equal days on the
device. The floor is the number to move, and it cannot go below the 3.0 ratio.

### The pattern break changes the optical size and leaves the point size alone

**Decided.** Newsreader at `opsz` 28, at `bodySerif`'s own 17sp, weight and line height
copied off the theme at the call site. Implemented in `ui/report/ReportBlocks.kt`.

**Why.** 11.1 says "set in Newsreader at opsz 28 rather than 17" and `bodySerif` in 5.3 is
"Newsreader 17, opsz 17", where the two numbers happen to be equal. So the instruction
reads either as change the axis or as change both, and it names only the axis. Changing
the axis alone invents no number, and `CLAUDE.md` is explicit that every dimension is
stated in dp in `design-v3.md` and that nothing may take a number from anywhere else. A
display cut set on a narrower measure over a gold ground between two full bleed rules is
legible as a difference without the paragraph shouting.

**Considered and rejected: 28sp at opsz 28**, which is larger than the sideheads and would
make the pattern the second loudest thing on a page whose loudest thing is the headline.

**Revisit if** the owner's glance on the device says the band does not read as a break.
This is the one number in 11.1 that reads two ways and it is worth looking at.

### The reveal's start times, and the stagger stops growing after five blocks

**Decided.** Eyebrow at 0, headline at 140, ribbon at 380, sections at 780, with the
section stagger capped at five steps. The last block on the longest page settles at
1,380ms. Implemented in `ui/report/ReportReveal.kt`.

**Why.** 8.2 item 12 gives the ribbon's 45ms per day, the sections' 90ms stagger and the
1.4 second ceiling, and leaves the start times open. The obvious answer is to stagger
everything, and it breaks the one number that entry states as a limit: a report carrying a
first week note, three sideheads, a pattern, a closing line and a footer is eight blocks,
which at 90ms puts the last one 720ms behind the first and the whole reveal past 1.7
seconds. Capping the stagger keeps a long report from being slower to read than a short
one, and nobody can see a cap as a fault.

**Considered and rejected: shortening the per block stagger as the page grows**, which
makes the same animation run at a different speed on different weeks, for a reason the
person cannot see.

**Revisit if** the ceiling in 8.2 changes. The table in the file is written so the arithmetic
can be redone rather than re-derived.

### No specks of light here, and the two lights do not follow the content

**Decided.** `deepBlack` with two fixed radial glows, one behind the headline and a fainter
one behind the closing line, both anchored to where those elements sit when the page is at
rest. No star field. Implemented in `ui/report/ReportBackdrop.kt`.

**Why.** Two rules meet here and the more specific one wins. 3.3 gives the Contemplative
world eight to fourteen specks and both other Contemplative surfaces take them; 11.1 says
four treatments and no more than four and calls the week ribbon the only non-text element
in the entire report. A field of specks is a fifth treatment and a second non-text element.
This is the unobvious answer, since every other Contemplative surface in this app has
stars, and the surface is lit rather than decorated. The lights are fixed for the reason
3.3 already gave the Focus surface in the same words: a light that moved when the content
changed would make the room itself feel like it had moved, and on a short report a light
chasing a scrolling headline would follow it off the top of the screen.

**Considered and rejected: specks at a lower count**, which is the same fifth treatment
with a smaller number on it.

**Revisit if** the device check says the page reads as flat beside the Pulse and the Focus
surfaces. The first move is the glow's reach, not a star field.

### Exactly one checkpoint row survives, and every checkpoint is a full rebuild

**Decided.** The snapshot table holds one row. It is written from a fold of the whole log,
compared against the state the app is running on, and a disagreement writes nothing and
clears what is stored. Implemented in `data/repo/ClarityRepository.kt`.

**Why.** A row per week accumulating forever is the obvious answer and it is wrong twice
over: nothing reads any checkpoint but the newest, each row is a serialized copy of
everything the person owns, and anything that ever did read an older one to say what a past
week held would be engine state living outside the log, which `CLAUDE.md` rule 6 forbids.
Past reports are what remain forever, in `clarity_report`, and they are a different table
for a different reason. Taking the checkpoint from the live projection would be one line
shorter and would carry any error the projection had picked up, and carry it forever,
because the next checkpoint resumes from this one and there is no later check that finds
it. The event log is the truth, so the checkpoint is taken from the event log.

**Considered and rejected: keeping a checkpoint per week for a history feature**, which is
the reason somebody would propose it and is exactly the use `CLAUDE.md` rule 6 rules out.

**Revisit if** a cold start with a year of events becomes slow enough to notice with only
one checkpoint. The answer then is how often one is taken, not how many are kept.

### Resuming is decided by a count, not by looking at the two ends of the log

**Decided.** `ClarityReplay.canResume` answers true only when the log holds exactly as many
events at or before the checkpoint's position as the checkpoint state was folded from, and
the event it was taken at is still there. One rule, two callers: a list for the harness and
three bounded SQL queries for the cold start.

**Why.** A checkpoint is the fold of a prefix of the total order, so resuming is correct
only while the log still begins with that exact prefix. The obvious check compares the ends:
the newest event sorts at or after the position and the position is in the log. That passes
on a merged log that inserted three events before the checkpoint, and `replayFrom` then
drops all three as already folded in when they never were. Nothing looks wrong afterwards.
The numbers are just smaller, forever. The count catches it because a prefix that grew by
three no longer matches what the checkpoint was folded from.

**Considered and rejected: relying on the merge path to clear checkpoints**, which it does,
and which was left in place. The point of stating the rule as a count is that forgetting
costs a slow cold start rather than a wrong one.

**Revisit if** the count query ever becomes expensive. It is one aggregate today and the
alternative is a stored event count, which is state outside the log.

### A parallel numeric clause is a lead rendering two or more numbers

**Decided.** The 7.4b cap counts a lead as parallel when it renders two or more numbers, not
when it contains one. Implemented in `domain/report/ReportComposer.kt`.

**Why.** `CLARITY_LOGIC_ENGINE.md` 7.5 and `CORPUS_2_REPORT.md` 7.4b forbid three parallel
numeric clauses in a row and neither says what one is. The obvious reading is any lead
carrying a number, and it is wrong in a way that would have been hard to find afterwards:
nearly every observation in this corpus carries one number, so capping runs of them at two
would silently drop the third and fourth observation of almost every report, and the reports
would get quietly shorter with nothing failing. The shape the corpus actually writes is one
number set against another inside one sentence, "You added 9 things and finished 6", and
three of those in a row is the three part list at the only scale a composer can see it.

**Considered and rejected: re-realizing the third at a different length**, which is the other
resolution 7.5 offers and which needs the bench. The composer holds finished sentences, so it
drops.

**Revisit if** phase 9 grows the corpus enough that reports start reading as a run of single
number sentences. The cap is then the wrong instrument and the register balance is the right
one.

### Observations are grouped by section, and the band rule is re-applied as a preference

**Decided.** The composer arranges the observations into 11.1's section order and, inside a
section, prefers the highest ranked line whose length band is not the one just used. Where
every remaining line in a section shares the band, the highest ranked one is taken anyway.
Implemented in `domain/report/ReportComposer.kt`.

**Why.** Rank order is what the engine returns and it is one plausible reading of the design.
It has a defect that only shows on the page: three observations can arrive as general, focus,
general, and the screen then draws `Your week, honestly` twice with `Focus` between them. A
repeated sidehead reads as a bug, and the sideheads are the only structure a page of prose
has. Grouping can then put two leads of one band together, so the rule is applied again over
the order the page is actually read in. It is a preference rather than a veto because
`MASTER_BUILD_PROMPT.md` 11.4 forbids padding a section to reach a minimum, and dropping a
true observation to improve the cadence is the same trade in the other direction. Rhythm is
worth a line, not a paragraph.

**Considered and rejected: dropping the observation whose band collides**, which is the
literal reading of `CLARITY_LOGIC_ENGINE.md` 7.5 and which trades something true for
something pleasant.

**Revisit if** phase 9 grows the benches and the collisions do not fall. The corpus is the
instrument that fixes this, and the measured number is one of the six phase 5 gates phase 9
is judged against.

### The regenerate wait is a shimmer, and 12.3's word for it is not the one that wins

**Decided.** A placeholder shimmer in the shape of the headline, on the headline block only,
with the percentage inverted for a dark ground. Nothing else on the page moves. Implemented
in `ui/report/ReportBlocks.kt`.

**Why.** 12.3 calls it a spinner. `design-v3.md` 8.2 item 22 is one sentence long and says
placeholder shimmer, 4 percent ink moving slowly, never a spinner. `CLAUDE.md`'s authority
order gives anything visual to `design-v3.md`, so the two documents settle it between
themselves and this needed no owner. The percentage is the one number chosen here: 4 percent
ink is a pale grey on the Daylight canvas and is invisible on `deepBlack`, so the same
relationship is inverted into a low band with a brighter one traveling through it.

**Considered and rejected: nothing at all**, since 12.3 also says regenerating is near
instant. A control that produces no acknowledgment reads as a control that did not work.

**Revisit if** the regeneration turns out to be fast enough on the device that the shimmer
never completes one pass. A wait indicator that flashes is worse than none.

### The report scope veto is its own state, and it is never shown as the empty state

**Decided.** Four states: a composed report, the styled empty state from `CORPUS_2_REPORT.md`
6.1, a withheld state when the integrity layer refused, and an unavailable state when the
corpus could not be read. The last two are fixed `strings.xml` copy about the app, carry no
number, and say nothing about the person. Implemented in `ui/report/ReportViewModel.kt` and
`ui/report/ReportScreen.kt`.

**Why.** `Nothing to report yet` is a corpus line and a true sentence about a week in which
nothing happened. It is a false sentence about a week the app could not prove its arithmetic
for, and showing it there would be the app telling somebody their week was empty because the
app had a fault. 12.3's prime directive is the reason the veto exists and it is not served by
lying about why it fired.

**Considered and rejected: showing the last good report instead**, which puts a stale week
under this week's eyebrow, and is the same failure with a date on it.

**Revisit if** the withheld state ever fires in ordinary use. It should not: the composer
applies every rule while it assembles and the property test asserts that no report it builds
is refused by its own integrity layer.

### Open, and not a builder's to settle: three Addendum 01 items this phase was given

**The recommendation, stated and not taken.** Build order 19 assigns phase 8 the week long
Report suppression after a return (14b.4), capacity aware decline detection and its cyclical
persona test (14b.9), and the estimate calibration facts, their floor and the delta veto
(14b.8). **None of the three landed.** All three are fact extraction and rule criteria in
`domain.engine`, none of them is a screen change, and none has a fact behind it in
`domain/engine/facts/` today.

They are recorded here rather than moved, because two of the three are refusals rather than
additions: a person returning after a fortnight can be told about the gap by the first report
they open, and a person whose activity is cyclical can be told they are declining when they
are not. **A refusal that nobody is holding ships as its opposite**, and neither has a
failing test today because there is nothing yet to fail.

The recommendation is that they go to phase 9 rather than to a phase of their own, because
14b.10's tone pass is already there and all four are the same audience protection, and
because 14b.8's delta veto and the corpus lines it governs cannot sensibly be built a phase
apart. **The owner's call.**

### Open, and not a builder's to settle: what a past report's headline is

**The recommendation, stated and not taken.** `design-v3.md` section 5 gives past report
headlines the display role. `ReportGenerated` carries `headlineKey` and `headlineVariantKey`
and does not carry the headline's rendered text, while `renderedSections` carries the text of
every observation. So the History page has a treatment for a string the payload cannot
supply, and a row leads with its week and its ribbon instead.

Re-realizing the variant from the corpus would be a second path to a sentence, which
`CLAUDE.md` rule 8 closes, and the facts of that week are gone in any case. The
recommendation is one more string on the payload, beside the sections that already carry
their rendered text, added by the same phase that adds the write. **It is a schema change
to a committed event format, `docs/EVENT_FORMAT.md`, which is why it is the owner's call
rather than a builder's.**

---

## August 27, 2026: the open choices in Momentum and the Areas banner

Phase 7, issue #5. `MASTER_BUILD_PROMPT.md` 11.2, 11.3 and 12.2, `design-v3.md` 3.4, 8.2,
8.3, 8.4, 10.2, 11, 13 and 16.2, `CLARITY_LOGIC_ENGINE.md` 6.5, and `CORPUS_3_MOMENTUM.md`.
Nine things those documents leave to the builder, settled here under `design-v3.md` 15.

**This is the calmest screen in the Daylight world and it is the one where rule 8 is
easiest to cross.** One sentence on it comes from the engine and everything under that is a
number the log was asked for, drawn as a dot, a tile, a figure or a mark, with its label out
of `strings.xml`. A caption reading "a strong week" under a figure would be an observation
written in a composable, and there is not one anywhere on the screen. Several entries below
are about keeping a number a number.

### The tiles are a three column grid

**Decided.** Three columns at the 20dp screen padding, wrapping. Implemented in
`ui/momentum/MomentumScreen.kt`.

**Why.** `design-v3.md` 11 asks for area tiles and gives them a corner radius and nothing
else. The two obvious answers both cost something specific. A horizontally scrolling row
hides areas off the edge of the one screen whose job is to show all of them at once. A four
column grid makes the tile small enough that 3.4's "one place where an area color gets real
presence" stops being true. Three columns give a tile wide enough to read as a block of the
person's own color, and five areas, which section 11 calls a comfortable screenful, fill two
rows with the second one short, which is what a mosaic looks like rather than what a table
looks like.

**Considered and rejected: sorting the tiles by how busy each area was**, which would turn
an identity mosaic into a leaderboard. They follow the order the person arranged, which is
the order the Areas screen uses, because two screens showing the same things in two orders
is two apps.

**Revisit if** somebody with twelve areas finds the block dominating the screen. The answer
then is the tile's height, not the column count.

### The three This Week figures are completions, minutes focused and items added

**Decided.** Those three, in that order. Implemented in `domain/momentum/MomentumComposer.kt`
and `domain/momentum/MomentumView.kt`.

**Why.** 12.2 asks for three typographic stats and does not name them. The obvious three are
completed, added and areas active, and two of those lose. The tiles directly above already
say which areas moved, so a figure repeating it is the same fact twice on one screenful. And
the lifecycle order, added then completed, opens the screen on a number that goes up every
time somebody has an idea, which is the wrong first thing for this audience to read about
their week. What is left is output, attention and intake, which are the three axes the
engine's own families read and none of which is drawn anywhere else on the page.

**Considered and rejected: a fourth figure**, which 12.2 rules out by saying three, and
which would have been areas active in every proposal.

**Revisit if** focus sessions turn out to be rare enough in real use that the middle figure
is usually zero. It carries a discovery line while the feature has never been used, which
is a different case from a quiet week.

### An unused feature is a lifetime question, never a weekly one

**Decided.** A figure renders dimmed with a discovery line only when the feature behind it
has never been used in the whole log. A zero this week is a zero. Implemented in
`domain/momentum/MomentumComposer.kt`.

**Why.** 12.2 asks for an unused feature to render dimmed with a soft discovery line rather
than be hidden. The obvious implementation asks the week, and it is worse than saying
nothing: telling somebody who finished nine things last week what completing is reads as the
app not knowing them. Intake carries no flag at all, because capture is the first thing
anybody does and there is no feature behind it to discover. The discovery lines live in
`strings.xml` because every one of them describes how the app works and none says anything
about the person, which is the line `CLAUDE.md` rule 8 actually draws.

**Considered and rejected: hiding the figure**, which 12.2 forbids by name, and which would
make the block two figures wide on some weeks and three on others.

**Revisit if** a discovery line ever needs to say something about the person's own data. At
that moment it stops being a `strings.xml` string and becomes a corpus line.

### The activity readout sits under the dots

**Decided.** The fourteen dots first, then `Active X of last 14 days` beneath them.
Implemented in `ui/momentum/MomentumScreen.kt`.

**Why.** `design-v3.md` 15, and this is the deliberate answer rather than the obvious one. A
label above a graphic makes the graphic an illustration of the label. Under it, the row reads
first as a texture and the sentence confirms what the eye already got, which is exactly the
arrangement 11.1 gives the Report's week ribbon: marks, then one caption line stating the
numbers. It is also what section 13 asks for, since the caption rather than the row is what
carries the claim aloud, and the row is one node to a screen reader that names itself and
tallies nothing.

**Considered and rejected: a spoken count of the gaps** in the row's content description.
Section 14 says a gap is rendered as a lighter dot with nothing said about it anywhere, and
a screen reader is somewhere.

**Revisit if** the readout gets lost under the row at a large font scale. The gap is the
number to move.

### The figures are Newsreader, the labels are the sans, and the columns are left aligned

**Decided.** Three figures in the serif over sans labels, on a common left edge, no card,
no rule, no container. Implemented in `ui/momentum/MomentumScreen.kt`.

**Why.** The obvious treatment for three numbers side by side is a heavy sans figure over a
small caps label, centered, which is a dashboard, and 15.1 lists stat banners as a tell.
Newsreader ties the three figures to the headline above them and makes the block read as
part of a page rather than as a widget dropped onto one. Centered columns are what a metric
row looks like; a common left edge is what a page looks like. It also happens to be the only
correct answer to a mechanical problem: 5.2 records that Hanken Grotesk ships no `tnum` and
Newsreader does, and these three figures are the one place in the app where a number counts
up through every value between zero and itself.

**Considered and rejected: `inkTertiary` for the dimmed state**, which 10.3 already had this
argument about on the area card's idle title. It measures 2.40:1 in light and section 13's
floor is 4.5. The dim is bought by stepping down to `inkSecondary` and by the discovery
line, which is the second signal 13 requires whenever a state is carried by color.

**Revisit if** the serif figures turn out to be hard to read at a glance on the device. That
is the one thing a screenshot cannot settle.

### The area name sits under the tile, and the idle outline is the hairline token

**Decided.** The name is caption ink on the canvas beneath its tile. The active tile is the
area color at 60 percent with no border; the idle tile is not colored at all and carries a
`hairline` edge. Implemented in `ui/momentum/MomentumScreen.kt`.

**Why.** Two rules in 3.4 decide both halves. It requires an area label to be verified at
4.5:1 against the surface it is drawn on and names the two surfaces that were measured, both
of them the card carrying that area's wash; a label on a 60 percent tile is a third surface,
one per area color per theme, and none of them has been measured. And it permits the accent
in four forms and ends "never as a stripe, bar, edge, border or filled block", so the faint
outline section 11 asks for on an idle tile cannot be drawn in the area's own color. The two
states then differ in fill, in edge, and in nothing else, which is one separation device each
per 6.1.

**Considered and rejected: measuring every area color against a 60 percent tile** and putting
the name inside it. That is 48 colors times two themes of contrast work to win one label
position, and it would have to be redone the day the palette changes.

**Revisit if** the name and the tile stop reading as one object at a large font scale.

### The idle module's sidehead reads `Quiet areas`

**Decided.** The sidehead is `Quiet areas`. The module is otherwise exactly what 12.2
specifies: seven or more days inactive, gentle, no red. Implemented in
`ui/momentum/MomentumInsights.kt`.

**Why.** 12.2 calls the module Idle Areas and asks for it to be gentle, and that word is the
argument. `Idle` is what the state is called in the code and on the area card, where it
describes one card; set as a heading over a person's own list it reads as a verdict on them
rather than a description of a fortnight. `Quiet` is the word the corpus itself already uses
for the same shape, in `mo.quiet` and `bn.quiet`, so the screen and the sentences above it
name it the same way. The line beside each name is the same `Last active N days ago` the area
card carries in 10.3, out of the same plurals resource, so one fact is worded one way
wherever it appears.

**Considered and rejected: keeping `Idle areas`**, which is the specification's own word and
is what a session with no context would write. It is a label change and not a behavior change,
which is why it is recorded here rather than argued as a conflict.

**Revisit if** the owner prefers the specification's word. Nothing else moves if it changes.

### The share rows are typography, with no bar behind the numbers

**Decided.** Area balance is a dot, a name and a percentage per row. The pace sparkline is a
line with one mark on the newest week. The focus strip is seven shaded cells with a floor
under an empty day. No axes, no gridlines, no fills, no bars, and nothing drawn in an area
color except the 7dp identity dot. Implemented in `ui/momentum/MomentumInsights.kt`.

**Why.** The obvious rendering of a share is a horizontal bar per row, and it is forbidden
three times over: 3.4 ends "never as a stripe, bar, edge, border or filled block", section 14
repeats it as the rule about colored bars, and 15.3 names the same fix as a refusal. The
percentage is the whole of the information and a row of numbers is legible without a chart
behind it. The empty focus cell keeps a floor for the reason section 11 gives the Pulse's
silent mark: below a floor a mark stops being quiet and starts being absent, and a week of
absent cells reads as a broken strip rather than a calm one.

**Considered and rejected: red anywhere on the idle module**, which 12.2 forbids and which
there is nothing to reach for anyway: the Daylight palette in 3.1 has no red token and
`deleteMuted` is scoped to one job.

**Revisit if** the percentages alone stop conveying the balance. The answer is the ordering,
which is busiest first, not a bar.

### The banner gets a ViewModel of its own

**Decided.** `AreasBannerViewModel`, resolved against the Activity's store, holding the once
per hour throttle as a value type with no Android in it. It does not recompute on the
projection. Implemented in `ui/momentum/MomentumViewModel.kt` and
`ui/momentum/BannerThrottle.kt`.

**Why.** 11.2 and `CLARITY_LOGIC_ENGINE.md` 6.5 both say the throttle goes in the ViewModel
and not in the engine, and say nothing about which ViewModel. The obvious answer is a field
on `AreasViewModel`, and it loses on two counts. That class is the queue: the areas, the
swipes, the promotions and the running session, none of which has anything to do with a
sentence from the engine. And the banner must not recompute on the projection, because a week
does not change shape when one item is completed, and redrawing it on every write is the
recomputation per frame failure issue #5 names as this phase's second risk, dressed as
correctness. The Activity's store is what makes one instance serve every visit to the tab, so
the hour is measured in app use rather than reset by a tab switch.

**Considered and rejected: a second throttle inside `MomentumComposer`**, which would make
the rate the product of two numbers nobody stated. The composer is documented as not
throttled and as never becoming throttled.

**Revisit if** the banner ever needs to react to a write, which would mean it had stopped
being a sentence about a week.

### The two entrances are written out rather than routed through the shared modifier

**Decided.** The dot cascade and the number roll are their own functions, sharing only the
decision of whether an entrance plays at all. The number roll has no reduced form and renders
its value. Implemented in `ui/momentum/MomentumMotion.kt`.

**Why.** `Modifier.clarityEntrance` is 8.2 item 4: a 350ms fade with a 16dp rise at a 50ms
stagger. Item 13 is a 35ms stagger with no rise and item 14 is not a fade at all, so sharing
the modifier means widening it with two parameters that exactly one screen ever passes. What
is shared is the thing that matters, which is one reading of the session entrance flag and one
of calm mode, so 8.4's once per session rule and 16.2's removal of entrances are answered in
one place for both. The number roll has no reduced form because a count up is not a fade, and
150ms of counting would be a flicker of wrong numbers rather than a gentler animation, which
is the same reading 8.3 takes of the focus ring.

**Considered and rejected: re-counting when a figure changes while the screen is open**,
which would be an entrance firing on a data change. 8.4 allows that for exactly one animation
in this app and it is the Report reveal.

**Revisit if** a third screen needs a stagger that is not 50ms. Two call sites is a
coincidence and three is a parameter.

---

## August 27, 2026: the twelve open choices in the Pulse

Phase 6, issue #4. `MASTER_BUILD_PROMPT.md` 11.3, 11.6, 12.1, 13.4 and 14b.4,
`design-v3.md` 3.3, 8.2, 8.4, 10.1, 11 and 13, `CLARITY_LOGIC_ENGINE.md` 6.1 and 6.2,
and `CORPUS_1_PULSE.md`. Twelve things those documents leave to the builder, settled
here under `design-v3.md` 15, and one that is not a builder's to settle and is recorded
as open.

**This is the first surface in the app that shows a sentence the engine wrote about a
person's own life.** Everything before it was a fixed label or a readout of a number the
app had just counted. That changes what a wrong decision costs: a screen that assembles
one string for one edge case is not a visible defect, it is a false claim about
somebody's week with nothing on the screen pointing back at the cause. Several entries
below are therefore about a shape that makes the wrong thing unreachable rather than
about a check somebody has to remember to write.

### The response pills are stacked, and never set side by side

**Decided.** The two options, or three for `quietDay`, sit in a vertical stack, identical
in width and in treatment, with one composable called once per option and no parameter
that could make one of them louder. Implemented in `ui/pulse/PulseSurface.kt` and
`ui/pulse/PulseResponsePill.kt`, and stated in `design-v3.md` 11.

**Why.** `design-v3.md` 11 says "response pills" and gives no arrangement, so section 15
applies. Side by side is the statistically common answer and it costs two things.
It puts one option on the left, and in a left to right reading order the left position
reads as the recommendation, which `CLARITY_LOGIC_ENGINE.md` 6.1 forbids the interface
from making: both responses must feel equally valid read out of context. And it does not
survive `quietDay`, the one family with three options, so the surface would rearrange
itself between families for a reason the person cannot see. One stacked layout answers
both cases in one shape.

**Considered and rejected: side by side for two and stacked for three**, which is the
arrangement that looks best in each case taken alone and is the worst of the three,
because it makes the layout a signal about which family fired. **Also rejected: a primary
and a secondary treatment**, which is the same prohibition stated in color instead of in
position.

**Revisit if** a three option stack turns out to push the observation off the top of the
room at a large font scale. The first move then is the room, not the arrangement.

### The room is a fixed band rather than a sheet that wraps its content

**Decided.** The Pulse opens at 520dp, collapsing to no less than 320dp, and both phases
scroll inside whatever height they are given. Implemented in `ui/pulse/PulseRoute.kt`.

**Why.** `design-v3.md` gives the Pulse no height. A sheet that wraps its content is the
obvious answer and would be about 300dp for the question and about 250dp for ambient
mode, which makes the amber night a panel rather than a room, and makes the surface
resize under the settle in 8.2 item 11. A little over half a phone is enough for a serif
observation to sit in space with its question beneath it, and short of the full screen
that would make a drag down feel like leaving the app rather than closing a sheet. The
band rather than a single value is what carries landscape, where a fixed 520dp is taller
than the device, and a 200 percent font scale, where the content scrolls inside the room
instead of growing it.

**Considered and rejected: full screen**, which is what the Focus surface is and is right
there, because Focus is a place a person stays for twenty five minutes and this is a
question that takes fifteen seconds.

**Revisit if** the device check shows the ambient phase looking empty in the room at
520dp. The answer then is what ambient mode holds, not the height.

### The amber tint reaches under half the height and stops

**Decided.** The dawn and evening tints are a vertical gradient at 55 percent from one
edge, reaching 45 percent of the height and going to nothing. In calm mode the tint is
not transformed, it is not drawn at all. Implemented in `ui/pulse/PulseBackdrop.kt` and
consistent with `design-v3.md` 3.3 and 16.7.

**Why.** 3.3 says the shift "must be felt rather than noticed", which is the whole
specification of its strength. The obvious reading of blending a whisper into one edge is
a gradient across the whole surface, which is what a hero background looks like in 2026
and which would make the time of day the loudest thing on a screen whose entire content
is one sentence. A tint that stops before the midpoint is a room lit from one side rather
than a colored screen.

**Considered and rejected: a tint that animates as the hour moves.** Nothing here counts
down to the next boundary. A background that redrew itself while somebody was reading is
the exact opposite of felt rather than noticed.

**Revisit if** the two tints are indistinguishable from midday on the device at low
brightness, which is the failure mode of choosing quiet.

### The chip's second signal is the destination's name, not a status word

**Decided.** The Pulse chip reads `Pulse` at rest and `Today's Pulse` when a Pulse is
ready and unanswered, alongside the 6dp `warnAmber` dot that `design-v3.md` 10.1
specifies. Implemented in `ui/areas/PulseChip.kt`, with both strings in `strings.xml`.

**Why.** `design-v3.md` 13 requires a second signal because color is never the only one,
and leaves the wording open. The obvious answers are a status word appended to the label,
`Pulse ready`, or a count, `Pulse 1`, which is the shape the inbox chip already uses.
Both report on the person rather than name the destination, and a count of one is a
number nobody needs. `MASTER_BUILD_PROMPT.md` 13.5 already calls this surface
`Today's Pulse` for the app shortcut that opens it, so the two entry points into one
surface say the same words, and the label reads as an invitation rather than as a notice
that something is outstanding.

**Considered and rejected: a content description on the dot.** Any wording for it would
be a sentence about an unanswered question, and 11.6 says not answering is never chased,
never counted and never mentioned. The changed label is the whole of what is said.

**Revisit if** user testing shows people do not read the chip label at all, in which case
the second signal has to be a shape rather than a word.

### The history list carries absolute dates, including today's

**Decided.** Every row in the Pulse history is a date. There is no `Today` and no
`Yesterday`. Implemented in `ui/pulse/PulseHistoryPage.kt`.

**Why.** The Trail does relative labels for the two newest days, and inheriting that here
was the default. The two lists are read for different things: the Trail is read for
recency, and this is read for pattern, down one column. A list whose first two rows are
labeled differently from the rest breaks the column a person is scanning.

**Considered and rejected: relative labels everywhere**, which would have been consistent
with the Trail and would have made the page a list of phrases instead of a list of dates.

**Revisit if** the Trail and the Pulse history ever appear on one screen, where two date
conventions side by side would be worse than either.

### The acknowledgment is held for 1,100ms, and the hold does not shorten under reduce motion

**Decided.** After an answer the pill fills over 220ms, the acknowledgment fades in over
400ms after a 250ms hold per `design-v3.md` 8.2 item 10, it is held for 1,100ms, and the
room then settles into ambient mode. Under reduce motion the two fades shorten to 150ms
and the 1,100ms hold is unchanged. Implemented in `ui/pulse/PulseSurface.kt`.

**Why.** 8.2 gives every number in that sequence except how long the acknowledgment
stands. The corpus calls these lines "shown briefly" and they are short: a second and a
bit is one unhurried reading of a five word sentence and is short of the point where a
person starts wondering whether the screen has stuck. The hold survives reduce motion for
the reason `design-v3.md` 8.4 keeps the empty state's delay: **a hold is not motion**, and
shortening it would remove reading time from the person most likely to need it rather
than removing an animation.

**Considered and rejected: dismissing on the next tap instead of on a timer**, which
would hand the person a control whose only function is to skip a sentence the app just
wrote for them.

**Revisit if** the corpus grows acknowledgment lines materially longer than the ones
authored today, in which case the hold has to be a function of length rather than a
constant.

### The reminder is a chain of one hop at a time, not a periodic request

**Decided.** The daily reminder is a `OneTimeWorkRequest` under a unique name, and each
run arms the next one by recomputing the target hour against the clock and its zone.
Implemented in `work/PulseReminderScheduler.kt` and `work/PulseReminderSchedule.kt`.

**Why.** A `PeriodicWorkRequest` with a twenty four hour period is the obvious
implementation and it loses on the one thing this feature is, which is a time. A period
is a fixed duration measured from the previous run and not a wall clock hour: the morning
the clocks change it fires an hour early or an hour late and stays there until something
reschedules it, and WorkManager's batching accumulates over months. Recomputing on every
hop makes daylight saving a calendar question the scheduling code never sees, and absorbs
a timezone change on the next hop. For the audience `MASTER_BUILD_PROMPT.md` 14b
describes, a reminder that arrives at the wrong hour teaches them the app's timing cannot
be trusted, and they switch it off.

**Considered and rejected: an exact alarm**, which needs `SCHEDULE_EXACT_ALARM` and is
out of scope under section 18. WorkManager may run this late and Doze can hold it on a
sleeping phone. A reminder that arrives a little late is the honest version of what this
app can promise.

**Revisit if** the device check shows the reminder arriving materially late in ordinary
use, which is the one thing the unit tests cannot see.

### The permission prompt fires on a transition, and never on the first composition

**Decided.** `NotificationPermissionOnReminderEnabled` remembers the value the switch
first showed and asks for `POST_NOTIFICATIONS` only when that value goes from false to
true. Implemented in `notifications/NotificationPermission.kt`.

**Why.** `pulseRemindersEnabled` defaults to true, per `MASTER_BUILD_PROMPT.md` 14b.12,
so the obvious `LaunchedEffect` keyed on the value would prompt every person who has
never touched the switch, the moment they first opened Settings. That is not quite launch
and it is close enough to be the same defect: a permission prompt for something they did
not just do. It is also derived from the setting changing rather than announced by
whoever changed it, so there is no `onCheckedChange` anybody has to remember to wire, and
the moment cannot drift into the wrong place in a later phase.

**Considered and rejected: prompting the first time the reminder would actually post**,
which happens with the app closed and cannot show a dialog at all.

**Revisit if** the default for `pulseRemindersEnabled` ever moves to false, at which
point the first composition and the transition are the same event and this can be
simplified.

### The reminder cannot be posted without a token read from the day's entry

**Decided.** `PulseReminderPoster.post` takes a `PulseReminderDue`, whose constructor is
private and whose only factory returns null unless the day's entry exists and is
unanswered. Implemented in `notifications/PulseReminderPoster.kt`.

**Why.** `MASTER_BUILD_PROMPT.md` 12.1 and issue #4 both say the reminder posts only if
that day's entry exists and is unanswered, never when IDLE. Written as an `if` at the
call site that is one line, it looks right without it, and it is exactly the kind of
check a later refactor drops. A notification on a day the engine chose to stay silent
turns designed silence into a broken promise, which is the most expensive defect this
phase could ship. Made a type, the rule is not something anybody can forget: there is no
way to reach the poster without having read the entry out of the log. It is the same
shape `NotificationMoment` uses to make "never ask at launch" structural.

**Considered and rejected: generating a Pulse inside the worker** so that there would
always be something to post, which would be the app speaking to somebody who did not open
it, forbidden by name in 13.4.

**Revisit if** a second thing ever needs to post on this channel, which is the point at
which the token stops being free.

### The rhythm row has three marks and today carries no ring

**Decided.** Filled amber for answered, a hollow ring for generated but unanswered, and a
smaller half strength mark for a silent day. No fourth state, no ring on today, no
caption, and a spoken description that names the element rather than tallying it.
Implemented in `ui/pulse/PulseRhythmRow.kt`.

**Why.** `MASTER_BUILD_PROMPT.md` 12.1 names the three and stops. Today's ring is
Momentum's treatment, 12.2, and importing it here would add a fourth mark to a row whose
whole safety property is that it has three: the mark takes a `PulseDayState`, so a fourth
kind of mark would need a fourth day state and the log cannot produce one. The row is
also the single most likely place in the app to reintroduce a streak by accident, so each
mark is drawn from its own day and knows nothing about the day beside it, and a gap is a
fainter mark and nothing else. The silent mark is drawn smaller as well as fainter, so
the three differ in form and not only in opacity, and it is held at 3.0 to one against
`deepBlack` per `design-v3.md` 16.7, because a mark below that stops being quiet and
starts being absent, and a fortnight of silence would then look like a broken row rather
than a calm one.

**Considered and rejected: a count beneath the row.** `design-v3.md` 14 forbids a
consecutive count and section 11 gives the row no caption, so a spoken figure would hand
a screen reader user a claim the sighted reader cannot see.

**Revisit if** the three marks turn out to be hard to tell apart on the device at arm's
length, in which case the silent mark's size moves before its opacity does.

### The corpus ships as an asset rather than as a Kotlin constant

**Decided.** The three committed corpus files are read at runtime through a `CorpusSource`
seam, which on the phone reads `assets/corpus/` and in a test reads the committed file off
disk. Implemented in `domain/corpus/CorpusSource.kt`, with the platform half private to
`di/ClarityGraph.kt`. It was in `domain/pulse/PulseCoordinator.kt` and `ClarityApp.kt` until
issue #55.

**Why.** CLAUDE.md's authority order gives the corpora the last word on the wording of
every sentence. A copy of a corpus embedded in Kotlin is a second corpus, and a second
corpus drifts silently: the file an author edits stops being the file the app reads, and
nothing fails when they disagree. The seam rather than a direct call is what keeps
`ClarityCatalog.build` taking text, which is what lets phase 9 judge a corpus edit against
the real rules from a string.

**Considered and rejected: parsing at build time into a generated Kotlin source**, which
is faster on the phone and puts a generated artifact between the author and the app,
which is the drift this decision exists to prevent.

**Revisit if** catalog build time is measurable on the first foreground of the day on the
device. The answer then is when it is built, not where it is read from.

### The Pulse generates after the presence marker, not before it

**Decided.** On the first foreground of a process, `ClarityApp` writes `APP_OPENED` and
then runs the generation sequence, in that order, in one call. Implemented in
`ClarityApp.kt`.

**Why.** 14b.4 suppresses the Pulse for the first two days after a return, and a return
is detected by comparing today's `APP_OPENED` against the newest one before it. Generating
first would find no return on the one day it matters, and a person coming back after a
fortnight would be met by exactly the observation about their absence that the whole of
14b.4 exists to prevent. The ordering is not a preference, it is the difference between
the rule holding and the rule reading correctly while doing nothing.

**Considered and rejected: running the two in parallel**, which is faster by a few
milliseconds on a path nobody is waiting on and reintroduces the race the ordering
removes.

**Revisit if** the presence marker ever moves out of the foreground callback, at which
point this ordering has to move with it.

### Open, and the owner's rather than a builder's: what WorkManager adds to the merged manifest

**Not decided.** This phase is the first thing in the app that really uses WorkManager,
and the question has been open since phase 2. It is recorded here rather than settled.

**What is actually true today.** The merged manifest, debug and release, declares six
permissions: `POST_NOTIFICATIONS`, `POST_PROMOTED_NOTIFICATIONS`, `VIBRATE`,
`FOREGROUND_SERVICE`, `WAKE_LOCK` and `RECEIVE_BOOT_COMPLETED`. The last three arrive from
work-runtime and none of them is a network permission, so `verifyNoInternetPermission`
passes on both variants and the privacy claim a person can check in Android settings is
intact. `ACCESS_NETWORK_STATE`, which work-runtime also declares, is removed with
`tools:node="remove"` and does not appear in either merged manifest.

**Why it is still open.** `MASTER_BUILD_PROMPT.md` 18 puts every permission beyond
notifications out of scope for v1, and three of the six are beyond notifications. Section
3 and 12.1 both require WorkManager for this reminder, and 12 requires it again for the
widget refresh, so the only ways to close the gap are to accept the three permissions in
writing or to build the reminder on something else. This phase uses WorkManager because
the specification tells it to, and reports what the merged manifest gains.

**The removal was checked against the library rather than assumed.** In work-runtime
2.11.2, `WorkConstraintsTracker.track` filters its controllers by
`ConstraintController.hasConstraint` for the spec it is given, so a request with no
constraints never reaches the network tracker and never starts it, and everything that
does touch `ConnectivityManager` already catches `SecurityException`. A constraint free
daily reminder is therefore safe without the permission. **Adding any constraint to any
work request in this app means putting `ACCESS_NETWORK_STATE` back**, which changes what
the privacy policy invites people to verify, so it is the owner's call and not a build
fix. The manifest says so at the point of the removal.

**The recommendation, stated and not taken.** Amend section 18 to name the three
WorkManager permissions explicitly rather than leave a document saying no permission
beyond notifications while the app ships three, since a specification that is quietly
untrue about permissions is worse than one that says which three and why.

**Revisit at** phase 11, where the privacy sheet and the permission card are built and
this list has to be shown to a person, and again at phase 12, where the widget refresh
makes WorkManager unavoidable a second time.

---

## August 27, 2026: the fourteen open choices in the engine skeleton and the simulator

Phase 5, issue #3. `CLARITY_LOGIC_ENGINE.md` 2 to 8, 11.1, 12 and 14,
`MASTER_BUILD_PROMPT.md` 9 and 11, and CLAUDE.md's authority order. Thirteen things those
documents leave to the builder, settled here under `design-v3.md` 15, and one reading that
is a finding rather than a choice and is recorded as open.

**Every one of these is invisible.** Nothing in this entry changes a pixel. A wrong
decision here becomes a false sentence about somebody's life some months later, with
nothing on the screen pointing back at the cause, which is why the entry is long and why
the losing option is named in every case.

### A family with a missing fact gets no rule at all

**Decided.** Nine families and three single stages have authored corpus language and no
rule, because the fact their trigger names is not declared in `CLARITY_LOGIC_ENGINE.md`
3.1. Each is recorded in code as a gap carrying the missing fact and the corpus line that
needs it, and a catalog test asserts that every family in the corpus either has a rule or
is on that list. Stated at the point of ambiguity in 6.1 and in `MASTER_BUILD_PROMPT.md`
11.7.

**Why.** Every one of them had an approximation available that is nearly the right shape.
Window active days in place of a run of consecutive quiet days. The window's swap count in
place of an area's. Days since an area's last event in place of the dormancy it returned
from. A per week area count that nothing keeps. Each would fire the family on a shape it
does not describe, and the sentence that came out would be arithmetic nobody could fault
and a claim about a person's week that was not true. That is the prime directive in
section 1, and this is the phase where obeying it costs something, which is the only kind
of obedience that counts.

**Considered and rejected: the near enough criterion**, which is what a builder under
schedule pressure writes and what nobody would ever catch, because the output is fluent
and the number is real. **Also rejected: deleting the corpus language**, which throws away
authored, reviewed lines to make a test pass and loses the record of what the family was
for. **Also rejected: a comment.** A comment is not read by a test, and the failure it
guards against is a later session wiring the family up with the approximation.

**Revisit if** phase 9 or a later phase adds the fact. The fix is a fact in 3.1 and a
query behind it, never a criterion built out of the facts that are already there.

### The escalation ladder drops the pair rather than raising the stage

**Decided.** When `FiringHistory` says a `(family, subject)` pair last spoke at a higher
stage than the one the magnitude now supports, the pair is dropped and the family says
nothing until the magnitude catches up. It is never raised to the stage last shown.
Recorded in 7.3.

**Why.** 7.3 requires that a pair never show a lower stage while the condition stayed
continuously true, and the obvious implementation of that is to render the stage last
shown. It is wrong because a stage is authored around a magnitude: stage 2 of
`persistence` is written for six to thirteen days, so showing it for an item whose age
just reset to three says `going into its second week` about a three day old item. The rule
exists to prevent whiplash, and the obvious implementation produces a false sentence
instead. **A missing sentence costs a day of silence. A false one costs the credibility of
every sentence after it.**

**Considered and rejected: rendering the higher stage anyway**, above. **Also rejected:
tracking the condition's continuity explicitly**, with an event or a field recording when
a ladder lapsed. That is engine state, it would have to live somewhere, and the only place
it could live and still merge is the log, which means a new event type carrying a fact
nothing else needs. The bound used instead is the family's own cooldown plus the window it
describes: inside that the family was either speaking or forbidden from speaking, and
beyond it the engine has no evidence and the ladder starts again.

**Revisit if** the simulator dump shows a family going quiet for long stretches after a
single high stage firing. The reading to look at is the family's own share of a persona's
year, not the count of dropped pairs.

### Editorial notability is specificity at three or more

**Decided.** The editorial register is offered only to a rule with three or more criteria,
and only for the Report, and only while the two lead budget in 7.4 is unspent.

**Why.** 7.4 caps the report at two editorial leads and `CORPUS_2_REPORT.md` says the
register is reserved for leads that have earned it with a genuinely notable fact. That is
a condition on the lead, and nothing in 3.1 or section 4 carries a notability flag, so
either something invents one or the realizer uses a measure the engine already computes.
Specificity is that measure: a rule requiring four things at once describes a narrower
situation than one requiring two, which is the entire mechanism of section 5 and the same
thing an editor means by a fact worth writing up. Three is the threshold because two is
the ordinary shape of a rule in this catalog, a condition and the floor that keeps it
honest, so three is where a rule stops describing a number and starts describing a
situation.

**Considered and rejected: an authored `notable` flag on the rule.** It is the obvious
answer, and it would be a second judgment about the same thing, drifting away from
specificity the first time somebody added a criterion. `CLARITY_LOGIC_ENGINE.md` says
specificity is never authored, for exactly that reason, and a notability flag is
specificity authored under another name. **Also rejected: letting the corpus tag decide.**
The register tag says which voice a line is in, not whether the fact behind it earned that
voice, and the fact is not knowable when the line is written.

**Revisit if** phase 9 finds editorial leads landing on facts that do not carry them. The
first move is the threshold, not a new flag.

### Two families 7.4 qualifies by a stage they do not have become two rules each

**Decided.** `persistentItem` and `switchingBehavior` are single stage families in
`CORPUS_2_REPORT.md`, and 7.4 marks them unflattering at stages the corpus does not have.
Each gets two rules pointing at the same stage, split at the magnitude the corpus already
states for the matching Pulse ladder: fourteen days for `persistentItem`, two swaps for
`switchingBehavior`. Only the higher rule is unflattering.

**Why.** 7.4 wrote the qualification deliberately, which rules out marking the whole
family; and three neutral agent lines are authored for the switching family, which rules
out marking none of it, because they would be unreachable. The qualification is really
about magnitude rather than about a stage number, and a rule is the object that carries a
magnitude in this design. The split points are not invented: both are already written down
in `CORPUS_1_PULSE.md` as the start of the corresponding Pulse stage.

**Considered and rejected: adding stages to the two corpus families** so the stage numbers
in 7.4 resolve. That is a corpus edit, phase 9 owns the corpus, and it would mean
authoring lines to satisfy a cross reference rather than because a person needed them.

**Revisit if** phase 9 gives either family real stages. The two rules then collapse back
into stage rules and this entry becomes history.

### A tie for the busiest day resolves to the earliest, and the family carries a floor

**Decided.** `WindowFacts.busiestDayKey` resolves a tie to the earliest day. Separately,
the family that names the day requires the day's count to be a real share of the window's
events. Recorded in 3.1.

**Why.** 3.1 makes `dominantAreaId` null on a tie and says nothing about this field, so the
tie had to go somewhere, and the earliest day is the day the peak was first reached. That
alone is not enough, and the second half is the part that matters: `Tuesday carried the
week` is false on a three way tie whichever day wins, so the honest guard is not the tie
break but the floor, which is the same guard every share based rule in the catalog
carries.

**Considered and rejected: null on a tie**, matching `dominantAreaId`. It reads as
consistent and it throws away a usable fact: a two way tie in a week of forty events is
still a real peak, and the floor is what makes it safe to name. **Also rejected: the
latest day**, which has no argument for it beyond recency.

**Revisit if** a corpus line ever wants to name the busiest day without a share claim
attached. It would need a different fact, not a different tie break.

### Response pairs live on the stage, and are not flattened onto the family

**Decided.** `PhrasingFamily` carries no flat response list. Each stage carries its
authored pairs, and the realizer chooses one pair and never splits it across stages.
Recorded in 7.1.

**Why.** 7.1 declares responses as a flat `List<ResponseOption>` on the family.
`CORPUS_1_PULSE.md` authors them as pairs, six or seven per stage, and the equal validity
test in 11.3 is a test on a pair: read both aloud with no context, and if one sounds like
the answer a good person gives, rewrite both. Flattening loses the pairing, which is the
only thing that makes that test mean anything, and a runtime that could combine the first
half of one pair with the second half of another would produce a question nobody reviewed.
CLAUDE.md's authority order gives the corpus the last word on the shape of a sentence, and
a response pair is a sentence shape.

**Considered and rejected: flattening and re-pairing by index**, which works until a stage
has an odd number of options and then silently pairs the wrong two.

**Revisit if** 7.1 is amended. This is a case where the corpus won over the engine
document, which is what the authority order says should happen, and the engine document
should probably be corrected rather than this reversed.

### The corpus violations that exist today are a recorded list, not a disabled check

**Decided.** 7.7 forbids a fragment appearing in two families and a rhetorical construction
appearing in more than two. The corpus breaks both, in six places and two shapes. The
checks are on, and the exact violations are enumerated in code with the keys and the
reason each is tolerated. A **new** violation fails the build.

**Why.** There were three options and only one leaves the build honest. Deleting the checks
loses them permanently, because nobody writes a check for a rule they have already decided
to break. Ignoring them until phase 9 means a check that is off, which is a check that
finds nothing on the day somebody adds a seventh violation. Writing down exactly what is
wrong keeps the check running, hands phase 9 a list rather than a rediscovery, and makes
every entry a debt with a name on it.

**Considered and rejected: fixing the corpus now.** Phase 9 owns the corpus, authors in
batches of forty with anchor lines, and presents them for approval. Editing six lines
outside that process is exactly the drift the process exists to prevent.

**Revisit if** the list stops shrinking. Each phase 9 batch that touches one of those
families should leave it shorter, and a list that is the same size at the end of phase 9
means nobody read it.

### Which families count as hot is measured, not authored

**Decided.** The simulator's stage coverage check calls a family hot when it fired forty or
more times across the run, which is 11.1's own definition of the tier, and reads its stages
from the rules that actually fired.

**Why.** 11.1 defines the tiers by expected firing frequency, so the simulator already
holds the only honest answer to which families are hot. A hand written list would be a
second place where a judgment about frequency lives, it would drift the first time a rule
changed, and it would let a family be called hot without ever having fired, which is the
one state the check exists to catch.

**Considered and rejected: the fifteen family list 11.1 estimates.** It is an estimate in a
document, and the simulator is the instrument that replaces estimates in this project. The
run produced twenty nine families at forty or more firings, which is not fifteen, and the
gap is itself a reading phase 9 should look at.

**Revisit if** the persona set changes enough that firing counts stop meaning anything
about a real year. The threshold is 11.1's, not the simulator's.

### The validator masks the person's own words before it reads a sentence

**Decided.** Checks 7, 8 and 10 read the rendered sentence with area names, item titles and
quoted response labels masked out. Check 9, the length check, reads the sentence as it will
appear. Recorded in section 8.

**Why.** An area is named by the person who made it. Somebody may spell a word the way they
were taught, put an exclamation mark in an item title, or write one in a language the
banned vocabulary list cannot spell. Vetoing on their string silences the engine over
somebody's own vocabulary, and the app already shows that exact string on every other
screen, so the veto would not even hide it. **The words the app chose are the words the app
is answerable for.** Length is the exception because a long name really does make a long
headline, and the reader sees the whole line.

**Considered and rejected: reading the whole rendered string.** It is the obvious
implementation and it fails on the first person who names an area in a language with
accents, which the non-ASCII rule would then read as a validator failure rather than as a
name. **Also rejected: sanitizing names on input**, which is the app editing what somebody
wrote about their own life.

**Revisit if** a masked check is ever found to have let something through that a reader
would fault the app for. The answer then is a narrower mask, not the whole string.

### A deferred simulator check runs, measures and reports, and only its verdict is deferred

**Decided.** Six of the ten checks in section 12 cannot pass in phase 5. Each carries a
deferral naming the date and the issue that lifts it, runs on every simulation, prints the
number it measured and prints its failures as loudly as an enforced check. What deferral
changes is only whether the build goes red.

**Why.** The obvious way to handle a check that cannot pass yet is to skip it, and a skipped
check produces no number. The whole reason the simulator is built before the corpus is so
that the growing can be aimed, and a gate that reports only on failure tells an author
nothing about how close the corpus is to the target it is being grown toward. A deferral
with a date and an issue is also the difference between a decision and a thing nobody comes
back to.

**Considered and rejected: an assumption or a commented out assertion**, which is a skip
with an apology attached. **Also rejected: relaxing the thresholds to what phase 5 can
meet**, which would leave the build green and the target lost, and is the failure mode that
turns a specification into whatever was convenient.

**Revisit if** phase 9 lifts a deferral. The deferral is deleted, not the check.

### Debug only is verified against the resolved source sets, not assumed from the layout

**Decided.** A Gradle task reads the source directories Gradle resolved for each variant and
fails unless the devtools package is present under a debug directory, absent from every
release one, and named by no file a release build compiles. It runs inside `verifyClarity`
and blocks `assembleRelease`.

**Why.** Putting a file in `src/debug` is the mechanism, not the verification, and the
failure mode is silent: the day somebody moves one simulator class into `src/main` so a
screen can reach it, nothing breaks and the release build quietly grows eleven synthetic
personas, a year of generated logs and a copy of the check suite. The first of the three
conditions is the one usually missing. A check that only looked for the package where it
must not be would pass on a repository where the simulator had been deleted.

**Considered and rejected: trusting the source set layout**, which is what everybody does
and what nobody notices failing. **Also rejected: a ProGuard rule**, which would strip the
code from the release binary and leave the source set wrong, so the next person would
inherit the mistake with the evidence removed.

**Revisit if** the simulator is ever wanted in a release build for a debug menu. That is a
product decision, and it would start with what a person is shown rather than with this task.

### The engine takes a zone at construction and keeps the signature 2.2 specifies

**Decided.** `ClarityEngine` and `ClarityValidator` take a `ZoneId` as a constructor
parameter. `observe(facts, history, purpose)` is unchanged.

**Why.** Three things in the engine need to know which local day it is speaking on: 7.6
hashes the date key, 5.1 buckets it, and every exclusion window measures against it. The
fact set carries the window as two instants and no zone. The ambient default is banned, and
`DomainPurityTest` fails the build on it, because `ZoneId.systemDefault()` is the documented
cause of two Pulses in one day or none at all. So the zone the extractor counted with is
handed in once and the date key is derived from the window itself.

**Considered and rejected: a fourth parameter on `observe`.** 2.2 states that signature and
every caller would then carry a value that never varies between calls. **Also rejected:
putting the zone on the fact set**, which would make it a fact, and it is not a fact about
the person's week; it is a property of the device asking.

**Revisit if** a person can ever change the zone the app counts in. Today the extractor's
zone is the app's, and both sides of this read the same one.

### The validator is a seam the engine cannot skip

**Decided.** `ClarityEngine` holds a `CandidateValidator` with **no default value**. There is
no constructor that omits layer 5. A permissive implementation exists for testing layers 3
and 4 alone and its name says what it does.

**Why.** `MASTER_BUILD_PROMPT.md` 11.4 says never bypass the validator, not for a simple
sentence, not for an empty state, not to fix a bug. A rule that can only be obeyed is
better than a rule that has to be remembered, and a seam with no default is the only kind
that cannot be forgotten. The failure it prevents is silent in the worst way: everything
would still render, and nothing would be checked.

**Considered and rejected: a default validator on the constructor**, which is what
convenience asks for and what a future session would reach for at four in the afternoon.

**Revisit if** the wiring in phase 6 makes construction awkward. The answer then is a
factory that supplies the real validator, not a default.

### Open, and a finding rather than a builder's choice: three corpus totals have drifted

**Not decided, and deliberately not fixed.** `CLARITY_LOGIC_ENGINE.md` 11.1 states 162
Momentum and banner lines and 1,519 authored lines in total. Counted at catalog load, the
Momentum volume carries 146 and the combined figure is 1,503. `CORPUS_3_MOMENTUM.md` itself
claims 112 Momentum headlines in two places and carries 96. Inside `CORPUS_2_REPORT.md`,
section 1's prose says 176 headlines against its own totals table's 158, and section 3's
says 128 patterns against 111; there the tables are right and the prose is stale. Pulse
agrees at 620 and the Report volume's table agrees at 737.

**Why it is left alone.** Whether a file grows to match its total or a total is corrected to
match its file is a question about the corpus, and phase 9 is where the corpus is grown. A
builder quietly editing a number in a specification to match what the code counted is the
exact move CLAUDE.md's authority order exists to prevent. The catalog counts the keyed lines
at load and holds the readings, so the drift is visible rather than remembered, and the
count is recorded at the point of the claim in 11.1.

**The recommendation, stated and not taken:** correct the two stale prose figures in
`CORPUS_2_REPORT.md`, which are wrong against that file's own tables and cost nothing to
fix, and leave the Momentum shortfall to phase 9, which will close it by authoring rather
than by editing a number.

**Revisit if** phase 9 closes the gap, or if the owner decides the totals should be
generated rather than written.

### The readings the deferred gates produced, which is what phase 9 is judged against

Eleven personas, a full simulated year each, against the corpus and the rules as phase 5
left them: 92 rules across 78 families, 3,148 simulated opens, 451 reports, and 1,388
invocations for the persona who accepts every plan.

| gate, `CLARITY_LOGIC_ENGINE.md` 12 | target | phase 5 measured |
|---|---|---|
| no variant repeats inside ninety days | none | 7,384 repeats, the tightest after 1 day |
| Pulse silence | 8 to 25 percent of opened days | 43 to 98 percent per persona, 76 percent overall |
| layer 6 silence | at least 15 percent of reports | not measurable, layer 6 is phase 9b |
| no family over a fifth of a year's Pulses | 20 percent | 27 to 60 percent per persona |
| every stage of every hot family fires | all | 29 hot families, one gap: `accumulation` never reached stage 2 |
| no two consecutive report leads share a band | none | 715 collisions across 451 reports |
| no three consecutive parallel numeric clauses | none | 27 runs of three or more |

The four enforced checks pass: no banned word, dash, emoji or non-ASCII character in any of
the year's sentences; no sentence naming an area with no events in its window; no visible
slot marker; and nothing in the plan-accepting persona's year referencing a plan, a
commitment, an intention or a failure to act.

**Two of those numbers say something the phase did not expect, and phase 9 should read them
before authoring anything.** The silence figure is three to twelve times the target band,
and the cause is not only bench depth: of the eleven Pulse families in 6.1, **six ever
fired**. Two have no rule at all, and `throughput`, `burst` and `queueDrain` have rules that
no persona's year ever satisfied. Of the 2,383 silent days, 1,238 were days where something
qualified and every candidate was filtered by a cooldown or by yesterday's family, and
1,134 were days where nothing qualified at all. **A bigger corpus moves the first number and
not the second.** Growing benches will fix the repetition figure and the length band
collisions; it will not fix silence, and the honest reading is that silence needs more rules
and, behind three of them, more facts in 3.1.

**Revisit if** any of these is measured again after phase 9. The table above is a baseline
with a date on it, not a target, and the targets are the ones section 12 states.

---

## August 27, 2026: the eleven open choices in the focus surface

Phase 4, issues #2, #28, #29, #30, #32 and #49. Addendum 01 4e, 4f, 4g, Step 5 and 8d,
`MASTER_BUILD_PROMPT.md` 10, 13.4, 14b.5 and 14b.6, `design-v3.md` 3.3, 8.2, 9, 10.18,
11, 11.3, 11.4 and 16. Ten things those documents leave to the builder, settled here
under `design-v3.md` 15, and one that is not a builder's to settle and is recorded as
open.

**Two platform first checks, recorded so nobody redoes them.** The Live Update is
the platform's own promoted notification progress style, taken at step 1 of
`design-v3.md` 17.1, and the end of session confirm is the platform `AlertDialog`
themed, taken at step 2. Neither earns a register row, because the register at the foot
of this file records components built by hand. What the availability check turned out
to be is worth writing down, since `MASTER_BUILD_PROMPT.md` 3.3 asks for the platform
to be verified rather than trusted: on androidx core 1.19.0,
`NotificationManagerCompat.canPostPromotedNotifications` answers both halves of the
question in one call, returning false below API 36 without touching the platform and
asking the platform above it. The register's `Depleting focus ring` row moves to built
in the same commit as this entry.

### Calm mode takes the collapse and the expanding circle, and leaves the check

**Decided.** In calm mode and under reduce motion the completion bloom is the check
appearing over 150ms. The ring does not collapse, the soft circle does not expand, and
the check does not scale from 0.6. Implemented in `ui/focus/FocusRing.kt` and stated in
`design-v3.md` 16.6 item 9, which this entry did not have to change because the row was
already written.

**Why.** The row in 16.6 says crossfade to the completed state and the check still
appears, and the question this phase actually had to answer is what "still appears"
means when the thing it appears out of is gone. A check that faded in where a bloom
would have been is the answer, because the check is the information and the bloom is
the atmosphere. Calm mode removes motion, not information, 16.4, and this surface has
exactly one piece of information at that moment: the session finished.

**Considered and rejected: keeping a shortened bloom.** Compressing 700ms to 150ms
produces a fast flare, which is more startling than the slow one it replaces and is the
opposite of what a person switched calm mode on for. **Also rejected: holding the ring
in place at zero.** It reads as a session that has not finished yet, which is a false
statement rather than a quieter one.

**Revisit if** anyone reports that the completion screen arrives with no sense that
anything happened. The honest fix then is the tone `MASTER_BUILD_PROMPT.md` 10 already
asks for and phase 4 did not build, not a return of the motion.

### The Live Update track is one segment and at most one point

**Decided.** One undivided segment carrying the area color, with progress set to the
seconds **remaining** rather than the seconds spent, so the track depletes. One point,
only when the transition warning is on, at the five minute position, taking no color of
its own. Implemented in `notifications/FocusNotificationPoster.kt` and stated in
`design-v3.md` 11.4.

**Why.** `MASTER_BUILD_PROMPT.md` 14b.6 says a single track is the likely right answer
and asks for segments or points to be used only if they genuinely add clarity, and the
two halves come out differently. Dividing the track would invent a structure inside a
session that the session does not have: nothing happens at the boundary between one
segment and the next, so a person would be reading a shape that means nothing. The
single point is the opposite case, because 11.4 makes the track reaching that point the
state change the transition warning asks for. The point is the mechanism rather than
decoration, and with the warning off there is no point and no state change at all.

**Considered and rejected: a segment per five minutes**, which is the demonstration
every platform release note leads with. It would put four boundaries on a twenty five
minute session, none of which is an event, for an audience that reads a shape rather
than a number precisely because arithmetic is expensive. **Also rejected: coloring the
point**, which 11.4 forbids in the same sentence that allows the track one color.

**Revisit if** a later session finds a real interval inside a session worth marking. A
platform feature is not one.

### The completion screen has one wording, and is not told which ending it is drawing

**Decided.** The value the completion screen is handed carries the session id, the area
name, the item, the real duration in minutes and whether the item can still be
completed. **It carries no field recording whether the planned time ran out.**
Implemented in `ui/focus/FocusViewModel.kt` and stated in `design-v3.md` 11.

**Why.** Addendum 01 4e requires a session ended early to reach the same screen in the
same words with the same actions, and the obvious implementation passes a boolean and
branches on nothing today. The problem with that is not this phase, it is the next one:
a fact a screen holds is a fact a later edit can render, and the edit that renders it
will look reasonable at the time. Not handing the screen the fact is the only version
of the rule that cannot rot. The duration line is the real duration, rounded to nearest
the way `domain/query/TrailRow.kt` rounds it so the same session does not read as
fourteen on one surface and fifteen on the other, and it is never a comparison against
what was planned.

The one asymmetry that survives is the sixty second threshold in
`MASTER_BUILD_PROMPT.md` 10, and it is deliberately about the interface rather than
about the log: both endings write `FOCUS_ENDED_EARLY`, and what changes under a minute
is that there is no confirm before it and no completion screen after it, because forty
seconds is a mis-tap rather than a short session somebody meant to have.

**Considered and rejected: a second, gentler completion screen for a short session.**
It is what a well meaning designer would build and it is the exact failure 4e names.
Fourteen minutes is fourteen minutes, and a quieter acknowledgment is a verdict
delivered in a whisper.

**Revisit if** the owner decides a person should be able to see, on the screen, that a
session ran its full planned length. That is a change to 4e and an owner's call, and it
would start with the corpus rather than with this model.

### The ring is thin and the weight is spent on the tip

**Decided.** A 6dp stroke on the 240dp ring, a 10dp filled tip, and a 15dp radial
falloff behind the tip at 38 percent. Implemented in `ui/focus/FocusRing.kt` and stated
in `design-v3.md` 11.

**Why.** v3 fixed the diameter and said nothing about the stroke, and the statistically
common answer is a heavy ring at twelve to sixteen dp, which is what an activity ring
looks like and what `design-v3.md` 15.1 names under a ring closing toward a daily
target. A heavy ring reads as a target filling even when it is depleting, because the
weight is what makes it look like a container being filled. A fine line with a bright
point traveling it reads as time passing, and it is the treatment that belongs in a
room lit by a radial glow and eight specks.

**Considered and rejected: a heavy ring with a lighter track**, which would have read
as the thing 15.1 warns about with an extra step. **Also rejected: no tip at all.** The
tip is what makes the direction of travel legible at a glance, which is the whole claim
in `design-v3.md` 11.3.

**Revisit if** the arc turns out to be hard to read at arm's length on the device. The
first move then is the tip, not the stroke.

### Both actions on the end confirm carry the same weight

**Decided.** `End` and `Keep going` are two text buttons in the same treatment.
Neither is filled, neither is styled as the recommendation, and neither carries the
`warn` haptic. Implemented in `ui/focus/FocusSessionScreen.kt`.

**Why.** The obvious answer is a filled confirm and a quiet dismiss, and it fails in
both directions at once. Filling `Keep going` recommends carrying on, which is the app
having an opinion about how long somebody should work. Filling `End` and dressing it as
destructive says ending is a loss, which is what Addendum 01 4e exists to deny. The
`warn` haptic is scoped by `design-v3.md` 9 to a destructive confirmation arming, and
this confirmation guards against a mis-tap rather than against a loss.

**Considered and rejected: no confirm at all past sixty seconds**, which
`MASTER_BUILD_PROMPT.md` 10 rules out by naming the dialog and its two words.

**Revisit if** the two equal actions test as hard to tell apart. The answer then is the
order and the spacing, not the weight.

### The Contemplative primary uses one of 10.7's two forms everywhere

**Decided.** Every filled control on the Focus surface is the surface accent at 14
percent with a bright label. The translucent white pill at 9 percent, which
`design-v3.md` 10.7 offers beside it, is not used anywhere in this app. Implemented in
`ui/focus/FocusControls.kt`.

**Why.** 10.7 offers two forms and does not say when each applies, and section 11
settles it by specifying `Mark item complete` on the completion screen as being in the
accent. A control that changed its treatment between two screens of the same surface
would read as two different controls, and the End session pill and the Mark item
complete pill are one component in this app with one appearance. One separation device,
6.1: a background lightness shift, no border, and no shadow, because the Contemplative
world has none.

**Considered and rejected: the white pill for End and the accent for Mark item
complete**, on the argument that ending is a different kind of act from completing. It
is not: both are ordinary, both are what the screen is for, and giving one of them a
different container would say otherwise.

**Revisit if** a later Contemplative surface needs two filled controls on one screen at
different ranks. Pulse in phase 6 is the first test of that.

### The Focus chip is permanent, and never becomes a countdown

**Decided.** The Focus chip is present in the Areas header at all times, including when
no area has an active item, and it keeps its label while a session runs rather than
becoming a live timer. Implemented in `ui/areas/AreasScreen.kt`.

**Why.** `design-v3.md` 10.1 calls the chip permanent, and the chooser has an empty
state of its own reading `Nothing to focus on yet`, which is a sentence that explains
the situation where a missing chip would leave a person wondering where the feature
went. A disabled control is a question somebody has to answer; a permanent one that
leads to an explanation is an answer. As for the countdown, it would be one line of
code, and the area card two thumbs below already carries the live countdown beside the
name of the item the session is on. **Two surfaces reporting the same number in one
screenful is how a person learns to read neither**, and the chip's job is to be the way
back in, which it does under the same label either way.

**Considered and rejected: hiding or dimming the chip with no active item anywhere.**
`design-v3.md` 10.16 settles the same question for the inbox chip in the opposite
direction, and the difference is real: the inbox chip is a count, so with nothing in
the inbox it has nothing to say, while the Focus chip is a door.

**Revisit if** the quick settings tile in phase 12 makes a second countdown surface
unavoidable anyway.

### Nothing stores whether the notification permission has been asked for

**Decided.** The request is made at one moment, the first tick of a session a person
just started, and no flag records that it happened. Implemented in
`notifications/NotificationPermission.kt`.

**Why.** The obvious implementation keeps a "we have asked" key so the prompt appears
exactly once, and it does not survive the check `design-v3.md` 15 asks for. From
Android 13 the platform already caps the prompt at two appearances for the life of the
install and denies silently afterwards, so the key would buy one fewer dialog in
exchange for a value in DataStore that has to be erased, exported and reasoned about.
It would also be wrong in the one case where asking again is right, which is a person
who granted the permission and later revoked it.

The moment is derived rather than announced, which is the part worth keeping: a session
whose elapsed time is under five seconds was started here, and a session restored after
a process death was not. Written as "the session id went from null to something" it
would fire at launch, because on a cold start the restore is asynchronous and the id
does go from null to something. `MASTER_BUILD_PROMPT.md` 13.4 forbids asking at launch
in the same sentence that asks for a contextual request.

**Considered and rejected: asking on the chooser rather than on the session.** It is
earlier and therefore feels safer, and it asks people who opened the chooser and
changed their mind.

**Revisit if** the platform changes its own cap, which is the only thing this reasoning
rests on.

### The completion notification takes the phone's own sound

**Decided.** The Focus channel is created at default importance with no call to
`setSound`, so a completion arriving while the app is elsewhere makes the sound the
person already chose for notifications. No tone is bundled. Implemented in
`notifications/ClarityNotificationChannels.kt`.

**Why.** `MASTER_BUILD_PROMPT.md` 13.4 asks for a gentle sound and the obvious answer
is to ship a soft chime. It loses for a specific reason rather than a stylistic one: a
person who has already set their phone to a quiet notification sound has told the
device what gentle means to them, and an app supplied tone overrides that decision on
the one notification this app posts that makes any sound at all. All three channels are
created together at process start, because a channel's importance and sound are frozen
for an install the moment it is created, and a channel created late by whichever code
path needed it is a channel whose settings were chosen by that code path.

**Considered and rejected: a bundled chime**, and **low importance for the completion
channel**, which would have made it silent and would have made the one moment this app
is allowed to interrupt for arrive as nothing.

**Revisit if** a person reports the completion sound is jarring, at which point the
answer is their own system setting and this decision is what makes that true.

### The chooser names an area in `textDim`, not in the area color

**Decided.** On the Focus chooser the area name is `textDim` and the 7dp dot beside it
carries the identity at full saturation. Implemented in `ui/focus/FocusChooserScreen.kt`.

**Why.** `design-v3.md` 3.4 permits an area label in the accent as one of four forms an
area color may take, and the helper that computes that variant was measured against a
Daylight card. On the indigo gradient it would be a fifth application point on a ground
nothing has measured, which is exactly the defect the phase 3c contrast audit found on
a surface that had been reasoned about rather than measured. The dot is excluded from
calm mode's transform by name, 16.2, because it is how an area is recognized, and it is
enough on its own.

**Considered and rejected: measuring the accent variant against the indigo ground and
using it.** It is the better looking answer and it is a contrast audit this phase did
not run. It can be done later with a number in hand.

**Revisit if** the phase 12b surface pass measures the Contemplative ground and finds
headroom.

### Open, and not a builder's to settle: a session a person ends fires no haptic

**Not decided. This is the owner's.** At a natural completion, with the person watching
the ring, the completion screen fires `focusEnd`, which is `design-v3.md` 9's row for
that moment. At a session the person ends themself, the End control fires the ordinary
`tap` and the completion screen that follows fires nothing. A session resolved on the
next resume fires nothing either, and that part is settled: section 9 forbids a haptic
on screen entry.

**Why it is open.** Issue #28 asks for the completion haptic and tone for an early end
to be the same as for a natural completion **or deliberately gentler**, and says
plainly that they are not to be absent, because absence reads as the app withholding
acknowledgment. What was built is absent. There is a real argument for it, which is
that section 9 fires nothing more than once per user action and the tap on End is that
action, so firing `focusEnd` a beat later would be a second event for one gesture. That
argument was never written down as a decision, and the requirement it collides with is
explicit, so recording it as settled would be recording a choice nobody made.

**The recommendation, stated and not taken:** fire `focusEnd` on the completion screen
for both endings, and let the End control fire nothing, so the acknowledgment lands
where the completion is rather than where the tap was. That is one event per gesture,
it satisfies #28 without a new haptic row, and it makes the two endings identical on
the one surface Addendum 01 4e cares most about. The alternative, a deliberately
gentler event, needs a row in section 9 that does not exist and would make the two
endings distinguishable by feel, which is the thing 4e is against.

**Revisit if:** it is open, so it does not need one. It should be closed before phase
13's accessibility and haptics pass, which is where a missing acknowledgment is found
by hand.

---

## August 27, 2026: the polish pass, and the design foundations under it

Phase 3c, issue #53, and the split that puts the rest of it in phase 12b, issue #54.
Addendum 01 3c, `MASTER_BUILD_PROMPT.md` 19, and `design-v3.md` 1, 3.1, 3.4, 5.3, 10.3,
11, 13, 14 and 15. Eight things settled, seven of which required changing the document
that is the authority on anything visual, which is the owner's call and not a builder's.

**The starting fact, recorded plainly because it is the least comfortable one.**
Addendum 01 3c asks for a polish pass and no phase ever carried it. When the addendum
was recorded, earlier the same day, 3c was written into `design-v3.md` 17.4 and
`MASTER_BUILD_PROMPT.md` 3.2 as a rule governing platform versus custom, and the pass
that the same section describes was never turned into a phase. That is a recording
error rather than a change of plan. **The owner found it by looking at the app rather
than at the plan**, on the build at 0.4.0:

> "I don't know if we've gotten to the design, polish, and premium feeling layer that's
> added on top of the Material 3 Expressive. But I can tell you so far what I've seen is
> not that great."

An audit was commissioned to separate three causes that have completely different
fixes: implementation drift, where the code does not match a number the document
already states; missing content, where a screen is running at partial content because
its phase has not arrived; and a genuine gap, where the code matches the document and
the result is still flat. **It found the third dominates**, and about twenty drift
findings of which five were visible on a screen the owner could open.

**The finding that made every decision below the owner's.** `design-v3.md` removed
every conventional decoration device on purpose, which is a strong position and the
right one, and it moves the entire burden onto four things: type, space, value and
motion. Measured on the two built screens, three of the four were not carrying. Value:
card against canvas at 1.126 to one, `raise` declared and drawn nowhere, and two colors
covering 91.6 percent of the Areas screen. Type: tracking set on two of the nine sans
roles, `body` and `bodyStrong` both at 16sp, and the signature serif appearing at five
call sites in the whole app of which four were empty states, which had quietly made
Newsreader mean "there is nothing here". Motion: nothing moving at rest. Space was
carrying, alone, and what it was expressing was emptiness. **None of that is reachable
by conforming harder**, which is the whole reason the document moved instead.

### The light world buys its depth downward, and pure white loses

**Decided.** `canvas` goes from `#F1F1F6` to `#E6E6EC`, L\* 95.3 to 91.4. `card` goes
from `#FFFFFF` to `#FCFBF9`, L\* 100 to 98.6, and is warm. `raise` goes from `#FAFAFC`
to `#F4F3F0`, L\* 98.3 to 95.8. Card against canvas moves from **1.126 to one** to
**1.202 to one**, and the light world's span from 4.73 L\* to 7.19. In dark, where
`design-v3.md` 6.1 allows no shadows at all and the lightness ladder is therefore the
only separation device there is, `card` goes `#191921` to `#1D1D25` and `raise`
`#15151C` to `#18181F`, while `canvas` is deliberately held at `#0E0E13`. Stated in
`design-v3.md` 3.1 and 3.2 and held by `SurfaceLadderTest`.

**This resolves a contradiction that had survived two phases, and it is recorded as
resolved rather than quietly fixed.** `design-v3.md` 1 says "backgrounds are never pure
white or pure black". Section 14 says "no pure white or pure black backgrounds". 3.1
said `card` is `#FFFFFF`, and 10.3 repeated the hex rather than naming the token. **Two
statements against one, and the build had followed the one**, faithfully, because 3.1
is the token table. The two win. 10.3 now names the token, because a component section
repeating a color out of the token table is how the contradiction lasted as long as it
did.

**Why the ground moved rather than the card.** There was no headroom. `card` was
already pure white, so no amount of lifting it could produce a step, and the only
direction with any room in it was down. That is also the answer to the instinct the
audit named and `design-v3.md` 15.3 now refuses: when an element does not separate,
measure whether the ground under it has any room left before reaching for the element.
The new canvas keeps the cool lean section 1 asks for, at 6 points of blue over red,
and the new card is warm at 3 points of red over blue, which is the "warmth in the
cards" the same sentence promises and which nothing in the app delivered. `cardWash` is
the user's own area color and four of the eight moods are cool, so the default area put
a cool wash on a pure white card.

**Considered and rejected: taking the dark canvas toward pure black**, which is the
statistically common 2026 answer for an OLED panel and would have bought dark its step
the cheap way. Section 14 bans pure black outright, and the Contemplative world's
`deepBlack` is only 0.97 L\* below the Daylight canvas, so a darker Daylight ground
would collapse the two worlds' floors and take the room dimming out of entering Focus.
Dark bought its depth upward instead, which is the exact mirror of the light world's
problem. **Considered and rejected: leaving dark alone** on the theory that the light
world was what the owner was looking at. Dark measured 1.102 to one card against
canvas, worse than the light world that also had a paired shadow to help it, and a
device capture was 86.6 percent one color.

**One half of the contradiction is deliberately left open.** Section 1 also says "dark
surfaces are warm blacks" and every dark token in 3.2 leans cool by 5 to 8 points of
blue over red. That is a separate decision about four tokens across two worlds and it
is left to be taken as one rather than folded into this entry.

**Revisit if** the parchment weekly banner arrives in phase 5 and still fails to
separate. It measured 1.036 to one against the old canvas, which is to say it would
have arrived **less** visible than the card was; against the new canvas it is a
different number and it was not re-derived here, because nothing draws it yet.

### `inkSecondary` moves to 0.64, and it was forced rather than chosen

**Decided.** The light `inkSecondary` goes from ink at 0.60 to ink at **0.64**.
Stated in `design-v3.md` 3.1, held by `TrailContrastTest`.

**Why this is in its own entry.** It is not a design improvement and nobody proposed
it. It is a second order effect of the canvas change, and it is the kind of effect that
gets missed. At 0.60 the light canvas was already the tightest surface in the app at
**4.5046 to one**, four ten-thousandths above `design-v3.md` 13's floor of 4.5, and a
test written in phase 3 pinned it there with a comment predicting that "a later
darkening of `canvas` would take the whole Daylight world under the floor without
touching this screen". Darkening the canvas did exactly that, to **4.33**. So the
canvas change is not free: it costs four points of alpha on an ink token, everywhere,
and had the prediction not been written down the first symptom would have been an
accessibility failure on a screen nobody had edited.

At 0.64 the tightest ground in the app is the light canvas at **4.88 to one**. The
raise also retires a defect phase 3b had to pin as unfixable rather than fix: at 0.60
this token measured 4.48 to one on a resting area card and **4.27 on an in-session
one**, and 16.7 recorded it as measured and held in a test because raising an ink token
for every screen in the app was out of scope for a calm mode audit. It is in scope for
a token pass. Both now clear, at 5.05 and 4.75.

**Considered and rejected: leaving the ink alone and taking less canvas.** A canvas
light enough to keep 0.60 over the floor is a canvas that does not buy the card a step,
which was the entire point. **Considered and rejected: raising the alpha only where a
measurement failed**, which would have made `inkSecondary` mean two different values
depending on the ground and defeated the purpose of a token.

**Revisit if** any future change to `canvas`, in either world, moves the tightest
measured pair. The rule this entry establishes: **the ink tokens are downstream of the
ground tokens and are re-measured whenever a ground moves.**

### `raise` gets a job

**Decided.** The three neutral surfaces are a **rank ladder** and each rank has named
occupants. `canvas` is the page. `raise` is chrome: the floating tab bar and an
unselected chip. `card` is content: area cards, sheets and the undo snackbar. Content
is the top plane and chrome recedes from it, and the statement is the same in both
worlds, which is why the steps are matched across them rather than each world being
tuned alone. Stated in `design-v3.md` 3.1, held by `SurfaceLadderTest`.

**Why.** 3.1 defined `raise` as "the 3 percent lightness step used instead of a border"
and never said where it goes, so it went nowhere: it was handed to Material's
`surfaceVariant` and drawn by nothing. The card, the tab bar, the sheets, the chips and
the snackbar were therefore one value doing four semantically different jobs, and a
screen of cards read as a screen of chrome. It would not have helped even if something
had drawn it, at 1.68 L\* under the card. Giving it a rank roughly triples the depth
budget of the app using no decoration whatsoever, which is the only kind of depth this
design system permits.

**`raise` plus a shadow is one separation device, not two, and that needs saying
because it looks like a violation.** 6.1's prohibition is specifically "never a hairline
and a shadow on the same element", and 3.1 defines `raise` as the step used **instead
of** a border. A card has always been lighter than the canvas and has always carried a
shadow. A value is what a surface is; only a deliberate step standing in for a hairline
is the device. Putting a hairline on anything in the ladder is still forbidden, and
`design-v3.md` 15.3 refuses it by name.

**Considered and rejected: putting sheets on `raise` as well**, which is the tidier
rule, one value for everything that floats. A sheet is where a person reads and types
and is content, not chrome. **Considered and rejected: putting the undo snackbar on
`raise`** with the rest of the floating furniture. For five seconds it is the top plane
on the screen and what it holds is the only way back from a deletion; chrome recedes
and that cannot afford to.

**Revisit if** a surface has to be drawn **inside** a sheet, which is the one job
`raise` was defined for and does not yet have. The color picker preview in 10.9 is the
likeliest first.

### Tracking runs the whole sans scale, and `body` and `bodyStrong` stop being the same size

**Decided.** Every one of the nine sans roles in `design-v3.md` 5.3 carries a tracking
value, and a role without one is now a defect rather than a default. The ramp opens at
the small sizes and closes at the large ones: +0.032, +0.022, +0.016, +0.004, -0.006,
-0.014, -0.022, -0.030em, from `swipeLabel` at 10.5sp to `timerNumeral` at 64sp.
`sidehead` sits deliberately off the ramp at +0.024em, above `label` at the same 13sp.
Separately, `body` drops from 16sp to **15** and `bodyStrong` rises from 16sp to **17**.

**Why tracking.** Hanken Grotesk carries one set of sidebearings, drawn to fit at
reading sizes. Set large it leaves proportionally too much room, because the space
between letters does not need to grow at the same rate the letters do; set small it
leaves too little, for the same reason in reverse. The correction is the oldest one in
typesetting. Two of the nine roles carried a value, so the seven that make up almost
every string on both built screens ran at whatever the font file happened to ship. This
is the cheapest change in the whole pass and it touches every label, sidehead, caption
and chip in the app, built and unbuilt.

**Why `sidehead` is off the ramp.** A section label has to read as a marker rather than
as a sentence, and the conventional way to buy that is capitals, which 5.3 and section
14 and 15.1 all forbid. Tracking says the same thing at a fraction of the volume and
leaves the sentence case intact. Recorded under section 15 as an open choice taken
against the obvious answer.

**Why the two body roles split, and why the step is uneven.** They were both 16sp,
which made a size hierarchy impossible inside a run of text. `design-v3.md` 11 gives a
Trail day header to `bodyStrong` and the rows under it to `body`, so the header could
outrank its rows by weight and by nothing else, and a device capture of four events
read as a wall of one size at 12 and 16sp. Forty rows would read worse. Most of the
step is taken below rather than above, because `bodyStrong` is also the button label in
10.7 and the undo action in 10.14, and one step is as far as either can move without
starting to shout. 16sp is in any case Material's `bodyLarge` default, so moving off it
is the choice section 15 asks for, and 10.11 already set a settings row title at 15sp,
which makes 15 an established row size in this document rather than a new one.

**Considered and rejected: a new role for the day header** and leaving the body pair at
16sp. It adds a fifteenth name to a scale of fourteen to avoid admitting that two of
the existing fourteen were the same size, and it would have left every other run of
text in the app with no step available. **Considered and rejected: moving `bodyStrong`
to 18sp** for a cleaner 15 and 18. It is one step from `title` at 19sp, and a button
label at 18sp is a loud button.

**Revisit if** a screen built in a later phase needs a size between 15 and 17. The sans
ladder is 13, 15, 17, 19, 21, one even step per rung, with 12 and 10.5 beneath it, and
inserting a rung is a change to the ladder rather than an addition to it.

### The Trail gets a serif title, which reverses a decision phase 3 took deliberately

**Decided.** The Trail opens with `Trail` in `displayTitle`, left aligned on the same
measure as the day headers, no glyph beside it, the same treatment the Areas title
takes in `design-v3.md` 10.1. Stated in `design-v3.md` 11 and built in
`ui/trail/TrailScreen.kt`.

**This reverses a decision, and the old one was not careless.** Section 11 gave the
Trail day headers and rows and stopped, phase 3 read that silence as intentional, and
it reasoned the reversal out: the tab bar already says Trail, so a heading reading Trail
repeats a word the user can see at the other end of the same screen. That is a real
argument and it is still true.

**What changed is not the argument, it is what the argument was weighed against.**
Phase 3 priced the repetition and did not price the absence, because with one screen
built there was nothing to price it against. Section 11 opens every other surface in
this app with a headline treatment: Areas takes a `displayTitle`, the Report an eyebrow
and a `displayHero`, Momentum a `readSerif` headline, About a `displayTitle`. The Trail
was the one screen in the document that began with its content. The audit measured what
that cost. The built Trail contained **no serif glyph at all**, and across the whole app
Newsreader had five call sites, of which four were empty states, so the signature
typeface of this design system meant "there is nothing here" four times in five. The
same absence had a second effect the audit named separately: with no title, the loudest
element on the Trail was the selected `All` filter chip, so **a filter outranked
everything it filtered**. One serif line fixes both, and it changes the app-wide ratio
as well as this screen.

**Considered and rejected: a different treatment for this title**, something with more
character than the Areas title, on the argument that the Trail is a different kind of
screen. Two screens that open the same way are one app; a Trail title styled to be
interesting would be a second design language for the screen that reads history.
**Considered and rejected: reusing `tab_trail` for the string.** The two say the same
word today and are free to stop, and a tab label and a serif screen title are not the
same kind of copy.

**Revisit if** a later screen has something that genuinely belongs beside its title, as
the archive and settings glyphs do on Areas. The Trail has nothing, which is why it has
no glyph and not because a title never takes one.

### The idle card title comes off `inkTertiary`, because section 13 beats 10.3

**Decided.** `Add your first item` and the `Last active` status line under it read at
`inkSecondary` rather than `inkTertiary`. The weight drop from 650 to 500 is unchanged
and is what carries the idle state. Stated in `design-v3.md` 10.3.

**Why, and it is a contradiction inside one document rather than a preference.** 10.3
said the idle title reads "at inkTertiary weight 500" and the same section calls the
active title "the most important string on the screen". `inkTertiary` measures **2.40
to one** on the card in light and 3.22 in dark, against section 13's floor of 4.5.
**13 wins, because a floor is a floor.** `inkSecondary` is the one step down from
`inkPrimary` that clears, at 5.29 to one in light and 6.36 in dark on the phase 3c card.
The idle state loses nothing: the weight is what says "waiting to be filled", and it
says it whether or not the color is also below the threshold at which a person can read
the invitation they are being given. The status line was the worse of the two, being
12sp rather than 21, and 10.3 named no color for it at all, so there was no
contradiction there to resolve, only section 13 to obey.

**Two inert weight calls were retired in the same change and they are worth recording,
because nothing failed.** `AreaCard.kt` asked for weight 500 on the idle title and 600
on the in-session status line, and both rendered at the wrong weight in phases 2 and 3b.
The sans family was built one weight per `FontFamily` with `FontVariation.weight` baked
into the typeface at load, so the font matcher had exactly one face to return and
`TextStyle.copy(fontWeight = ...)` resolved to the pinned instance. **The requested
weight was discarded with no error and no visible symptom.** The family now registers
every weight the design system asks for, so the matcher has something to choose between.

**Considered and rejected: keeping `inkTertiary` and lifting the alpha until it
clears.** `inkTertiary` at an alpha that clears 4.5 to one is `inkSecondary` with a
second name.

**Revisit if** a variable weight axis is ever needed at a weight not on the registered
list. A request off the list resolves to the nearest one that is, which makes a missing
entry a slightly wrong rendering rather than a silent no-op, and that is the contract
this change restored.

### The default area walk starts at Berry and never hands out a function color

**Decided.** Two changes, and the second is the load bearing one. The walk starts at
**Berry** rather than Ocean, and it never assigns `#2D7FF9`, `#4DA3FF`, `#22C55E` or
`#F59E0B`, each of which is byte identical to a function token in `design-v3.md` 3.1 or
3.2. **All four remain choosable**: 3.4 opens with "all 48 available to everyone" and
that is untouched. What changed is only what the app hands out on its own.

**Why.** The shipped walk gave the first area `#2D7FF9`, which is `actionBlue`. On the
first run screen, where the only two colored elements in the entire app are one area and
one FAB, they were the same color, so identity and function collapsed on the first
screen a new user ever sees. **An identity indistinguishable from a status is not an
identity.** It is also not a single collision: `#22C55E` is the first color of Meadow,
so the walk also handed out `positiveGreen`, the completion color, at area five, and it
reaches both inside the first eight areas from **every** starting point. Moving where
the walk starts cannot fix that. Naming what it may not hand out can, which is why the
reservation is the real change and the starting point is the smaller one.

**Why Berry.** 3.4 asks the walk for one thing, that the first four be distinct without
the user choosing, and the shipped start did not deliver it: `#2D7FF9` at hue 216 next
to `#6366F1` at 239 is a 23 degree step and two blues in a row. Berry's first four are
292, 0, 142 and 41, whose narrowest step is 68 degrees, the widest that any of the eight
possible starts produces, measured across all eight rather than assumed.

**Considered and rejected: Twilight, which is the obvious move**, one step along the
list from Ocean. `#6366F1` is 23 degrees from `actionBlue`, an indigo that reads as a
shade of the button rather than as an identity, and it walks toward the family 15.1
names twice. It is the worst available start on both counts. **Considered and rejected:
Earth and Stone**, both of which sit further from `actionBlue` than Berry does. Starting
at Earth puts `#CA8A04` and `#A68B6B` eight degrees apart in the first four, two muted
yellows a person has to compare rather than recognize; starting at Stone takes the
narrowest step to 22. Each buys distance from one button by giving up the distinctness
the walk exists for. **Considered and rejected: stepping over a reserved color at
assignment time** rather than filtering it out of the list. Stepping maps two different
area counts onto the same color, so area three and area nineteen would both come out
`#18BFFF`; filtering keeps the walk a bijection over each mood's remaining colors, which
is the property that makes "distinct without anyone choosing" true past the first eight.

**Revisit if** a function color is ever added to or removed from 3.1 or 3.2. The
reserved list is derived from that table and is not independent of it.

### The pass is split: foundations now, surfaces at phase 12b

**Decided.** Phase 3c, tokens and type, sits between 3b and 4. Phase 12b, surfaces,
sits between 12 and 13. `MASTER_BUILD_PROMPT.md` 19 carries both, issue #54 carries
12b's acceptance criteria.

**Why, and it is not a compromise between doing it now and doing it later.** The two
halves have genuinely different properties. **A token or a type role corrected now is
inherited free by every screen that does not exist yet.** Focus, Pulse, Momentum, the
Report, onboarding, Settings, About and six widgets will all be built on the corrected
values without anyone touching them again. Corrected at phase 13 instead, all eight of
those would have been composed against a value structure the owner has already said is
not good enough, and every one would have to be re-examined, which is exactly the
re-polishing cost the scheduling tension in #53 names. The two highest impact changes
in the entire audit are both token changes.

**Surface changes have the opposite property and get better information by waiting.**
Fading the scroll edge on Areas does nothing for Momentum. Deciding what a text field
looks like is worth doing once, when there is more than one sheet to judge it against.
And the Contemplative world is unbuilt: the indigo Focus night, the amber Pulse night
and the gold Report carry half this app's character and all of its atmosphere, and
polishing the Daylight surface with no Contemplative surface to balance it against is
judging half a design against itself.

**Phase 12b and not a bullet inside phase 13**, which is the part of this decision most
likely to be undone by someone being sensible. Phase 13 already carries the Baseline
Profile, R8, the accessibility pass, the full checklist, real screenshots, the README,
the store listing and the release. **A polish pass buried inside a ship phase is the
first thing cut when a date moves.**

**One expectation set honestly, because it is easier to set now than to explain later.**
Phase 3c moves the app from flat to solid. It does not move it from flat to distinctive,
and it was not designed to. Distinctive is 12b, and it is mostly five open questions
none of which can be answered well with two screens on the table: whether anything in
this app moves at rest, what the Trail's event circle carries, whether an inactive tab
keeps its label, what a text field looks like, and what a sheet full of choices looks
like. 3c should be judged as "the foundation is now right".

**Considered and rejected: doing all of it now**, which polishes four screens and
re-polishes as eight more arrive. **Considered and rejected: doing all of it at phase
13**, which lets eight more screens be built against a look nobody is happy with and
turns the pass into a rewrite. **Considered and rejected: waiting for the whole pass
before shipping anything**, which the audit argued against directly: the largest single
visual return in it was thirty minutes of work, an area card whose shadow was being
clipped away in full by the row that made it swipeable.

**Revisit if** phase 12b's five questions turn out to be answerable earlier than phase
12, which would mean the Contemplative surfaces built in phases 4, 6 and 8 settled them
on arrival. That is a reason to pull 12b forward, never a reason to fold it into 13.

### Where the audit's refusal list went, and why

**Decided.** The audit's "what not to do" list is `design-v3.md` **15.3**, a section of
its own, rather than additional entries on 15.1.

**Why.** 15.1 is a dated record of what the industry currently produces, 15.2 requires
it to be re-swept before every release, and it is expected to churn. The refusals are
not industry observations and they do not expire: each is a specific temptation in this
specific app, paired with the sentence in this document that already forbids it, and
re-deriving them at every release is the work the section exists to prevent. Merging
them would also blunt 15.1, which is read as a list of tells to check a design against,
while half the refusals are not tells at all but fixes that break a rule. The two lists
fail in opposite directions, which is the last reason they are two: a stale entry on
15.1 costs a false alarm about a treatment nobody makes any more, and a missing entry in
15.3 costs the treatment.

**Considered and rejected: `DECISIONS.md`**, meaning this file, which is where the
reasoning would naturally go. This file has no authority over anything and says so in
its own header. A refusal that has to hold against a session reaching for the nearest
premium looking thing has to live in the document that wins on anything visual.

**Revisit if** 15.3 grows past the point where a person will read it before starting.
It is fifteen entries and it is meant to be read once per pass.

## August 27, 2026: six open choices in the calm mode and entrance retrofit

Phase 3b, issues #48, #50 and #27. Addendum 01 8c, 8e and 4d,
`MASTER_BUILD_PROMPT.md` 14b.4 and 14b.12, `design-v3.md` 8.4 and 16. Six things
those documents leave to the builder, settled here under `design-v3.md` 15.

**The capture path's four choices are the entry below this one** and are not repeated
here: the FAB captures into the inbox, deleting an area does not orphan its items, the
first step truncates on the card and wraps in the sheet, and the estimate is a free
number field.

### An app session is the process lifetime

**Decided.** An entrance fires on the first open of its tab per app session, and a
session begins at process start and ends when the process ends. A rotation or a theme
switch does not re-arm it. A process death does. Implemented in
`ui/theme/ClarityEntrance.kt` and stated in `design-v3.md` 8.4.

**Why.** The flag is stored as **which session spent it** rather than as a boolean,
and that is the whole trick. `design-v3.md` 8.4 wants a rotation to leave the entrance
spent and a process death to re-arm it, and those pull in opposite directions: a
`remember` loses a rotation and would re-fire on every one, a `rememberSaveable`
survives a system initiated process death and would therefore never re-arm. Holding a
token read once per process settles both, because a bundle restored into a new process
carries a token that matches nothing. The token is `SystemClock.elapsedRealtimeNanos`,
read once when the holder initializes: two launches cannot read the same nanosecond,
and a reboot resets the clock but also discards every saved bundle.

It is keyed to the tab rather than to the screen, in the per tab saveable state the
shell has held since phase 3, so a sheet opening over Areas is not a first open.

**Considered and rejected: re-arming after some period in the background**, which is
what a person would guess if asked, and which several apps do. It invents a threshold
nobody asked for, it makes one screen behave two ways for a reason the user cannot
see, and predictable interface behavior is worth more to this audience than a second
chance to show an animation.

**Revisit if** phase 8's Report reveal needs its content key, which `design-v3.md` 8.4
already carves out as the one entrance that re-arms on a content change. That is a key
alongside the session token, not a change to what a session means.

### Calm mode does not go back to following the system

**Decided.** Calm mode follows the OS reduce motion setting, live, until the user sets
the switch. The first explicit choice gives the switch a value of its own and it stops
following. There is no path back to following, short of erasing all data. Implemented
in `data/prefs/ClarityPreferences.kt` and `ui/theme/CalmMode.kt`, and stated in
`design-v3.md` 16.1.

**Why.** A control that silently changes state because something outside the app
changed is a control this audience cannot rely on. Predictability is worth more here
than saving somebody one tap. The storage carries three states, absent or true or
false, and the interface shows two, which is what makes both halves true at once: the
default is not off, it is whatever the system asks for, and a `Boolean` defaulting to
false would have meant off for every person who has the system setting on and never
opens Settings. That is precisely the person the feature exists for.

**Considered and rejected: keeping the switch tracking the system whenever the two
happen to agree.** It is the tidier model and it is unpredictable in the one way that
matters: the app would look different tomorrow because of something the person changed
in a different app last week. A three-state control, On, Off, Follow system, was
rejected separately in `design-v3.md` 16.1, because a third state is a third decision
on a settings screen already full of them.

**Revisit if** anyone asks for a way back to following the system. `Erase all data`
clears the key with everything else, which is a real answer but a heavy one.

### The entrance starts at the screen title, not at the first card

**Decided.** The Areas header takes entrance index 0 and the cards follow at 1 and
after, so the screen arrives as one sequence. Implemented in `ui/areas/AreasScreen.kt`.
The conflict cards deliberately take no entrance index at all.

**Why.** The obvious answer, and the one most list screens use, is to stagger the rows
and leave the header fixed. A fixed title with rows pouring in underneath reads as
content loading into a frame. What this entrance is for is the app arriving. Starting
at the title costs one stagger step, 50ms, and makes the screen one thing rather than
two. A conflict card is an interruption that arrives because a merge produced one,
carrying its own reveal, rather than part of the screen's resting content, so it is
outside the sequence.

**Considered and rejected: a fixed header.** Cheaper by 50ms and it reads as a loading
state, which is the one thing an entrance must not look like.

**Revisit if** a header ever carries something a person needs before the list, at
which point the thing that needs to be immediate is the argument, not the header.

### The color picker's swatches keep their true color in calm mode

**Decided.** The 48 swatches and the selection ring are not transformed. The live
preview card beside the grid is. Implemented in `ui/areas/ColorPicker.kt`, where the
preview goes through `Modifier.areaWash` and the swatches call `parseAreaColor`
directly, and recorded in `design-v3.md` 16.7.

**Why.** `design-v3.md` 16.2 sorts every use of an area accent into atmosphere, which
takes the transform, and identity, which does not. A swatch is neither. It is a
**choice**, and a person picking a color has to see the color they are picking. A
desaturated grid would have somebody choose one accent and receive a different one the
moment they turned the switch off. The preview card takes the transform for the
opposite reason: it is a miniature of the real card and its job is showing what the
card will look like, which in calm mode is calm.

**Considered and rejected: transforming the grid too, for consistency.** Consistency
is the argument, and it is the wrong one: it would make the picker consistent with the
card at the cost of making it inconsistent with the thing it is picking.

**Revisit if** the swatch grid is ever shown without the live preview beside it, which
is the arrangement that makes the distinction legible.

### The platform bottom sheet cannot honor calm mode, and it ships anyway

**Decided.** `ClaritySheet` stays on Material's `ModalBottomSheet`. With calm mode on
and the system reduce motion setting off, the sheet's own entrance and dismiss still
travel. The 42 percent scrim and everything drawn inside the sheet do honor calm mode.
Recorded in `design-v3.md` 16.8.

**Why.** The sheet's animation is the platform's, it honors the system animator scale
and therefore honors reduce motion, and it exposes no specification an app preference
can reach. Two standing rules point in opposite directions here: `design-v3.md` 17.2's
fourth reason says a component that cannot honor calm mode is a component that cannot
ship, and 17.4 says a polish pass never reimplements a working platform component.
Both are true, which makes this a decision rather than a defect to fix quietly. The
person calm mode exists for almost certainly has the system setting on as well, which
is the case where the gap does not arise; the gap is real only for somebody who wants
the app calm and the rest of their phone lively.

**Considered and rejected: hand-building the sheet.** It would be the most used
component in the app rebuilt to change one curve, and it would put predictive back,
the insets behavior, the drag to dismiss and the accessibility handling on this
project's own maintenance. The standing register at the foot of this file exists for
components with no platform equivalent, and a bottom sheet is not one of them.

**Revisit if** the platform grows a hook for its own sheet animation, or if the sheet
turns out to be the one thing a calm mode user says bothers them.

### Re-entry detection hands out the date of the return, never the length of the absence

**Decided.** `ReEntry` carries `returnedOn` and nothing else. There is no field, no
function and no overload anywhere in `domain.query` that yields the number of days a
person was gone. `daysSince(dateKey)` exists, counts forward from the return, and
every caller of it is a suppression window. Implemented in
`domain/query/TrailFacts.kt` and `domain/query/TrailQueries.kt`.

**Why.** `MASTER_BUILD_PROMPT.md` 14b.4 forbids stating the length of the gap "not in
days, not in weeks, not as a date, not as `since March`". The statistically common
shape for this is `fun gapDays(): Int?`, and it is the shape that makes the forbidden
screen a one line mistake four phases from now, when somebody who has not read 14b.4
is building the surface and finds a number sitting there that looks like it wants to
be rendered. A prohibition that rests on somebody remembering it has a shelf life. This
one rests on the number not existing.

**Considered and rejected: carrying the gap and marking it forbidden in KDoc.** It is
the version that survives review and fails in six months. The comparison against the
14 day threshold happens inside the query and the number does not survive the return.

**Revisit if** 14b.4 itself changes, because nothing short of that could make the
length of somebody's absence a thing this app is allowed to say.

---

## August 27, 2026: the four open choices in the capture path

Phase 3b, issues #24, #25 and #26. Addendum 01 4a, 4b and 4c, `MASTER_BUILD_PROMPT.md`
14b.1 to 14b.3, `design-v3.md` 10.16 and 10.17. Four things those documents leave to
the builder, settled here under `design-v3.md` 15: identify the statistically common
answer, and take something else unless the common one is genuinely best.

### The FAB captures into the inbox, not into the first area

**Decided.** With at least one area, the FAB opens the add sheet with **no area
chosen**, and the item is written with a null `areaId`. The add sheet carries no area
control at all. Adding straight into a known area stays where it already is, on that
area's detail sheet, where the area is context rather than a choice. At zero areas the
FAB still creates an area, which `MASTER_BUILD_PROMPT.md` 8.4 states and 14b.1 leaves
standing until its own open question is answered.

**Why.** The obvious answer is an area picker on the add sheet with the inbox as one of
its options, and it is the wrong one twice. It puts a decision with N options between
the thought and the record, which is the exact thing 4a exists to remove, and it makes
the inbox look like a place you file into rather than the absence of filing. What the
FAB did before this was worse than either: it added into whichever area sorted first,
which is a decision the app made on the person's behalf and got right only by
accident. Nothing is lost by removing it, because there was never a picker to lose.
The FAB now means one thing everywhere, and it means capture.

**Considered and rejected: a row of area chips at the foot of the add sheet**,
deselectable, nothing selected by default. It is defensible and it was close. It loses
on the same ground the picker does: a control that is visible is a decision that has
been offered, and an audience whose difficulty is deciding will read an unselected row
of areas as an unanswered question. It also grows the sheet at 200 percent font past
what one screen holds.

**Revisit if** 14b.1's own open question is answered the other way, or if the Quick
Capture widget in phase 12 makes the FAB's meaning ambiguous again.

### Deleting an area does not orphan its items into the inbox

**Decided.** `AREA_DELETED` tombstones the area's items with it, which is what
`ClarityReducer.areaDeleted` already does. Nothing moves to the inbox. Asserted in
`ClarityInvariantsTest` so that reversing it is a deliberate act rather than a side
effect of an edit somewhere else.

**Why.** The obvious answer is to orphan, on the reasoning that nothing is lost that
way. It loses anyway. Deleting an area is already the most deliberate act in the app:
it takes a typed confirmation, it has no undo, and the sheet says in plain words that
the area and everything in it goes and that the history stays in the Trail. Thirty
queued items reappearing in the inbox would contradict a sentence the person read and
typed DELETE against, which is worse than either behavior chosen honestly. And it would
turn one deliberate act into a pile of work nobody captured, at the top of the one
surface `design-v3.md` 10.16 built specifically so that a pile never sits above the
thing a person opened the app to see.

**Considered and rejected: orphan, and say so in the delete copy.** Honest, but it
makes delete a bulk unfile, which means there are then two ways to empty an area and
one of them is spelled `delete`. Archive already exists as the non destructive path and
its copy already says what it does.

**Revisit if** anyone ever reports losing work to this. The undo window does not cover
an area delete, so the evidence would arrive as a person saying so rather than as a
metric, and there are no metrics.

### The first step truncates on the card and wraps in the sheet

**Decided.** One line, ellipsized, at `caption` on the area card and on an inbox row.
In full, wrapping, in the area detail sheet. The field itself has no length limit.

**Why.** `design-v3.md` 10.3 caps the card at four lines and names this the row that
truncates first when a status line is also present, because the status line is about
now and the first step is about next, and now wins on a card that has to pass a three
second test. The sheet has no such job and is where reading happens, so it wraps. The
field is unbounded because `MAX_ITEM_TITLE` exists to protect the line every surface
prints, and refusing a capture over a long second line would lose the thought this
whole path exists to keep.

**Considered and rejected: a character limit on the field**, which is the common
answer and would make both surfaces easy. It moves the cost to the wrong place: the
person typing, at the moment of capture, rather than the layout.

**Revisit if** a surface ever needs the first step in a fixed height slot. A widget
might, and phase 12 owns that.

### The estimate is a free number field, not a set of durations

**Decided.** Optional minutes, typed as digits, four at most. No chips, no stepper, no
presets. `design-v3.md` 10.17 says `entered as a number` and this is that.

**Why.** The statistically common answer is a row of duration chips: 15, 30, 60, and
so on. It loses twice. It is a decision with five options placed in the capture path,
which is what this phase exists to remove, and it silently makes the buckets somebody
else chose into the shape of the data. `MASTER_BUILD_PROMPT.md` 14b.8 has phase 8 read
these numbers as ratios and tendencies, and a ratio computed over five preset values is
a fact about the presets. Typing `20` is one gesture; picking the nearest chip to 20 is
a comparison.

**Also decided, and it is the harder half:** no surface counts down against the number,
anywhere. The card does not show it at all. The detail sheet shows it once, as plain
text, and no surface renders it beside an actual. An estimate that becomes a deadline
is a worse instrument than no estimate, and it is a deadline the person set for
themselves in a hopeful moment and then has to watch expire.

**Revisit if** phase 8's calibration facts turn out to need a coarser input to be
stable at low counts. That would be an argument for rounding what is read, not for
bucketing what is typed.

---

## August 27, 2026: a key is chosen against the whole space, not the visible part

**Decided.** Any order key is computed against **every entity that can occupy that
ordering space**, including the ones a filtered list leaves out, and excluding only
tombstoned ones that can never return. In the repository this is `tightenedBetween`,
and every path that mints a key goes through it or through `tailOrderKey`.

**Why.** A class of defect shipped in 0.2.0 and was found here by the replay harness
after the harness was taught to file items out of the inbox. Four separate paths had
it, and every one had the same shape: bounds were taken from the entities currently in
view rather than from the space.

The two spaces, and what each hides:

- **An area's items.** The active item holds a key in the same space as the queue but
  is not a member of the queue. `queueIn` therefore leaves it out, and at either end
  of the queue one bound is null and encloses everything. The plainest instance: the
  second item added to a fresh area took `OrderKey.first`, which is deterministic for
  a given jitter, and the first item was holding exactly that key already. It also
  cannot be assumed the active item sits below the whole queue, because promotion
  from the head leaves it there but a swap promotes whichever item the person chose.
- **Areas.** An archived area keeps its key. Archiving is reversible, so the key is
  not free, and unarchiving returns the area to the live list still holding it.

**What made it worth the hunt is the distance between cause and symptom.** A duplicate
key does nothing at the moment it is made. Both lists still render, the tie broken by
id, and no screen looks wrong. It surfaces much later as an `IllegalArgumentException`
out of `OrderKey.between`, on the first drag that asks for a key between the two of
them, in a session that did nothing unusual. Nothing about that crash points back at
the add, the swap or the unarchive that caused it.

The affected paths were `addItem`, `fileItem`, `reopenItem`, `moveItem`, `createArea`
and `moveArea`, plus the matching four in the test generator.

**Considered and rejected.**

- **Detecting the collision when a key is used and repairing it then.** Rejected: it
  puts the fix at the symptom, where the information about intended position has been
  lost, and a repair that guesses a position is a silent reordering of a person's
  queue.
- **Making `OrderKey.between` tolerant of equal bounds** by returning something
  plausible. Rejected for the same reason, and worse: it would have buried this defect
  permanently instead of surfacing it. The refusal is the feature.
- **Fixing only the paths the harness caught.** Rejected. Two of the six were found
  only after the first two were fixed, which is the usual sign that the class matters
  more than the instances.

**Revisit if** a third ordering space appears, or if an entity type gains a state that
holds a key while being filtered out of the list a key is computed from. Both are the
precondition for this defect, and neither is visible in the code that does the
computing. `OrderKeySpaceTest` is a property test over generated streams and is the
thing that would catch it; it asserts the property rather than the six paths.

---

## August 27, 2026: the language gate is widened, and what "user facing" means

**Decided.** Two things, both narrowing what the build will accept.

1. `verifyLanguageHygiene`'s British spelling list grows from 15 patterns to roughly
   130, built from three families: a curated `-ise` stem list, a `-yse` stem list, and
   a table of irregulars. It is curated rather than a blanket rule.
2. Em dashes and en dashes remain banned across every `.kt`, `.kts`, `.xml`, `.md` and
   `.pro` file in the repository. The owner's instruction was narrower, that they must
   never appear in anything user facing. The wider rule is kept, and the reason is
   recorded below so nobody relaxes it to match the letter of the instruction.

**Why.** The old list had a hole that a real document fell through.

A note on how this entry is written, because it is the first cost of the wider rule:
**the gate scans this file too, so the banned forms cannot be quoted here.** They are
named by description instead. A rule that applies to every file in the repository
cannot be documented inside that repository without this workaround, and that is worth
knowing before anyone tries to write the next entry about it.

The hole was this. The list matched the British form of "analyze" with a pattern
anchored by a word boundary, and that boundary cannot match inside the British form of
"paralyze", because the stem sits in the middle of the word rather than at its start.
So that word reached a committed document unnoticed and was caught by hand rather than
by the gate.

Widening the list immediately found two more British spellings that were already in
the repository and had passed every previous build: the past tense of "travel" in a
comment in `ui/components/Reorderable.kt`, and the gerund of "label" in the
transcribed addendum. The first had been sitting in shipped code since phase 2.

Three misses is enough evidence that a hand-picked list of fifteen was not a gate, it
was a sample.

On the second point: the reason to keep the dash ban wider than user facing text is
that nothing in this repository stays on one side of that line. A Trail row sentence
lives in `strings.xml`, but the sentence explaining why it is worded that way lives in
a KDoc, the rule behind it lives in a specification document, and the corpus lines the
engine will render live in a `.md` file that is not user facing today and is the
source of user facing text tomorrow. A rule that applies to some files is a rule
somebody has to think about before typing, and this project already asks a session to
hold a great deal in its head. A rule that applies to every file is one nobody has to
think about at all.

**Considered and rejected.**

- **A blanket `is(e|ed|ing)` rule** instead of a curated stem list. Rejected because a
  great many `-ise` words are correct in American English: advertise, exercise,
  surprise, compromise, franchise, improvise, supervise, televise, revise, devise,
  disguise, enterprise. A gate that fails the build on correct copy is a gate people
  argue with, and a gate people argue with gets switched off.
- **Narrowing the dash ban to user facing files only**, which is the literal reading
  of the instruction. Rejected for the reason above. The instruction is satisfied by a
  stricter rule, and where the two differ the stricter one costs nothing.
- **Adding `grey` to the list.** Still absent, still deliberately. The design system
  uses it, MASTER_BUILD_PROMPT 2.9 does not name it, and both spellings are current in
  American English.
- **Removing the `app/src/main/res/raw/**` exclusion**, which holds the two OFL license
  texts. Both are currently pure ASCII, so the exclusion hides nothing today. It stays
  because a third party license text is not ours to edit, and many of them legitimately
  contain a copyright sign or an accented author name. Failing the build on a file we
  are not permitted to change would be a gate with no correct response.

**Revisit if** a third party text lands in `res/raw` that is displayed to a person and
contains a character the rest of the app would be failed for. At that point the
licenses screen is showing something the gate would reject, and the honest answer is
to record that exception in this file rather than to widen the exclusion quietly.

---

## August 27, 2026: Addendum 01, executive function support

**Source:** `docs/addenda/ADDENDUM_01_EXECUTIVE_FUNCTION.md`, a directive from the
owner. That copy is provenance, not authority; it records what was asked for on one
date. What the project will build lives in MASTER_BUILD_PROMPT.md, design-v3.md, this
entry and the issue board.

**Decided.** Adopt the addendum in full. Its Step 2 schema changes land now, while no
user data exists. Its Step 3 platform first rule becomes standing and applies from
this date forward. Everything in Steps 4 through 10 is assigned to a phase and waits
for that phase. Nine conflicts with the existing specification were found. Eight were
resolved by the recording session; the ninth, C6, was put to the owner and decided by
them the same day, so all nine are settled below.

Nothing here is built yet. At the time of writing the project is at the end of phase
3, version 0.3.0, and every item in this entry is pending.

### Why this direction

The addendum came out of research and user panel work on serving people with
executive function challenges: ADHD, autism, brain fog from long COVID or ME/CFS,
cognitive changes in perimenopause, TBI recovery, depression and anxiety, and
burnout.

It states that provenance and does not cite sources or name a market size, so this
entry does not pretend to either. What it does give, and what is worth recording
because it is the checkable part, is a specific mechanism for each change and a reason
that mechanism bites for this audience. Those reasons are the argument, and they are
reproduced here in the addendum's own terms:

- **Widgets matter more than notifications, for a specific reason.** A widget is
  persistent and cannot be dismissed, so it works with out of sight, out of mind
  rather than against it. A notification is a one time event that gets swiped away and
  forgotten. A widget is still there tomorrow
- **Time blindness makes the session's visibility the point, not a nicety.** A focus
  session that exists only inside the app does not exist for someone who cannot feel
  the hour passing
- **The hardest moment is starting, and the title of a task is often the intimidating
  part of it.** Hence the first step field and the widget that shows it instead of the
  title
- **Every decision between the thought and the record is a place the thought is
  lost.** Hence capture that never requires choosing an area
- **Onboarding currently demands up to twelve decisions from people whose central
  difficulty is deciding.** Hence a genuine zero decision path
- **Predictability matters enormously to autistic users, and surprise interface
  behavior is a real cost.** Hence announcing Pulse before it first appears
- **Rejection sensitivity is common in this population, and an observation read as a
  verdict is how someone deletes the app.** Hence the tone pass across every rule that
  concerns a decline, a gap or a neglect
- **A fluctuating condition looks identical to decline in the data.** This one is a
  correctness fix and not politeness. Without a capacity check, the app will tell
  someone with a cyclical or relapsing condition that they are deteriorating, on a
  fixed schedule, forever, and it will be technically accurate every time
- **This audience leaves and returns, and that is normal use rather than failure.**
  Hence the re-entry state, and the rule that a returning user is never greeted by a
  measurement of their absence

Two of the addendum's conclusions are about where the evidence is thin rather than
where it is strong, and they are the reason to trust the rest: it declines body
doubling because the evidence is still thin despite the idea being popular, and it
declines a dyslexia friendly typeface on the same ground, putting the accessibility
budget into text size, spacing and contrast instead. A document that only ever
argued for adding things would be worth less.

### None of this is a rebuild

This is the part that matters most for a later session's judgment, and it is the
addendum's own first claim:

> **None of this is a rebuild.** The core mechanic is unchanged and is already well
> suited to this audience. These are additions and refinements.

A directive of this size arriving after three shipped phases reads like a pivot, and a
session that treats it as one will start rewriting things that are already correct.
It is not a pivot. Nothing in the addendum touches one active item per area,
everything else queued, promotion on completion, the event log as the single truth,
the six engine layers, the corpora, the visual system, or any of the four
non-negotiables in MASTER_BUILD_PROMPT.md 2.

What it changes is narrow and additive:

| what changes | what it was before |
|---|---|
| Capture no longer requires choosing an area | Capture required an area |
| Items gain two optional fields, an estimate and a first step | Title and note only |
| Presence gets a date only marker so a long gap can be detected | No marker existed |
| A focus session gains an extension and an honest early end | Neither existed |
| The reflective layer learns to tell a rhythm from a decline | Decline only |
| Six widgets in v1 rather than two | MASTER_BUILD_PROMPT.md 13.3, two |

Each of those sits at the edge of a mechanic that already does the thing this audience
needs, which is to show one item instead of a list of forty. The addendum closes with
its own guard on this point: **do not let this document pull the build out of order.**

**Revisit if:** never, as a framing. If a later session finds itself proposing a
change to the queue mechanic in the name of executive function support, the proposal
is outside this entry and needs its own argument.

### The four things deliberately ruled out

Addendum Step 9. Each of these is the kind of feature that any session reading the
audience description above will propose, which is exactly why the reason is recorded
and not just the no.

**9a. No body doubling, and no real time social presence of any kind.** It requires
networking, and the no `INTERNET` permission guarantee is the strongest claim this app
makes. That claim is checkable by a person in Android settings, it is checked by
`verifyNoInternetPermission` against the merged manifest on every build, and the whole
privacy section of the README rests on it. Body doubling is popular in the community
and the evidence for it is still thin. Trading a verifiable guarantee for a thin
evidence base is a bad trade at any price.
**Revisit if:** nothing short of abandoning the no network position, which would be a
different app rather than a feature.

**9b. No AI task breakdown.** It breaks two commitments at once: a hosted model needs
the socket the app does not have, and an on device model is still a model, which
MASTER_BUILD_PROMPT.md 2 rules out as a category rather than as a dependency. The
first step field is the deterministic version of the same idea, and it is arguably the
better one, because the user's own words for the first physical action are more use to
them than a generated decomposition of a task the app cannot see.
**Revisit if:** the owner drops the no AI commitment. That is a positioning decision
and not an engineering one, and it would invalidate several pages of the README.

**9c. No streaks, badges, XP, levels, confetti or celebration.** This was already the
rule. design-v3.md 14 says no streak breaking or shame mechanics, no confetti, no
premium badges. MASTER_BUILD_PROMPT.md 12.2 says of the Momentum activity row that
there is no streak and a missed day never resets anything. What the addendum adds is a
second and stronger reason: **streak loss is a documented abandonment trigger for this
audience.** The design position was about tone; this one is about whether the person
still has the app installed next month.

The consequence is already written into the widget family: the Rhythm widget renders
the fourteen day dot row and must never become a streak. No consecutive count, no fire
icon, no invitation not to break a chain. A gap is just a lighter dot.
**Revisit if:** never. This is now held up by two independent arguments, and losing one
would not move it.

**9d. No Live Update for anything but a focus session.** A focus session is a user
initiated, start to end, time bound task, which is the exact case Android's Live
Updates were designed for. Pulse, the Report, a reminder, and anything else the user
did not just start are none of those things. The addendum asks that this constraint be
written into design-v3.md rather than left implicit, so that a later session wanting to
extend the surface has to argue against a written rule instead of filling a silence.
It also sits directly on top of an existing rule in MASTER_BUILD_PROMPT.md 13.4: no
re-engagement notifications, ever, and nothing that exists to pull the user back.
**Revisit if:** never for the constraint. If the platform later offers a promoted
surface for something that is genuinely a user initiated ongoing task, that is a new
case and gets its own entry.

### The nine conflicts, and how each was resolved

The addendum's closing instruction:

> If any item here conflicts with something already in MASTER_SPEC.md, DESIGN.md or the
> corpora, do not silently pick a winner. Flag the conflict, propose a resolution, log
> it in DECISIONS.md, and continue.

Nine were found. The tiebreak used throughout, stated once so it is not re-argued nine
times: **the addendum is the owner's directive and post-dates every document it
contradicts, so where it plainly overrides an earlier scope line by the same author,
the later instruction wins and the earlier line is rewritten rather than left to rot.**
Where it instead collides with a shipped mechanism or with a number the engine reports
about a person, the shipped behavior is examined first, because a document can be
edited quietly and a wrong sentence shown to a person cannot be un-shown.

| id | subject | resolution | decided by |
|---|---|---|---|
| C1 | Document names | Read as aliases, rename nothing | recording session |
| C2 | The addendum fails the language gate | Normalized at transcription | transcription |
| C3 | Six items assigned to closed phases | New phase 3b, runs next | recording session |
| C4 | Two widgets in v1 versus six | Addendum supersedes, 13.3 and 18 rewritten | owner |
| C5 | App shortcuts out of scope | Addendum supersedes, struck from 18 | owner |
| C6 | The `FOCUS_ABANDONED` event name | Renamed to `FOCUS_ENDED_EARLY` | owner |
| C7 | `APP_OPENED` versus `activeDays` | Never counts as user activity | recording session |
| C8 | Nullable `areaId` versus the invariants | Unfiled sits outside area scope | recording session |
| C9 | Overlap with open issue #19 | One schema commit, not two | recording session |

#### C1. The documents the addendum names do not exist under those names

The addendum refers throughout to MASTER_SPEC.md, DESIGN.md, DECISIONS.md and
`tools/audit.py`. This repository has MASTER_BUILD_PROMPT.md, design-v3.md, no
decision log at all, and `audit.py` at the root.

**Resolution:** read them as aliases. MASTER_SPEC.md means MASTER_BUILD_PROMPT.md,
DESIGN.md means design-v3.md, `tools/audit.py` means `audit.py`, and DECISIONS.md is
this file, created by this entry. Nothing is renamed to match the addendum, because
the real names are cited from inside the source comments, the issue board and every
other document.

The alias predates the addendum, which is why it is worth a line rather than a
correction. Until August 27, 2026, `audit.py` carried DESIGN.md in a short whitelist of
known absent references inside its dangling file check, so something in this project's
history had been calling design-v3.md by that name for a while. That whitelist was
removed the same day in favor of asking whether a file exists anywhere in the
repository, which is the question the check was always trying to ask. The consequence
for this file: the alias may be written about, but never in the backticked form the
check reads as a reference to a real document.

This conflict is bookkeeping rather than substance, and is recorded only so that the
next session to read the addendum does not go looking for files that were never there.

**Revisit if:** a document is renamed. The alias line moves with it.

#### C2. The addendum's own text fails this project's language gate

The source document used em dashes throughout, and British spellings, including the
British form of color nine times and the British forms of behavior and paralyzing.
`verifyLanguageHygiene` fails the build on an em dash, an en dash, any character above
U+007F, or a British spelling, across every `.kt`, `.kts`, `.xml`, `.md` and `.pro`
file in the repository. Committing the document unedited would have broken the build
for every later session until someone found it.

**Resolution:** the stored copy was normalized at transcription. Em dashes were
replaced with commas, four spellings were Americanized, nothing else was changed and
no sentence was reworded. The file's own header records exactly that, names itself as
provenance rather than authority, and says the original is held outside the
repository.

The wider point, since this will happen again: **any document arriving from outside
this repository is subject to the gate the moment it is committed.** Normalize it at
the door, record what was changed in the file's header, and keep the original
elsewhere. Do not weaken the gate for an incoming file.

*Note at the time of writing:* the stored copy still contains the British form of
honored twice, in Step 8c. `audit.py` does not catch it because its British form list
is the shorter of the two; the Gradle gate does, because `BRITISH_FORMS` in
`build.gradle.kts` matches on that word. It is a live build breaker and belongs to
whoever owns that file. Strike this note once it is fixed.

**Revisit if:** never. The gate is not negotiable, per CLAUDE.md.

#### C3. Six items are assigned to phases that have already shipped

The addendum assigns 4a the unfiled inbox, 4b the first step field, 4c estimate
capture and 4d re-entry detection to phase 2, 8c calm mode to phase 1, and 8e the once
per session staggered entrance to phase 2. `docs/BUILD_STATE.md` records phases 1, 2
and 3 as done, at version 0.3.0, installed and verified on the device. Phase 4 is
next.

**Resolution:** insert **phase 3b, executive function retrofit**, running next, before
phase 4. It carries all six items plus the Step 2 schema changes, which the addendum
wants in the current phase regardless of where the matching interface lands.

Not reopening phases 1 and 2. A phase in this project is a closed issue with
acceptance criteria that were checked, a version number, and a build installed on a
physical device. Reopening one makes the record of what shipped and when unreadable,
and `docs/BUILD_STATE.md` is the document a cold start trusts most.

Running 3b before phase 4 is a dependency order rather than a preference. Phase 4 needs
`FOCUS_EXTENDED` for 4f, uses the `FOCUS_ENDED_EARLY` name C6 settled for 4e, is
governed by 8d on analog time for its ring, and cannot honor calm mode in its motion
if calm mode does not exist yet.

**Decided by:** the recording session. Pending. The phase 3b issue and the
MASTER_BUILD_PROMPT.md 19 build order edit are not part of this entry.

**Revisit if:** the owner would rather fold these six into phase 4 and accept one
larger phase. The dependency order survives either way; only the accounting changes.

#### C4. The widget count: two in v1, or six

MASTER_BUILD_PROMPT.md 13.3 is explicit: **v1 ships two**, Next Up and All Areas, with
four deferred and listed only so the snapshot format accommodates them, Area Focus,
Action Board, Weekly Momentum and Weekly Insight. It gives its reason plainly: "Glance
is finicky and six widgets is a lot of grinding for early payoff." Section 18 then puts
"The four deferred widgets" out of scope for v1.

Addendum Step 6 specifies eight and requires six in v1: Next Up, First Step, Quick
Capture, Focus Countdown, All Areas and Rhythm, with This Week and One Thing optional
if phase 12 has room.

**Resolution:** the addendum supersedes. 13.3 and 18 are rewritten. The four old
deferred names are superseded by the addendum's naming rather than kept alongside it,
because nothing in the new set maps cleanly onto Area Focus or Action Board, and
carrying two vocabularies for one surface is how a snapshot format ends up with dead
fields nobody dares remove.

The reason this is a supersession and not a coin toss: 13.3's argument was cost against
payoff, and **early payoff** was its operative phrase. The addendum's argument is that
for this audience the payoff is not early, it is central, and it says why, which 13.3
never had to: a widget is persistent and cannot be dismissed. That is a different claim
from "widgets are nice to have", and it beats a cost estimate.

The cost is real and is not waved away. Glance is still finicky, six widgets is still
grinding, and phase 12 additionally carries the Live Update, three app shortcuts and a
quick settings tile. Two of the eight are marked optional for exactly that reason, and
dropping them is the expected release valve rather than a failure.

**Decided by:** the owner, by issuing the addendum. The mechanics of the rewrite are
the recording session's, and are pending.

**Revisit if:** phase 12 runs long. The two optional widgets absorb it. If six in v1
still proves undeliverable after that, the shortfall is a conversation with the owner
and a new entry here, not a quiet reversion to 13.3.

#### C5. App shortcuts were out of scope

MASTER_BUILD_PROMPT.md 18 lists "Home screen shortcuts" among the things out of scope
for v1. Addendum 6i requires three static shortcuts in phase 12, quick capture, start
focus and today's Pulse, built on the androidx.core shortcuts APIs.

**Resolution:** the addendum supersedes and the line is struck from 18. The reasoning
is the same as the widgets, fewer steps between intention and action, and three static
shortcuts are a small piece of work next to the widget family, which is presumably why
18 could afford to drop them and can now afford to keep them.

One ambiguity worth naming rather than resolving silently: "Home screen shortcuts" in
18 may have meant pinned shortcuts placed on the launcher, which is a different feature
from the static long press menu the addendum asks for. The narrow reading is the one
struck. Pinned and dynamic shortcuts stay out of scope, and nothing currently asks for
them.

**Decided by:** the owner, by issuing the addendum. The edit to 18 is pending.

**Revisit if:** nothing. This is settled.

#### C6. The FOCUS_ABANDONED event name. DECIDED: rename to FOCUS_ENDED_EARLY

Addendum 4e: a focus session ended early is a completed short session, the completion
screen says so, and

> The word "abandoned" appears nowhere the user can see it, including in Trail. Rename
> the event if necessary.

**What is already true.** No user visible string in the app contains the word. The
Trail row for that event reads `Stopped after N minutes`. On a plain reading the
requirement is already met and no rename is needed. The word lives only in the event
type name `FOCUS_ABANDONED`, in `FocusCounts.abandoned` in
`domain/query/TrailFacts.kt`, in the payload, in the golden fixture and in the event
format document.

**Why that reading does not settle it.** Phase 11 export, which the addendum itself
specifies at 4h, writes the entire database, every event and all derived state, to one
portable file. It offers an optional password, and when no password is set the file is
readable, and the addendum requires the export screen to say so plainly rather than
imply safety. So the addendum creates a route by which the raw event type names are
user visible, and export is a safety feature people are meant to actually use. Anyone
who opens their own export file reads the word.

**The recommendation, stated and not taken:** rename to `FOCUS_ENDED_EARLY` now, while
the schema window is open. Step 2 exists precisely because a payload change is nearly
free before user data exists and painful afterward, and a type name is worse than a
payload field, because it appears in the log, the reducer, the query facade, the golden
fixture and the format document at once. Doing it now costs one commit that also
regenerates the golden fixture, which C9 says is happening regardless. Doing it later
costs a schema version and a reader that accepts both spellings, forever.

**The argument on the other side, recorded so the owner can weigh both:** internal
identifiers are not copy, and treating them as copy is how a codebase acquires a
euphemism layer. That argument holds right up until the export is readable, which is
the whole difficulty here.

**One point that cuts the same way as the recommendation:** the comment on
`FocusCounts` in `TrailFacts.kt` already argues that the log does not reliably know
that a session was abandoned. A killed process leaves a started session with no
terminal event, so `started != completed + abandoned` is a legal state rather than a
bug, and nothing anywhere is allowed to infer abandonment by subtraction.
`FOCUS_ENDED_EARLY` is the more accurate name on those grounds alone, independent of
what the user can see.

**Decided by the owner on August 27, 2026: rename it to `FOCUS_ENDED_EARLY`**, in the
same schema commit as the Step 2 changes and the issue #19 payload fields, while the
window is open.

**The owner's reason went past the export path**, which had been the whole of the
argument above, and it is the more durable one:

> `docs/EVENT_FORMAT.md` is a public contract that the future Linux desktop app will
> be built against in a separate session. The word teaches the wrong framing to the
> next implementer.

That reframes the question. The export file is a route by which one user might read
the word once. The format document is the specification a second implementation gets
built from, by someone with no other context, and every name in it is an instruction
about what the concept means. A type called `FOCUS_ABANDONED` invites the next
implementer to write a screen that says "abandoned", to add a rule that counts
abandonments, or to infer abandonment by subtraction, which the facade explicitly
forbids. The name is not internal in the way the counter-argument assumed, because it
crosses a process boundary into another codebase.

Scope of the rename: the event type, the payload class, `FocusOutcome.ABANDONED`,
`FocusCounts.abandoned`, the golden fixture, `docs/EVENT_FORMAT.md`,
`MASTER_BUILD_PROMPT.md` 5.2 and 10, and `design-v3.md`. No user visible string
changes, because none contained the word.

#### C7. APP_OPENED versus activeDays

This is the sharp one, and the only conflict in the nine that would have silently
corrupted a number the app states to a person.

**What the addendum adds.** Step 2d: a new `APP_OPENED` event recording the date key
only, no time and no count, written at most once per calendar day. It is the input to
4d, the re-entry state that appears when the app is opened after a gap of fourteen or
more days. The addendum's phrasing is that this is what makes gap detection possible
without any tracking, and that is right: a date key per day is the smallest possible
record that can answer the question.

**What the project already had.** Phase 3 shipped `ClarityEventType.isUserActivity` in
`domain/query/TrailFacts.kt`, and `TrailQueries.activeDays`, `totalEvents` and
`eventsPerArea` all count only events for which it is true. The predicate's own comment
states the reason it exists:

> PULSE_GENERATED, REPORT_GENERATED and PLAN_OFFERED arrive with no user gesture. If
> they counted, someone who opened the app daily and touched nothing would read as
> active on every day of the fortnight, and CLARITY_LOGIC_ENGINE.md 6.1's `quietDay`
> and `quietWeek` families could never fire at all.

**The defect that was one line away from shipping.** `isUserActivity` is written as a
negation: it is true unless the type is one of the three the engine authors itself. A
new event type is therefore user activity **by default**. Add `APP_OPENED` to the enum
without a second thought and merely opening the app marks the day active, at which
point the sentence in that comment stops being the hypothetical it was written to guard
against and becomes a literal description of the app's behavior.

Three numbers would then be wrong, and each of them would still look entirely
plausible:

- **`mo.steady` in CORPUS_3_MOMENTUM.md** fires on active on 9 or more of the last 14
  days and renders lines including `Active {dayCount} of the last fourteen days.`
  Someone who opened the app each morning and did nothing else for a fortnight would be
  told they had been steady
- **`ob.day.l03` in CORPUS_2_REPORT.md 2.13**, `{n} of seven days had activity.`, would
  become a count of app opens presented as a count of activity. The sentence would be
  false about the only subject it names
- **`quietDay` in CLARITY_LOGIC_ENGINE.md 6.1** fires on fewer than two events in the
  window. An `APP_OPENED` plus any single gesture clears that bar, so the family would
  be close to unreachable, and `quietWeek` with it

There is a second order harm that makes it worse. `mo.steady` and the day shape
observations are exactly the surfaces the addendum's own 4d exists to protect a
returning user from. An event added for the sole purpose of detecting an absence would
have become the mechanism that told a returning user they had been present.

**Resolution: `APP_OPENED` is excluded from `isUserActivity`.** It is a presence marker
for gap detection and is never counted as activity, anywhere, by anything. It joins
`PULSE_GENERATED`, `REPORT_GENERATED` and `PLAN_OFFERED` in being excluded.

One precision for whoever implements it. The current spelling of the predicate is
`isUserActivity = !isEngineAuthored`, and that equivalence stops being true here.
`APP_OPENED` is not engine authored; it is written by the app shell on launch. Folding
it into `isEngineAuthored` to get the right answer would record a false reason in the
name, and the comment on that predicate is the thing a later session actually reads.
The shape is the builder's call. The constraint is that the exclusion is exhaustive and
the reason is written down accurately.

**The standing rule that comes out of this, which is the more valuable half:** every
new event type is classified against `isUserActivity` deliberately, in the same commit
that adds it, with the classification argued in the comment. A default-inclusive
predicate is safe only while somebody is looking at it.

**Raised but not decided:** whether `APP_OPENED` renders a Trail row at all. A daily
"opened the app" line would be noise in a chronological log, and it would also be a
visible tally of presence, which 4d says never to show. That is a question for the
phase 3b issue, not a decision made here.

**Decided by:** the recording session, on the strength of the argument the shipped
comment in `TrailFacts.kt` had already made about the three engine authored types.
Pending implementation in phase 3b.

**Revisit if:** never, for the counting question. If some later feature genuinely needs
a count of app opens, it reads `APP_OPENED` directly, which is what the event is for.

#### C8. A nullable areaId versus the replay invariants

Addendum 2c gives items an unfiled state: `ITEM_ADDED`'s `areaId` becomes nullable, and
a new `ITEM_FILED` event moves an unfiled item into an area. 4a gives the reason,
capture must never require a decision, and sets the limit, an unfiled item can be
filed, edited or deleted but cannot be active or completed until filed.

MASTER_BUILD_PROMPT.md 6.2's invariants are written as though every item has an area:
at most one `ACTIVE` item per non-deleted, non-archived area, and every queued item in
an area has a distinct `orderKey`. `ClarityState.ItemState.areaId` and the Room
`ItemRow.areaId` are both non-null. The engine's facts are area scoped throughout, and
the Trail's `eventsPerArea` already drops an event whose area cannot be resolved rather
than bucketing it under a placeholder, because a placeholder would eventually be
printed and CLARITY_LOGIC_ENGINE.md 1 is blunt about what one fabricated area name
costs.

**Resolution:** an unfiled item sits outside every area scoped invariant and outside
every engine fact. It cannot be `ACTIVE` and cannot be `COMPLETED` until `ITEM_FILED`
gives it an area, which is the addendum's own rule rather than an addition to it. Room
migrates to schema 3. MASTER_BUILD_PROMPT.md 6.2 gains the qualifier in writing rather
than by implication, because an invariant that is silently conditional is an invariant
nobody can check.

**Considered and rejected: a synthetic inbox area.** A reserved area id holding every
unfiled item satisfies all four existing invariants with no nullable column, no
migration and no new state. It is the obvious answer, and design-v3.md 15 says the
obvious answer is not the answer unless it is genuinely best. It is not. A synthetic
area leaks into every place areas are enumerated: the Areas screen, the All Areas
widget, `eventsPerArea`, area count facts, the color palette, archive, delete, and the
area scoped engine families. Each of those becomes a special case that a later session
has to know about and one of them will eventually be missed, and the failure mode is a
fabricated area name in a rendered sentence. A null is one thing to remember, and the
type system raises it at every call site rather than waiting for a corpus line to
print it.

**Decided by:** the recording session. Pending. The schema half lands in the Step 2
commit and the interface half in phase 3b.

**Revisit if:** a second unfiled-like state ever appears. Two nullable states would
argue for a proper item location type rather than a null, and that would be a new
entry.

#### C9. Overlap with open issue #19

Issue #19, open and labeled `engine`, was raised during phase 3: the Pulse and Report
payloads cannot produce the `FiringHistory` the engine requires. `PulseGenerated`
carries no `subjectId`, `PulseAnswered` has the same gap, which disables
`selfReportVsData`, and `ReportSectionSnapshot` carries no family key, variant key,
stage, register or subject, so the variant exclusion, the family cooldown and
`hardStretch`'s six week limit are all underivable. Its stated urgency is word for word
the reason the addendum gives for Step 2: it is a change to the event format contract
and it is cheapest to settle before a real log exists.

**Resolution:** they land in one schema commit. Two commits mean two golden fixture
regenerations, and the event format document is explicit that a change to either
fixture belongs in a diff someone argues for rather than in a quiet commit. One argued
diff is better than two, and the second one always gets less scrutiny than the first.

Both open questions gate that commit and should be answered together: #19 still needs
the owner's decision on the exact fields, `subjectId` and probably `subjectKind` on the
Pulse events, and either the five fields on `ReportSectionSnapshot` or a parallel
rendered variant record list on `ReportGenerated`. C6 above was decided by the owner
on the event name. Both touch the same commit and the same fixture.

**Decided by:** the recording session, for the pairing only. The field shapes remain
the owner's call, per the issue.

**Revisit if:** #19 is answered in a way that raises `schemaVersion` and requires a
reader that accepts both shapes. At that point the ordering stops mattering and the two
can separate.

### Platform first, and where the per component records go

Addendum Step 3 makes Google's components and assets the default and custom the
fallback, in four steps: the official Material 3 Expressive component unmodified, then
themed with our tokens, then extended, then custom. Custom is named as a legitimate and
expected outcome rather than a failure. The reasons the platform comes first are stated
as accessibility, RTL, dynamic type, predictive back and motion physics arriving
correct for free, hardware acceleration and testing at a scale one person cannot match,
and a codebase small enough for one person to maintain.

**The rule requires a record either way**, which is the part that concerns this file.
When a component goes custom, note what was checked and which of four reasons applied:

| # | reason to go custom |
|---|---|
| 1 | No platform equivalent exists |
| 2 | The platform component carries meaning the app rejects: achievement, scoring, celebration, or progress toward a target |
| 3 | The platform component fights a design rule: two separation devices on one element, a colored edge treatment, an all caps label |
| 4 | The platform component is worse for this audience: motion or saturation that theming cannot tame |

And what is explicitly **not** a reason: preferring the look of something you would
draw yourself. That instinct belongs in the polish pass at 3c, which works through
theming rather than replacement.

**Decided: those records go in the standing register at the foot of this file, one row
each, append only.** Not as dated entries, because they accumulate a line at a time
across a dozen phases and would bury every substantive entry between them. Not in
design-v3.md, because that document says what a component is and what its dimensions
are, not what was checked before someone built it. The register is the file's one
exception to reverse chronology and is named as such in the header above.

Two things the register is not. It is not a discouragement: the addendum says plainly
that reaching the custom conclusion for a given component is the rule working, not a
conflict, and the register exists so a later session does not redo an analysis someone
already did. And it is not free: a custom component inherits the platform's
obligations, accessibility, RTL, dynamic type, reduce motion and calm mode, to the
same standard the platform component would have met. That inheritance is the real cost
of going custom and it is why the platform comes first when it fits.

The companion rule at 3d, verify current library versions at build time rather than
trusting any version named in any document, is a build practice rather than a decision.
It belongs with the phase that integrates the library.

**Revisit if:** a Material release ships a genuine equivalent for something in the
register. Every row is dated so that this can be checked rather than assumed.

### Positioning, and why the wording has legal weight

Addendum Step 10, recorded now, applied in phase 13.

**The distinction the whole section turns on: Google Play's health policy triggers on
claims, not on keywords.** Using the words people actually search for is permitted.
Stating that the app does something clinical is not. These are two different acts and
the policy treats them differently, which is what makes it possible to reach this
audience honestly without making a single medical claim.

**Permitted and encouraged** in the long description and the keywords: ADHD, autism,
executive function, executive dysfunction, task paralysis, time blindness, brain fog,
neurodivergent, overwhelm, procrastination, focus, one thing at a time. The addendum
gives the register it wants: "Built for people who find long lists paralyzing. Designed
with ADHD, autism and brain fog in mind."

**Forbidden**, verbatim from the addendum, anywhere in the listing, the app copy or any
marketing: treats, manages, cures, therapy, therapeutic, clinically proven, medically,
symptoms, diagnosis, disorder used as a claim, and any statement that the app improves
or reduces anything clinical.

**Required**, in the store listing and in About, per MASTER_BUILD_PROMPT.md 14.4:

> Clarity Now is a productivity tool. It does not provide medical advice, diagnosis or
> treatment.

Google Play's Health Apps Declaration must be completed. With zero data collection, no
accounts, no health data access and no network permission, this app should certify
cleanly, and the source being public means every one of those claims is checkable
rather than asserted.

**The part of this that ages, and the reason it is recorded as a decision rather than
as a fact:** the current Play Console requirements must be read at the time of listing
rather than trusted from any document, including this one. The policy changed through
2025 and adds medical device labeling in January 2026. An entry dated August 2026 is
not evidence about a form submitted in 2027.

**Revisit if:** the Play health policy changes again, which this entry assumes it will.
The permitted and forbidden lists above are the addendum's, and a change to either is
an owner decision and a new entry.

### Revisit if, for the direction as a whole

There is no measurement that could reopen this, and that is deliberate rather than an
oversight: the app collects nothing, reports nothing and will never have telemetry, so
there is no number that could arrive later and argue against the direction. What could
reopen it is the owner deciding the audience framing is wrong, or a specific addition
proving genuinely unbuildable in this stack. The addendum asks that the second be said
plainly before starting rather than discovered halfway through a half-built feature,
and that instruction is adopted here: an item that cannot be built is raised as an
issue and an entry, not quietly dropped.

Each conflict above carries its own revisit condition. C6 is not a revisit condition,
it is an open question, and it is the owner's.

---

## August 28, 2026: layer 6 ships, and the reservation in section 19 is not exercised

`MASTER_BUILD_PROMPT.md` 19 registers a formal reservation against the follow through:
it is the first thing removed if it reads as supervision when tested, and it says
**removed rather than tuned**. This entry is the test being run and the answer being
given, so that a later session can see the evidence rather than the verdict.

### The four readings, taken over a forced run

An up to date `verifyClarity` returns green in 541 milliseconds and establishes nothing,
because the three corpus files are read at runtime and Gradle does not track them as
task inputs. Every number here is from `--rerun-tasks`: 44 of 44 tasks executed,
**1,076 tests, 0 failures, 0 errors**, one deliberate skip.

| reading | result |
|---|---|
| the non compliance test | **pass.** 1,441 invocations of `acceptsEveryPlan`, 0 referencing a plan, a commitment, an intention or a failure to act |
| layer 6 silence | **pass.** 185 of 451 reports carried no plan, 41 percent, against a floor of 15 |
| cue substantiation | **pass.** No plan renders with a cue below threshold across ten thousand generated fact sets |
| no imperative anywhere | **pass.** Asserted over the frame, cue, action and commitment benches themselves, not over the sentences they happened to produce |

The second one had never been measurable. It is the last of the ten checks in
`CLARITY_LOGIC_ENGINE.md` 12 to become so, and it is enforced from this phase.

### The decision: the reservation is not exercised

**A count cannot answer the question section 19 asks**, which is whether the reports
around an accepted plan feel different in a way a person would read as being watched.
So the year was measured against itself. `Selector.FOLLOW_THROUGH_BOOST` was set to 0,
the eleven persona years were re-run, the two dumps were diffed, and the constant was
restored and the file checksummed back to byte identity.

**The boost changes 118 lines, all of them report observations, on 27 of 52 weeks.**
Nothing on the Pulse, Momentum or the Areas banner, because every caller outside the
Report omits the argument. On 51 of the 52 weeks it changed only the order of four true
observations. A reader has no access to the counterfactual, so on those weeks there is
nothing available to notice, and nothing in any of them names a plan.

**The judgment is that it does not read as supervision, and the reservation stands
unexercised rather than withdrawn.** It stays cheap to exercise: one file, one
parameter and one integer, and `Selector` compiles with `domain/guidance/` deleted.

### The one week that is not order, and what it actually is

On 2026-09-06, the only `hardStretch` week in that persona's year:

- **Without the boost:** the report says `The stretch is three weeks old.` and layer 6
  returns `Nothing`. That is 10.4 rule 6 working exactly as written
- **With the boost:** `queuePressure` and `hardStretch` sit at equal specificity and
  equal priority. The boost breaks the tie that rule key order would have given to
  `hardStretch`, which falls outside `MAX_OBSERVATIONS`. `heavy` reads off what
  appeared rather than off what qualified, so **the heaviest week of the year stops
  looking heavy and gets a closing line**

An accepted plan can therefore delete the one family `CORPUS_2_REPORT.md` 2.21 exists
for, and turn a deliberate silence into a sentence.

**It is a report composer property rather than a layer 6 one, and that was measured
rather than argued.** `Selector.selectObservations` was temporarily instrumented to
report every week where `hardStretch` qualified and was crowded out, the eleven years
were re-run, and the patch was reverted. It happens **twice in eleven simulated years,
once with a non empty boost set and once with an empty one.** A cap of four and a tie
broken on rule key can drop `hardStretch` with no plan anywhere near it.

**Recorded and not fixed, which is the part that is a decision.** The obvious fix is to
make `heavy` read `hardStretch` off the qualified set rather than off the kept four.
That loses, for now, because 10.4 argues the opposite reading deliberately and in
writing: "a report that was heavy and did not say so is not treated as heavy, the rule
is about the page the person is reading, not about the facts behind it." Overturning a
stated reading of a specification section is the owner's call, the remedy touches every
week rather than the two, and the simpler and more reversible option is to leave the
behavior alone and write the finding down. **Open question, recommendation stated and
not taken: protect `hardStretch` from displacement, by exempting it from the cap rather
than by changing rule 6.** That keeps 10.4's reading intact and removes both cases.

### Two things a reader of the Trail can see, and one of them is new this phase

`PLAN_OFFERED` and `PLAN_ACCEPTED` render Trail rows, `One thing was suggested` and
`Accepted one thing`. Declining writes no event, so an offer a person left shows as a
row with no acceptance beside it, and a person scrolling their Trail can therefore tell
which offers they took up. 10.5 says the plan exists in the report and nowhere else.

**It is left alone, and the reason is that it is exactly the Pulse's shape.**
`PULSE_GENERATED` and `PULSE_ANSWERED` have rendered the same asymmetric pair since
phase 6 and nobody has read it as a compliance record. Both rows quote nothing and name
nothing, `TrailRow` gives both an empty `TrailRowContent`, and the Trail is a log
renderer whose job is to show what happened. The precedent that would argue the other
way is `APP_OPENED`, which renders no row because a log of when somebody was present is
a measurement of absence turned inside out. **Whether an unanswered offer is closer to
an unanswered Pulse or to a presence marker is a judgment about the person's own
reading, and it is the owner's.** Recorded as open, unchanged, and outside the engine
either way.

### One recording error corrected

Section 12's prose read "Eight measurements exist and the eighth is the current one"
for two passes after the ninth column was already in the table below it. A count kept
in prose beside a table that grows goes stale silently, and it did. It now reads ten
and names all ten. 10.7's own figure was likewise still the 43 percent layer 6 measured
before the guidance language pass revived two cues; it reads 41 now, which is the
figure taken over all three lanes together.

---

## August 28, 2026: one catalog for the process, and the four choices inside that

Issue #55. `PulseCoordinator`, `MomentumCoordinator` and `ReportCoordinator` each held a
`Mutex`, a cached catalog and a failure field that were character for character the same
code, so the three corpus volumes were read and parsed three times per process.

**None of the three builders was wrong.** `ClarityGraph` is the file where the three would
have met and it was outside every surface phase's file list, so each phase did the correct
local thing and the duplication existed only between them. All three found it, all three
left a note at their own construction site, and all three named this fix. What is recorded
below is only what none of them could reach.

### The holder is in a new package, `domain.corpus`, and not beside the seam it uses

**Decided.** `CorpusText`, `CorpusSource` and the new `SharedCatalog` are in
`domain/corpus/`. The platform half, the class that opens the three packaged assets, is a
private class at the foot of `di/ClarityGraph.kt`.

**The two losing options.** The seam was in `domain/pulse/PulseCoordinator.kt`, so the
smallest change was to put the holder there beside it; Momentum and the Report already
imported `CorpusSource` from `domain.pulse` and would have imported the holder from there
too. That reads as though reaching the corpus were a Pulse idea two other surfaces had
borrowed, and it is nobody's idea in particular. The other option was `di`, next to the one
binding that builds it. `di` imports Android, `domain.momentum` may not, and a coordinator
in `domain` taking a type from `di` inverts the layering for the convenience of one file.

**What decides it.** The three callers are in `domain.pulse`, `domain.momentum` and
`ui.report`, and the type has to be reachable from all three without an Android import
landing in `domain`. A pure package below all three is the only shape that is true of every
caller. `domain.corpus` is now in `DomainPurityTest`, which is what makes that a check
rather than a claim.

### The load returns one value, and not a nullable catalog beside a failure field

**Decided.** `SharedCatalog.load()` returns `CatalogLoad.Ready` or `CatalogLoad.Failed`.

**The losing option is the obvious one and it was nearly taken:** keep each coordinator's
shape exactly and share it, one nullable catalog and one failure string. It reads as a
faithful extraction, and it carries a race that three separate holders made almost
impossible and one shared holder would make ordinary. Every caller reads twice, the catalog
and then the reason. Between those two reads another surface can succeed and clear the
reason, and the first caller then renders nothing while reporting that nothing is wrong.
That failure is silent, it is on the path a person hits when the corpus is missing, and it
is exactly the path nobody exercises. One value cannot be half read.

**The property that had to survive the merge** is the one all three coordinators state in
their own words: the failure is held rather than thrown, because the caller wants to render
everything that does not need language. It survives. Nothing throws, the Pulse still reports
its reason, and Momentum and the Report still draw every number they counted from the log.

### A mutex, not a lazy delegate and not a double checked field

**Decided.** One `Mutex`, held across the read and the parse together, with no fast path
outside it.

**Why not `by lazy`.** Reading the corpus suspends and a lazy delegate takes a blocking
initializer. The spelling that compiles wraps `runBlocking`, which blocks whichever thread
arrives first, and on a cold start that is the thread drawing the first frame.

**Why no double checked read outside the lock.** It is the standard optimization and it buys
nothing here: an uncontended `withLock` is a compare and swap with no suspension, and the
callers ask a handful of times per screen. What it costs is that the correctness of the
class stops depending on the lock and starts depending on the memory model.

**Two surfaces asking at once is the ordinary case rather than the exotic one.** A cold start
generates the Pulse on the foreground callback while the shell is already building the tab
the person left the app on. `SharedCatalogTest` runs eight callers into it and asserts one
read between them; without the lock that test reads eight times.

### The catalog is held and the failure is not

**Decided.** A successful parse is cached for the life of the process. A failure is reported
and forgotten, so the next ask reads again.

**The losing option** is to cache the failure too, on the argument that a missing asset will
still be missing in four seconds. It is true and it is the wrong trade: the cheap failure to
recover from is a transient one, and the expensive one is a broken build, which a retry
cannot make worse and which costs one failed open per ask.

### One finding, which is a defect this issue did not set out to fix

**The parse was running on the main thread and three separate notes said it was not.**
`CorpusSource` puts the file read on an IO dispatcher and `withContext` returns to the
caller, so the parse itself ran wherever the caller stood. All three surfaces reach it from a
`viewModelScope`, which is the main dispatcher. Every one of the three notes, and the issue
written from them, described the cost as being on a background dispatcher; the read was, and
the parse was not. `SharedCatalog` dispatches the parse to `Dispatchers.Default`, so the
claim those notes made is now true of the code.

**It is recorded rather than folded in quietly** because it is the reason the issue mattered
more than it read: three parses of a corpus that grew from 1,503 authored lines to roughly
4,800 were three chances at a visible stall on the main thread, on surfaces meant to feel
calm, and not three cheap background parses.

### One correction to CLAUDE.md

Rule 5 listed `domain.engine`, `domain.guidance` and `domain.replay` as the pure packages.
`DomainPurityTest` has scanned `domain.query` as well since it was written, so the rule
understated itself by one package before this change and by two after it. It now names all
five and says that the test's list is the one that counts.

---

## Standing register: platform first records

Append only, oldest first, one row per component built by hand rather than taken from
the platform. The four reasons are numbered in the platform first section of the
August 27, 2026 entry above. A row is added in the same commit that builds the
component, and its state is updated when that component ships.

This register exists so that a later session can see the platform was considered and
does not have to redo the analysis. It is not a list of exceptions to apologize for.

| date | component | reason | state |
|---|---|---|---|
| August 27, 2026 | Depleting focus ring | 1, no platform equivalent exists | built, phase 4 |
| August 27, 2026 | Week ribbon, in the Report | 1, no platform equivalent exists | pending, phase 8 |
| August 27, 2026 | Fourteen day rhythm dot row | 1, no platform equivalent exists | built, phase 6, in the Pulse; Momentum's own row is phase 7 |
| August 27, 2026 | Area color wash | 1, no platform equivalent exists | built, phase 2 |
| August 27, 2026 | Two stage color picker | 1, no platform equivalent exists | built, phase 2 |
| August 27, 2026 | Tutorial spotlight | 1, no platform equivalent exists | pending, phase 10 |

All six are named in Addendum 01 Step 3a as components with no platform equivalent,
with the instruction not to contort a platform component into their shape. Two of them
were already built by hand in phase 2, before the rule existed, and are recorded here
so the register is complete rather than only forward looking.
