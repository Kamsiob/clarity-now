# Corpus anchors

Ten already approved lines per hot family, to be read before writing a batch and again
before every third one. `CLARITY_LOGIC_ENGINE.md` 11.2 step 3.

## Why this file exists

Phase 9 writes roughly two thousand lines into a corpus of fifteen hundred, across eight
sessions, and the owner reads the result once, at the end, as one annotated file. Nothing
between those two points can catch voice drift. The mechanical gates in
`app/src/test/java/com/kamsiob/claritynow/domain/engine/corpus/` catch a banned word, a
shared clause, a repeated shape, a flat bench and a line that cannot render. **None of them
can tell that line four hundred sounds like a different writer than line forty.** That is
what these are for.

11.2 step 6 states the failure directly: voice drifts noticeably by sentence 200. The drift
is not toward anything bad in particular. It is toward the average of everything the writer
has ever read, which is exactly the texture the whole project exists to avoid.

## How to use them

1. Before a batch of forty, read the ten anchors for that family. Out loud is better.
2. Write the batch.
3. Read the anchors again, then read your own forty against them. **Cut the ones that do not
   belong beside the anchors**, and expect that to be a quarter to a third of what you
   wrote. A cut rate near zero means the second reading did not happen.
4. Before every third batch, read the anchors for the family again from the top. Not the
   batch, the anchors.

## How these were chosen

Every anchor is a line already in the corpus, chosen to span the registers its volume uses
and the length bands its surface can reach, and, where the family has an escalation ladder,
to span the ladder. **Nothing here is new writing.** The right hand column says what about
the voice that line carries, which is the only reason to prefer one anchor to another.

**An anchor has to be a line the engine can actually say.** Two lines in the corpus were
never fillable from any real moment eleven simulated years produced, and a further hundred
and eighteen are held out of their benches by `SlotBindings.EXCLUDED`. None of the hundred
and twenty is an anchor. Anchoring on a line that never reaches a screen would teach the
voice of something nobody reads.

**Two anchors moved when the binding pass retired their lines**, and both are worth a
sentence here because they are the shape a future anchor is most likely to be wrong in.
`ob.pers.e02`, *It is the longest anything has been active in {areaName}*, and `ob.tod.e04`,
*Your longest focus sessions were the early ones*, were both superlatives about a quantity
their family's rule never establishes: `longestEverActiveDays` is across the whole app
rather than one area, and no fact anywhere carries the length of a focus session. They read
beautifully, they rendered, they were being said, and neither corpus gate could see them
because a false claim in words carries no marker. An anchor teaches the voice of the line it
quotes, so an anchor that quotes a claim the app cannot support teaches exactly that. **That rule left nineteen
of these families short of ten anchors when phase 9 opened, six when volume 2 was
half grown, and none now.** All thirty six carry ten, which is 360 rows, because a bench
grown to sixty lines can supply ten sayable ones even when a handful of its markers are
unbound. **The last one to get there was `weekQuiet`**, which joined the hot tier when the
register pass gave it a voice it could speak in, carried its whole bench of eight for one
pass because eight was all it had, and now carries sixty. Where a heading still names a
sayable count below the line count, that gap is a finding about the bench rather than a gap
in this file.

`CorpusAnchorsTest` checks every row on every run: the key exists, the sentence matches the
corpus character for character, the register and band are the ones the catalog computes, the
line is renderable, every hot family is covered and no other family is, and each family
carries ten anchors or every sayable line it has. A misquoted anchor teaches a sentence
nobody approved, and nothing else in the build reads this file.

The hot families are the thirty six benches `HotFamilies` records at forty firings a year or
more, measured over eleven personas and a simulated year each. Warm and long tail benches
have no anchors here because phase 9 does not touch them.

## What the anchors are protecting

Read across the whole file and five habits repeat. They are the voice.

- **The fact is the subject, and the person is rarely the object of a verb.** `The queue
  behind {itemTitle} has not moved`, not `you have not moved it`.
- **Two beats beat one long sentence.** `Still {itemTitle}. {ageDays} now.` The second beat
  is usually shorter than the first and never explains the first.
- **A number is stated and never interpreted.** `{n} added, {m} finished.` Nothing says
  whether that is good.
- **Where a reading is offered, both readings are offered.** `That is either a season or a
  slide.` The line names two and picks neither.
- **The reach happens once per family and is short.** One line in persistence says an item
  becomes furniture. The other sixty do not reach at all.

---

## Pulse

Three registers: `[P]` plain, `[O]` observational, `[R]` reflective. Every Pulse statement
is about yesterday, and every one of them combines with a question and a response pair from
its own stage.

### persistence, 482 firings a year, 276 statements today

| key | tag | band | line | what the voice carries |
|---|---|---|---|---|
| `persistence.s1.02` | P | SHORT | `{ageDays} on {itemTitle}.` | A duration and a name, no verb at all. The shortest true thing the family can say. |
| `persistence.s1.05` | O | SHORT | `Nothing has moved past {itemTitle} yet.` | The absence is the subject. `yet` carries no pressure because nothing follows it. |
| `persistence.s1.11` | R | MEDIUM | `{areaName} has asked the same thing of you for {ageDays}.` | The area is the one doing the asking. The person is the object of the sentence, never its culprit. |
| `persistence.s1.14` | O | MEDIUM | `Whatever else moved this week, {itemTitle} did not.` | Concession, then bare fact. The contrast does all the work and no adjective is spent. |
| `persistence.s2.01` | P | SHORT | `Still {itemTitle}. {ageDays} now.` | The house rhythm at its tightest: two beats, no verb in either. |
| `persistence.s2.10` | O | MEDIUM | `Other areas have moved. {areaName} has not.` | Two flat clauses side by side. The comparison is left entirely to the reader. |
| `persistence.s2.15` | R | MEDIUM | `{ageDays} is long enough that it is worth naming.` | The reflective register naming its own act rather than passing a judgment. |
| `persistence.s3.10` | R | MEDIUM | `{ageDays}. {itemTitle} is no longer a task, it is a state.` | A change of category, not a verdict. Nothing here says the state is wrong. |
| `persistence.s3.13` | R | MEDIUM | `Three weeks is long enough for something to stop being a decision.` | Generalizes without addressing the reader. The word `you` is absent on purpose. |
| `persistence.s4.07` | R | MEDIUM | `{ageDays}. At some point an item stops being a task and becomes furniture.` | The one reach the family allows itself, at the top of the ladder, and still no instruction. |

### quietDay, 283 firings a year, 198 statements today

| key | tag | band | line | what the voice carries |
|---|---|---|---|---|
| `quietday.s1.02` | P | SHORT | `Nothing moved yesterday.` | Three words. No consolation offered and no alarm raised. |
| `quietday.s1.07` | O | SHORT | `Everything is where you left it.` | A quiet day framed as continuity rather than as a loss. |
| `quietday.s1.08` | R | SHORT | `A still day.` | A noun phrase with no verb. The reflective register at its most restrained. |
| `quietday.s1.09` | R | MEDIUM | `Yesterday did not leave a mark here.` | The day is the subject and the verb is gentle. Nothing was missed, nothing was owed. |
| `quietday.s1.10` | R | MEDIUM | `Nothing to report from yesterday, which is its own report.` | The one line that comments on the app's own silence, and it does so lightly. |
| `quietday.s2.03` | O | MEDIUM | `Your areas have been still for a few days.` | `a few` where a precise number would read as counting against somebody. |
| `quietday.s2.09` | R | SHORT | `The app has been waiting.` | The app is the subject. It waits; it does not expect. |
| `quietday.s3.04` | O | MEDIUM | `Everything is where it was almost a week ago.` | `almost` at the stage where an exact number would begin to sting. |
| `quietday.s3.05` | R | SHORT | `A long still stretch.` | Escalation carried by one adjective and by nothing in the tone. |
| `quietday.s3.10` | R | MEDIUM | `Sometimes a quiet week is the right week.` | The furthest this corpus goes toward reassurance, and it is stated as a general truth rather than about this person. |

### concentration, 246 firings a year, 199 statements today

| key | tag | band | line | what the voice carries |
|---|---|---|---|---|
| `concentration.s1.05` | O | SHORT | `Attention leaned toward {areaName}.` | `leaned` is a verb with no force in it. Nothing was pulled or dragged. |
| `concentration.s1.09` | R | MEDIUM | `Yesterday had a center of gravity, and it was {areaName}.` | One figure of speech, used once in the family. |
| `concentration.s1.14` | O | MEDIUM | `{areaName} was busy. The rest was not.` | Two beats, the second a bare negation. |
| `concentration.s2.09` | R | MEDIUM | `Yesterday was not a balanced day, and it may not have needed to be.` | The longest line in the family and the clearest refusal to grade. |
| `concentration.s2.11` | R | SHORT | `The day belonged to one area.` | Possession as the image, never domination. |
| `concentration.s2.13` | P | SHORT | `Only {m} things happened outside {areaName}.` | `only` is the single intensifier the family permits, and it sits on a plain line. |
| `concentration.s3.01` | P | SHORT | `Everything yesterday was {areaName}.` | An absolute with no hedge, because at this stage the fact is absolute. |
| `concentration.s3.08` | R | MEDIUM | `{areaName} has been everything for a while now.` | The escalation is carried by `everything` and by `a while`, and by nothing sharper than that. |
| `concentration.s3.09` | R | MEDIUM | `Several days in a row have all been the same area.` | A run, stated without the banned word for a run. |
| `concentration.s3.12` | R | MEDIUM | `This has stopped being a busy day and become a pattern.` | Escalation as a change of category rather than a change of temperature. |

### accumulation, 163 firings a year, 199 statements today

| key | tag | band | line | what the voice carries |
|---|---|---|---|---|
| `accumulation.s1.03` | P | SHORT | `{n} added, {m} finished.` | Two numbers, no verb, and no comparison drawn between them. |
| `accumulation.s1.05` | O | SHORT | `Intake ran ahead of output.` | Abstract nouns keep the person out of the sentence entirely. |
| `accumulation.s1.09` | R | MEDIUM | `Yesterday was better at noticing than finishing.` | The day is the subject, and both halves of the contrast are competences. |
| `accumulation.s1.10` | R | SHORT | `The list grew.` | Three words for the whole fact. Nothing about growth is called a problem. |
| `accumulation.s2.06` | O | SHORT | `Yesterday was mostly about noticing things.` | Capture named as an activity with worth, which is the family's whole posture. |
| `accumulation.s2.11` | R | SHORT | `A lot arrived. Not much left.` | Two beats, both agentless. |
| `accumulation.s3.03` | O | MEDIUM | `The gap between what goes in and what comes out is widening.` | The widest claim in the family, made about a gap rather than about a person. |
| `accumulation.s3.07` | R | MEDIUM | `At some point a queue stops being a plan.` | A general statement, offered rather than applied to this week. |
| `accumulation.s3.08` | R | MEDIUM | `Things keep arriving faster than they leave.` | Things are the agents. Nobody let them in. |
| `accumulation.s3.10` | O | MEDIUM | `{areaName} is holding {n} things that have not moved.` | The area holds. Nothing suggests somebody is holding it back. |

### rebalance, 100 firings a year, 136 statements today

| key | tag | band | line | what the voice carries |
|---|---|---|---|---|
| `rebalance.s1.01` | P | SHORT | `{areaName} moved again after {ageDays}.` | The return stated as a fact with a duration attached, and nothing else. |
| `rebalance.s1.03` | O | SHORT | `{areaName} came back yesterday.` | Four words. The family never celebrates a return. |
| `rebalance.s1.04` | O | SHORT | `After {ageDays} of stillness, {areaName} moved.` | The gap is called stillness, which is neutral, and never neglect. |
| `rebalance.s1.06` | R | SHORT | `{areaName} woke up.` | The one personification in the family, and it is three words long. |
| `rebalance.s1.07` | R | MEDIUM | `Something returned to {areaName} after a gap.` | `something` rather than a name, when the name would add nothing. |
| `rebalance.s1.08` | P | MEDIUM | `{ageDays} of quiet in {areaName}, then yesterday.` | A duration, a place, a pivot. The pivot is the whole sentence. |
| `rebalance.s2.05` | R | SHORT | `{areaName} has been away a while.` | `a while` where the number is already in the reader's head. |
| `rebalance.s2.06` | R | MEDIUM | `Something came back that had been gone a long time.` | No name, no number, no area. The most abstract line the family has. |
| `rebalance.s2.07` | P | SHORT | `First activity in {areaName} in {ageDays}.` | Six words, and `first` does the escalating that an adjective would otherwise have to. |
| `rebalance.s2.08` | O | MEDIUM | `{areaName} was almost forgotten. Yesterday it was not.` | Two beats where the second cancels the first. The nearest this family gets to warmth. |

### freshStart, 42 firings a year, 10 statements today

New to the hot tier at the ninth measurement, at 42 firings against 26 at the eighth. The
whole bench is listed because the whole bench is ten lines: this family was sized for the
long tail and the recency bound on the Pulse repeat filter moved it over the line. It is a
bench debt rather than an anchor set, and the ten below are what the voice is today rather
than what it should be grown into.

| key | tag | band | line | what the voice carries |
|---|---|---|---|---|
| `freshstart.s1.01` | P | SHORT | `{areaName} has its first item.` | The area is the subject and the person is absent from the sentence. |
| `freshstart.s1.02` | P | SHORT | `You added {areaName}.` | The one line in the family that addresses the reader, and it does nothing but state what they did. |
| `freshstart.s1.03` | P | MEDIUM | `{itemTitle} is the first thing in {areaName}.` | Two names and an ordinal. No adjective anywhere near a beginning. |
| `freshstart.s1.04` | O | MEDIUM | `There is somewhere new to put things now.` | Describes what the person gained without congratulating them for it. |
| `freshstart.s1.05` | O | SHORT | `{areaName} started yesterday.` | Three words and a date word. A beginning stated as flatly as a quiet day is. |
| `freshstart.s1.06` | O | MEDIUM | `A new area, with one thing in it.` | A noun phrase and a subordinate clause, no verb between them. The house rhythm at a beginning. |
| `freshstart.s1.07` | R | SHORT | `{areaName} exists now.` | Three words. `exists` is the plainest verb available and the family takes it. |
| `freshstart.s1.08` | R | MEDIUM | `Something new got a place of its own.` | `something` rather than a name, and the thing is the one doing the getting. |
| `freshstart.s1.09` | P | SHORT | `{areaName} went from empty to active.` | A transition named by its two ends, with no word for the crossing. |
| `freshstart.s1.10` | O | MEDIUM | `{areaName} has one thing and nothing behind it.` | States the queue depth at a beginning, where every later family would read it as pressure. |

---

## Report

Four registers: `[P]` plain, `[O]` observational, `[E]` editorial, `[N]` neutral agent.
Reflective belongs to the Pulse and Momentum and is not authorable here. Editorial is
budgeted at two leads per report, and the neutral agent register is reached only through a
rule the engine marks unflattering, where the fact becomes the grammatical subject instead
of the person.

**A headline is capped at seven words by validator check 9**, which is why every headline
anchor below is two to five words long and why the length band gate exempts headline
benches. Do not read the headline anchors as permission to write short observations.

### comeback, headline, 91 firings a year, 60 lines, 10 listed

| key | tag | band | line | what the voice carries |
|---|---|---|---|---|
| `hd.back.01` | P | SHORT | `{areaName} came back.` | Three words. The return is the whole headline. |
| `hd.back.02` | P | SHORT | `{areaName} moved again.` | `again` does the work that a longer line would spend a clause on. |
| `hd.back.03` | P | SHORT | `Something returned.` | No name at all, for the week where naming would overstate it. |
| `hd.back.04` | P | SHORT | `{areaName}, after {ageDays}.` | A name, a comma, a duration. No verb anywhere. |
| `hd.back.05` | P | SHORT | `A revival.` | Two words, and the only abstract noun the family uses. |
| `hd.back.06` | P | SHORT | `{areaName} is back.` | Present tense where the others use past. Deliberately the warmest of the eight. |
| `hd.back.07` | P | SHORT | `The quiet one moved.` | Names the area by its recent history rather than by its title. |
| `hd.back.08` | P | SHORT | `{areaName} woke up.` | The same personification the Pulse family uses, which is how the two stay one voice. |
| `hd.back.24` | P | SHORT | `{ageDays} of nothing, then {areaName}.` | The gap first and the name second, which is the order the week happened in. |
| `hd.back.39` | P | SHORT | `What stopped has started.` | No name, no number, and no verb the person is the subject of. |

### personalBest, headline, 88 firings a year, 60 lines, 10 listed

| key | tag | band | line | what the voice carries |
|---|---|---|---|---|
| `hd.best.01` | P | SHORT | `Your most productive week.` | States the record without a word of praise attached. |
| `hd.best.02` | P | SHORT | `A record.` | Two words. Nothing is congratulated. |
| `hd.best.03` | P | SHORT | `More than any week before.` | A comparison with no adjective in it. |
| `hd.best.04` | P | SHORT | `The most you have finished.` | `you` appears, and the verb is still just finishing. |
| `hd.best.05` | P | SHORT | `A new high.` | The house register for a peak: flat, factual, three words. |
| `hd.best.06` | P | SHORT | `Nothing has come close.` | A superlative expressed as a negation, which keeps the temperature down. |
| `hd.best.07` | P | SHORT | `{n} completions. A first.` | Two beats: number, then category. |
| `hd.best.08` | P | SHORT | `Your best week here.` | `here` scopes the claim to the app, which is the only place it is true. |
| `hd.best.09` | P | SHORT | `Above everything before it.` | Spatial rather than evaluative. |
| `hd.best.10` | P | SHORT | `A week that stands out.` | The softest of the ten, for a record that is narrow. |

### mostActiveSince, headline, 78 firings a year, 60 lines, 10 listed

| key | tag | band | line | what the voice carries |
|---|---|---|---|---|
| `hd.since.01` | P | SHORT | `Your busiest week since {sinceRef}.` | The plain form of the whole family. |
| `hd.since.02` | P | SHORT | `Nothing like this since {sinceRef}.` | A negation carrying the same fact, which is how the bench varies without varying the claim. |
| `hd.since.03` | P | SHORT | `The most since {sinceRef}.` | Four words. No noun for what there was most of. |
| `hd.since.04` | P | SHORT | `A return to {sinceRef} form.` | The one line that reads the record as a return rather than a peak. |
| `hd.since.05` | P | SHORT | `Not since {sinceRef}.` | Three words, and a fragment rather than a sentence. |
| `hd.since.06` | P | MEDIUM | `Back to where you were in {sinceRef}.` | The longest of the eight, and the only one addressing the reader directly. |
| `hd.since.07` | P | MEDIUM | `{sinceRef} was the last week like this.` | Puts the reference week in the subject position. |
| `hd.since.08` | P | SHORT | `The strongest since {sinceRef}.` | `strongest` is the only intensifier in the family. |
| `hd.since.17` | P | SHORT | `A peak, though not the peak.` | The one line that says the record is unbroken, and it says it without apology. |
| `hd.since.41` | P | MEDIUM | `A rise. An old mark still stands.` | Two beats where the second does not explain the first, which is the house rhythm. |

### balanced, headline, 62 firings a year, 60 lines, 10 listed

| key | tag | band | line | what the voice carries |
|---|---|---|---|---|
| `hd.bal.01` | P | SHORT | `Attention everywhere.` | Two words, no verb, no count. |
| `hd.bal.02` | P | SHORT | `A wide week.` | The house adjective for breadth. Never `balanced`, which would grade it. |
| `hd.bal.04` | P | SHORT | `No center.` | A negation as a whole headline. |
| `hd.bal.05` | P | SHORT | `Spread across the board.` | Idiom used once, and it is the only idiom here. |
| `hd.bal.06` | P | SHORT | `Every area moved.` | The literal fact, three words. |
| `hd.bal.07` | P | SHORT | `A week in all directions.` | Spatial image, no judgment about whether that is good. |
| `hd.bal.08` | P | SHORT | `Nothing dominated.` | Two words, and the negation is the whole point. |
| `hd.bal.09` | P | SHORT | `Broad, not deep.` | The `X, not Y` shape, which this family owns and which the construction gate caps at two families. |
| `hd.bal.11` | P | SHORT | `Evenly distributed.` | The flattest possible statement of the week. |
| `hd.bal.12` | P | SHORT | `A week without a subject.` | The one line that is nearly a figure of speech, and it still states a fact. |

### intakeVsOutput, observation, 212 firings a year, 240 lines, 10 listed

| key | tag | band | line | what the voice carries |
|---|---|---|---|---|
| `ob.flow.s1.l02` | P | SHORT | `{n} in, {m} out.` | Four words for the entire week's flow. Nothing says which way is better. |
| `ob.flow.s1.l03` | O | SHORT | `A little more arrived than left.` | `a little` sets the stage without a number, so stage 1 cannot read as an alarm. |
| `ob.flow.s1.l06` | E | MEDIUM | `The week was slightly better at noticing than finishing.` | Editorial at its quietest: the week is the subject and both halves are skills. |
| `ob.flow.s1.e02` | O | MEDIUM | `Last week the balance went the other way.` | An extension whose whole job is to stop one week reading as a trend. |
| `ob.flow.s2.l04` | E | MEDIUM | `This was a week of collecting rather than closing.` | Two gerunds, neither of them praised or blamed. |
| `ob.flow.s2.l10` | N | SHORT | `{n} things arrived. {m} left.` | The neutral agent register exactly as 7.4 defines it. The things act; the person is absent. |
| `ob.flow.s2.l14` | N | MEDIUM | `Nothing added this week has left yet.` | Same register, negative fact, and still nobody is doing anything wrong. |
| `ob.flow.s2.e04` | E | MEDIUM | `Capture is useful right up until the point the queue stops being read.` | The furthest an editorial line reaches, and it is a general truth rather than an accusation. |
| `ob.flow.s3.l07` | E | MEDIUM | `The list got lighter, and it stayed lighter.` | The positive stage, and it is still just two facts about a list. |
| `ob.flow.s3.l10` | P | SHORT | `The list got shorter.` | Four words. Note that the good week gets no more celebration than the heavy one gets blame. |

### areaRevival, observation, 219 firings a year, 80 lines, 10 listed

| key | tag | band | line | what the voice carries |
|---|---|---|---|---|
| `ob.rev.l01` | P | MEDIUM | `{areaName} moved again after {ageDays} of nothing.` | `of nothing` is the strongest phrase the family allows, and it describes time rather than a person. |
| `ob.rev.l02` | O | SHORT | `{areaName} came back this week.` | Five words. No welcome, no relief. |
| `ob.rev.l04` | E | MEDIUM | `Something you had stopped touching started moving again.` | `you had stopped` states a fact about the past without asking why. |
| `ob.rev.l06` | O | MEDIUM | `{areaName} had been the quietest area. It was not this week.` | Two beats where the second reverses the first, which is this family's characteristic shape. |
| `ob.rev.e02` | E | MEDIUM | `Whether it holds is next week's question.` | Declines to predict, and hands the question forward rather than asking it. |
| `ob.rev.e04` | O | SHORT | `Its queue is now empty.` | A flat detail that earns its place by being checkable. |
| `ob.rev.e05` | E | MEDIUM | `Returns like this are usually a decision rather than an accident.` | Credits the person indirectly, by describing a class of events rather than this one. |
| `ob.rev.l14` | O | MEDIUM | `{ageDays} of stillness, then a week with something in it.` | The gap is the subject and the week is only what happened to it. |
| `ob.rev.l53` | E | MEDIUM | `{areaName} was not gone. It was still.` | Two beats where the second corrects the category rather than the fact. |
| `ob.rev.l44` | P | LONG | `{areaName} had been quiet for {ageDays} before this week, and the report had nothing to say about it in that time.` | The app naming its own silence, and never the person's. |

### queuePressure, observation, 156 firings a year, 80 lines, 10 listed

| key | tag | band | line | what the voice carries |
|---|---|---|---|---|
| `ob.qp.l01` | P | MEDIUM | `Your queues hold {n} things, up from {m}.` | Two numbers and a direction. No word for whether that is a lot. |
| `ob.qp.l02` | O | MEDIUM | `{areaName} is holding {n} items behind its active one.` | The area holds the queue, and `behind` is spatial here, which is the sense 11.3 protects. |
| `ob.qp.l03` | O | MEDIUM | `Two areas grew their queues this week.` | The areas grew them. The person is not in the sentence. |
| `ob.qp.l04` | E | MEDIUM | `There is more waiting now than at any point this month.` | A record stated with `there is`, which keeps the person out of the subject position. |
| `ob.qp.l05` | P | MEDIUM | `{n} items are queued across {areaCount} areas.` | Two counts, one preposition, nothing else. |
| `ob.qp.l06` | O | MEDIUM | `The longest queue is in {areaName}, at {n}.` | A superlative and a number, with no word for whether the number is bad. |
| `ob.qp.e02` | O | MEDIUM | `The queues have grown three weeks running.` | The one trend claim, and it is a count of weeks rather than a warning. |
| `ob.qp.e04` | E | MEDIUM | `Length is not the problem. Length that never shortens is.` | The family's sharpest line, and it names the problem as a property of a queue. |
| `ob.qp.l07` | N | SHORT | `{n} things are in the queues.` | Neutral agent at its shortest. A count and a place, and no verb the person is the subject of. |
| `ob.qp.l09` | E | LONG | `A queue is a record of decisions not yet made, and this one got longer.` | Editorial reaching once, and what it reaches for is a definition rather than a judgment. |

### areaBalance, observation, 118 firings a year, 80 lines, 10 listed

| key | tag | band | line | what the voice carries |
|---|---|---|---|---|
| `ob.bal.l01` | P | SHORT | `{areaName} at {pct}, {otherArea} at {otherPct}.` | Two areas, two numbers, no verb. |
| `ob.bal.l02` | O | MEDIUM | `Attention split across {areaCount} areas, none above {pct}.` | A ceiling stated instead of a spread, which is more useful and shorter. |
| `ob.bal.l03` | O | SHORT | `Every area moved this week.` | Five words. The whole observation. |
| `ob.bal.l04` | E | SHORT | `No single area owned this week.` | Editorial in six words, which is rarer than editorial at twelve. |
| `ob.bal.l05` | P | MEDIUM | `{areaCount} areas active, {n} events between them.` | Two counts joined by a comma rather than by an argument. |
| `ob.bal.l06` | O | MEDIUM | `The gap between your busiest and quietest area was {n} events.` | Measures the distance between two areas rather than ranking them. |
| `ob.bal.e01` | O | MEDIUM | `That is more even than any week this month.` | A comparison against the person's own history, never against anybody else's. |
| `ob.bal.e02` | O | MEDIUM | `Completions were less even than events: {n} of {m} were in {areaName}.` | Complicates the family's own headline finding, which is what an extension is for. |
| `ob.bal.e03` | E | MEDIUM | `Broad weeks and deep weeks measure different things.` | Refuses the ranking the reader is about to make. |
| `ob.bal.l16` | E | MEDIUM | `Spread is not the same as balance. This week was spread.` | Refuses the word the reader is about to reach for, in the same breath as the fact. |

### persistentItem, observation, 90 firings a year, 80 lines, 10 listed

| key | tag | band | line | what the voice carries |
|---|---|---|---|---|
| `ob.pers.l01` | P | MEDIUM | `{itemTitle} has been active in {areaName} for {ageDays}.` | The plain statement of the whole family. |
| `ob.pers.l02` | O | MEDIUM | `Nothing has moved past {itemTitle} in {ageDays}.` | The absence as subject, exactly as the Pulse family does it. |
| `ob.pers.l04` | E | MEDIUM | `{itemTitle} has stopped being this week's work and become a standing condition.` | A category change rather than a verdict, which is the whole trick of this family. |
| `ob.pers.l05` | P | MEDIUM | `{ageDays} on {itemTitle}, with {n} things queued behind it.` | The spatial sense of `behind`, which is correct here and which the vocabulary gate protects. |
| `ob.pers.l06` | O | MEDIUM | `Most things you finish take {medianDays}. This one is at {ageDays}.` | Two beats and a comparison the reader draws. |
| `ob.pers.l07` | E | MEDIUM | `There is one item here that has been true for longer than anything else.` | `has been true` treats the item as a statement rather than a task. |
| `ob.pers.e14` | O | MEDIUM | `It has been at the front since before this week began.` | The week is the frame and the item predates it, which is the whole observation without a number. |
| `ob.pers.e03` | E | MEDIUM | `Long is not wrong. Long and unexamined is a different thing.` | Two beats, and the first exists only to disarm the second. |
| `ob.pers.e04` | O | SHORT | `{n} things are waiting behind it.` | Six words, one number, the spatial `behind` again. |
| `ob.pers.l11` | N | LONG | `The front of {areaName} has been {itemTitle} for {ageDays} and nothing in the queue has come past it.` | The longest line here, and the person is not in it at all. The queue is what did not move. |

### timeOfDay, observation, 105 firings a year, 80 lines, 10 listed

| key | tag | band | line | what the voice carries |
|---|---|---|---|---|
| `ob.tod.l01` | P | MEDIUM | `Most of this week happened in the morning.` | The week happened. Nobody made it happen. |
| `ob.tod.l03` | O | MEDIUM | `Evenings were where most things got added.` | Time as a place, which is the family's one recurring image. |
| `ob.tod.l04` | E | MEDIUM | `You finish in the morning and collect at night.` | The one line that addresses the reader, and it describes a habit rather than prescribing one. |
| `ob.tod.l06` | O | MEDIUM | `Your focus sessions all started before 11am.` | A hard fact with a clock time in it, which the family uses sparingly. |
| `ob.tod.e01` | O | MEDIUM | `That has been consistent for three weeks.` | Duration as the only evidence offered. |
| `ob.tod.e02` | O | MEDIUM | `Last week it was the other way around.` | The extension that keeps one week from reading as a rule. |
| `ob.tod.e03` | E | MEDIUM | `Knowing when you finish things is more useful than knowing how many.` | The nearest the app comes to advice, and it is about knowing rather than doing. |
| `ob.tod.e13` | O | MEDIUM | `The concentration is in when, not in what.` | Names the axis the family reads and refuses the other one in the same breath. |
| `ob.tod.l31` | O | LONG | `When something happened this week, it was in one part of the day at least as often as in the other three put together.` | The claim spelled out exactly, because every shorter form of it rounds it off. |
| `ob.tod.l55` | E | SHORT | `Days have shapes.` | Three words of generalization, and the only line here with no week in it. |

### completionSplit, observation, 162 firings a year, 80 lines, 10 listed

| key | tag | band | line | what the voice carries |
|---|---|---|---|---|
| `ob.split.l05` | P | MEDIUM | `{n} answers this week: {m} positive, {k} not.` | The only interpretation the app makes of an answer is whether it was positive, and this line says exactly that much. |
| `ob.split.l06` | O | SHORT | `You said {priorLabel} {priorCount} times.` | Quotes the person's own word back, unchanged, and counts it. |
| `ob.split.e01` | O | SHORT | `The numbers agree with that.` | Five words. The agreement is stated, not celebrated. |
| `ob.split.e02` | O | SHORT | `The numbers read a little differently.` | Disagreement stated with the same flatness as agreement. That symmetry is the family. |
| `ob.split.e03` | O | SHORT | `Completions were up, which fits.` | `which fits` is the lightest possible way to connect two facts. |
| `ob.split.e04` | E | LONG | `What a week feels like and what it counts as are not always the same.` | The one LONG line here, and the only one that generalizes. |
| `ob.split.e05` | O | MEDIUM | `Last week the split went the other way.` | History used to widen the frame rather than to build a case. |
| `ob.split.e06` | E | MEDIUM | `That is worth holding next to the numbers above.` | Invites a comparison and does not make it. |
| `ob.split.l30` | O | LONG | `Across the week you gave {n} answers. {m} of them were positive and {k} were not.` | The whole split in one line, and the only reading of an answer the app is licensed to make. |
| `ob.split.l60` | E | LONG | `The only thing this app does with an answer is store it and count it.` | The app describing its own limits, which is the one place editorial is safe in a callback family. |

### mostActiveSince, observation, 88 firings a year, 80 lines, 10 listed

| key | tag | band | line | what the voice carries |
|---|---|---|---|---|
| `ob.since.l01` | P | SHORT | `Your busiest week since {sinceRef}.` | Five words, and the same sentence as the headline family, which is deliberate. |
| `ob.since.l02` | O | SHORT | `{n} events, the most since {sinceRef}.` | A number, then its rank, in one breath. |
| `ob.since.l03` | O | MEDIUM | `Nothing since {sinceRef} has looked like this.` | `has looked like` keeps the claim to appearance rather than to worth. |
| `ob.since.l04` | E | MEDIUM | `The last week with this much in it was {sinceRef}.` | Editorial by construction rather than by commentary. |
| `ob.since.l05` | P | SHORT | `{n} completions, the highest since {sinceRef}.` | Narrows the claim from events to completions, which is a different and smaller fact. |
| `ob.since.e02` | O | SHORT | `It has been {n} weeks.` | Five words, one number, no comment. |
| `ob.since.e03` | E | MEDIUM | `Whether that is a return or a spike shows up next week.` | Names two readings, picks neither, and puts the answer in the future. |
| `ob.since.l26` | P | MEDIUM | `{n} moves. No week since {sinceRef} finished more.` | A count and a ranking kept in separate sentences, because they are separate measures. |
| `ob.since.l45` | E | LONG | `Two facts sit side by side. This week beat last week. {sinceRef} still beats this week.` | Three beats, and the third is the one that stops the line reading as praise. |
| `ob.since.l53` | O | SHORT | `Nothing nearer than {sinceRef}.` | Four words, and the month carries the whole comparison on its own. |

### personalBest, observation, 88 firings a year, 80 lines, 10 listed

| key | tag | band | line | what the voice carries |
|---|---|---|---|---|
| `ob.best.l01` | P | MEDIUM | `{n} completions, more than any week before.` | The record, and nothing about how it feels. |
| `ob.best.l02` | O | MEDIUM | `This was your most productive week since you started.` | The word `productive` appears once in the family and never becomes a value. |
| `ob.best.l03` | O | MEDIUM | `The previous best was {m}, in {sinceRef}.` | Gives the reader the comparison rather than making it for them. |
| `ob.best.l04` | E | MEDIUM | `Nothing in eleven weeks of history looks like this one.` | Scope stated exactly, so the superlative is checkable. |
| `ob.best.l05` | P | SHORT | `A new high: {n} completions.` | A colon carries what a clause would have. |
| `ob.best.l06` | O | MEDIUM | `You finished more this week than in any two earlier weeks combined.` | The biggest claim in the family, and it is arithmetic. |
| `ob.best.l08` | P | SHORT | `{n} finished. A record.` | Two beats and four words, and the second beat is a category rather than a compliment. |
| `ob.best.e02` | O | SHORT | `It came alongside {sessions} focus sessions.` | `alongside` refuses to claim a cause. |
| `ob.best.e03` | E | MEDIUM | `Peaks are worth noticing. They are not worth defending.` | The line that keeps a record from becoming a standard, which is the whole reason this family is careful. |
| `ob.best.e05` | E | MEDIUM | `What made the difference is worth knowing, if you can name it.` | An invitation with an escape in it. Nothing here has to be answered. |

### selfReportVsData, observation, 85 firings a year, 80 lines, 10 listed

This is the flagship family, and 9.1 gives it a cooldown that never lets it repeat about the
same subject. Every line quotes the person to themselves, and none of them wins the argument.

| key | tag | band | line | what the voice carries |
|---|---|---|---|---|
| `ob.srvd.l03` | E | MEDIUM | `You said {priorLabel}. Since then, {itemTitle} has not moved.` | The quote, then the fact. The gap between them is left entirely alone. |
| `ob.srvd.l05` | E | MEDIUM | `You described that as {priorLabel}. The week went on to prove you right.` | The family points both ways, and this is the direction that is easy to forget to write. |
| `ob.srvd.l06` | O | MEDIUM | `You called the concentration in {areaName} deliberate. It held all week.` | Confirms the person's own reading without congratulating them for it. |
| `ob.srvd.l09` | E | LONG | `Last time this area went quiet you called it planned. This time it lasted longer.` | The longest line in the family and still no conclusion drawn. |
| `ob.srvd.e01` | E | SHORT | `Both things can be true.` | Five words that make the whole family safe to read. |
| `ob.srvd.e02` | O | MEDIUM | `That was the only pulse you answered about it.` | Undercuts the app's own evidence, on purpose. |
| `ob.srvd.e03` | E | MEDIUM | `Worth revisiting the answer, or the item.` | Two options, and the first one lets the person be right. |
| `ob.srvd.e04` | O | MEDIUM | `You have said the same thing about it twice now.` | A count of the person's own words, with no reading attached. |
| `ob.srvd.e05` | E | MEDIUM | `The first read may still be the right one.` | The app siding with the person against its own numbers. |
| `ob.srvd.e06` | O | MEDIUM | `It has been active {ageDays} in total.` | A plain fact placed after a delicate one, to land the section on the ground. |

### singleFocus, observation, 49 firings a year, 160 lines, 10 listed

| key | tag | band | line | what the voice carries |
|---|---|---|---|---|
| `ob.single.s1.l04` | O | SHORT | `{areaName} took most of the week.` | Six words. The area took it; nobody gave it. |
| `ob.single.s1.l06` | O | MEDIUM | `The week had one clear center, and it was {areaName}.` | The center of gravity image, shared with the Pulse family and used at the same weight. |
| `ob.single.s1.l08` | E | MEDIUM | `{areaName} was not one of the things you did. It was the thing.` | Two beats, and the second is a correction of the first. |
| `ob.single.s1.e05` | E | MEDIUM | `Weeks like that are usually deliberate, or they are drift.` | Two readings, named in the same sentence, neither chosen. |
| `ob.single.s1.e08` | O | SHORT | `The queues elsewhere did not change.` | A consequence stated as a fact about queues rather than about neglect. |
| `ob.single.s2.l05` | E | MEDIUM | `For seven days, the app had one subject.` | The app is the subject of the sentence. That is the second stage's whole tone. |
| `ob.single.s2.l09` | E | MEDIUM | `A week this narrow is either a sprint or a blind spot.` | The sharpest line in the family, and it still offers both readings. |
| `ob.single.s2.l14` | P | SHORT | `{pct}, one area.` | Three words. A percentage and a count. |
| `ob.single.s2.l15` | N | SHORT | `One area. All week.` | Neutral agent at its shortest: two fragments, no verb, no person. |
| `ob.single.s2.e04` | E | MEDIUM | `Sprints end. Blind spots do not, until something forces them to.` | Follows `l09` without repeating it, which is what an extension is for. |

### steadyPace, observation, 43 firings a year, 80 lines, 10 listed

| key | tag | band | line | what the voice carries |
|---|---|---|---|---|
| `ob.stead.l01` | P | MEDIUM | `{n} completions, close to your average of {m}.` | The person's own average, never a target. |
| `ob.stead.l02` | O | MEDIUM | `This week looked much like the three before it.` | `looked much like` is deliberately imprecise where a number would imply a standard. |
| `ob.stead.l03` | O | MEDIUM | `Activity has stayed within a narrow band for a month.` | A band, not a line. Nothing is being held to a level. |
| `ob.stead.l04` | E | MEDIUM | `Nothing about this week stands out, which is its own kind of result.` | The family's reason to exist, in one sentence. |
| `ob.stead.l05` | P | MEDIUM | `{n} events, {m} last week, {k} the week before.` | Three numbers in a row, and the steadiness is left for the reader to see. |
| `ob.stead.e01` | O | MEDIUM | `The distribution across areas was similar too.` | A second dimension of the same steadiness, stated flatly. |
| `ob.stead.e02` | E | MEDIUM | `Consistency is harder to notice than a spike and usually worth more.` | The one evaluative line, and what it values is the ordinary week. |
| `ob.stead.e03` | O | MEDIUM | `Your queues have stayed the same length throughout.` | Steadiness in the one place a reader might expect drift. |
| `ob.stead.l55` | O | SHORT | `The week held.` | Three words for a family whose subject is that nothing happened worth more. |
| `ob.stead.l46` | E | LONG | `What the app can see is that the size held. What that was like is not in the record.` | Names the limit of the measurement in the same breath as the measurement. |

### reportedVsActual, pattern, 177 firings a year, 60 lines, 10 listed

| key | tag | band | line | what the voice carries |
|---|---|---|---|---|
| `pt.rva.04` | P | MEDIUM | `Your answers have been consistent. So has the pattern behind them.` | Two beats, and the parallel is the entire observation. |
| `pt.rva.06` | P | MEDIUM | `The same answer, three weeks in a row, about the same thing.` | Three fragments joined by commas, no verb, no conclusion. |
| `pt.rva.12` | P | SHORT | `A repeated question.` | Three words, and the repetition is the entire finding. |
| `pt.rva.26` | P | SHORT | `Answers accumulate.` | Two words, no person in the sentence, and the shortest true thing the family has. |
| `pt.rva.20` | P | SHORT | `The app kept asking.` | The app is the subject, so nobody is answering to anybody. |
| `pt.rva.30` | P | MEDIUM | `The same kind of question has come back to you three times.` | The count is exact and the subject is the question rather than the answer. |
| `pt.rva.34` | P | MEDIUM | `Five answers or more sit in the record now.` | A floor rather than a figure, because a floor is what the rule actually guarantees. |
| `pt.rva.50` | P | LONG | `A question that comes back three times is a question about something that did not resolve between the askings.` | The one line that reads the repetition, and it reads it as a fact about the situation. |
| `pt.rva.51` | P | LONG | `Three answers about one kind of moment is not a verdict on anything, and it is a record that the moment kept happening.` | Names what the count is not before it names what the count is. |
| `pt.rva.58` | P | LONG | `Nothing here compares what you said against what happened. It records that you said something, three times.` | The family's name promises a comparison and this line declines to make one. |

### comebackPattern, pattern, 70 firings a year, 5 lines, 1 sayable

Three of the five make a claim no fact supports and are held out of the bench by
`SlotBindings.EXCLUDED`; the fourth needs a marker nothing binds. **This family fires 70
times a year and has exactly one sentence.**

| key | tag | band | line | what the voice carries |
|---|---|---|---|---|
| `pt.come.03` | P | MEDIUM | `{areaName} moves in bursts, with weeks of nothing between them.` | Describes a rhythm rather than counting it, which is why it is the one line here that is true. |
| `pt.come.19` | P | SHORT | `{areaName} stops. {areaName} starts.` | The area named twice, in two beats, which is the pattern said as rhythm rather than counted. |
| `pt.come.44` | P | SHORT | `{areaName} comes and goes.` | Four words. The shortest true thing this family has. |
| `pt.come.51` | P | SHORT | `The gaps ended.` | Three words, past tense, and the area is not named at all. |
| `pt.come.16` | P | MEDIUM | `This is at least the second return for {areaName}.` | `at least` is the whole discipline here: the rule counts two or more and never exactly two. |
| `pt.come.20` | P | MEDIUM | `Whatever stops {areaName} has stopped it before.` | Names a cause without claiming to know it. |
| `pt.come.30` | P | MEDIUM | `Every quiet spell in {areaName} so far has ended.` | `so far` keeps a record of the past from becoming a prediction. |
| `pt.come.36` | P | MEDIUM | `The silences in {areaName} have all been temporary so far.` | The same hedge from the other side, so the bench can vary without varying the claim. |
| `pt.come.55` | P | LONG | `Whether the quiet weeks in {areaName} were a decision or an interruption is not something the app has any way to know.` | Two readings named, neither chosen, and the app admits which of them it cannot tell. |
| `pt.come.57` | P | LONG | `Two things are true of {areaName}: it has been quiet for whole weeks, and it is not quiet now.` | A colon and two facts. No third clause drawing them together. |

### growingQueues, pattern, 58 firings a year, 60 lines, 10 listed

| key | tag | band | line | what the voice carries |
|---|---|---|---|---|
| `pt.grow.01` | P | MEDIUM | `Your queues have grown every week for three weeks.` | The pattern stated once, plainly, with the window named. |
| `pt.grow.02` | P | MEDIUM | `Total waiting: {k}, then {m}, then {n}.` | Three numbers and two commas. The reader draws the line. |
| `pt.grow.03` | P | MEDIUM | `Nothing has come back down in three weeks.` | The same fact as a negation, which is how a one register bench varies. |
| `pt.grow.05` | P | MEDIUM | `More arrives than leaves, and has for three weeks.` | Things arrive and leave on their own. |
| `pt.grow.07` | P | SHORT | `Three consecutive weeks of net growth.` | Six words, no verb, no person. |
| `pt.grow.09` | P | SHORT | `The line keeps going up.` | Five words, and the only figure of speech in the family. |
| `pt.grow.10` | P | MEDIUM | `Each week ends with more waiting than it started with.` | The week is the subject, and the shape is stated rather than the size. |
| `pt.grow.30` | P | MEDIUM | `Longer than last week. Longer than the week before that.` | The repetition is the rise; nothing states it a third time. |
| `pt.grow.49` | P | LONG | `Whether the queues are longer because more arrived or because less left is not something this reading separates.` | The unflattering family saying what it has not measured, before anyone infers it. |
| `pt.grow.58` | P | SHORT | `Still rising.` | Two words. The shortest the family gets, and it still names no one. |

---

## Momentum and the Areas banner

Four registers are permitted here: `[P]` plain, `[O]` observational, `[R]` reflective and
`[N]` neutral agent. Editorial is the Report's alone, because a clever line read several
times a day is tiresome by the third reading, and these are the two surfaces a person sees
most often. **A Momentum headline is capped at eleven words by validator check 9**, so the
band gate holds these benches to two bands rather than three.

The banner families are the highest firing benches in the app: `weekMixed` fires 1,241 times
a year against seventy seven lines. Read those four banner tables as the tightest constraint in the
whole corpus, not as the loosest.

### singleAreaWeek, Momentum, 1,033 firings a year, 12 lines, 10 listed

| key | tag | band | line | what the voice carries |
|---|---|---|---|---|
| `mo.single.01` | P | MEDIUM | `{pct} of the last fortnight was {areaName}.` | A percentage and a window. Nothing about whether that is narrow. |
| `mo.single.02` | O | MEDIUM | `{areaName} has held most of the last two weeks.` | The area holds the time, rather than the person spending it. |
| `mo.single.03` | O | MEDIUM | `Attention has stayed mostly in one area.` | `stayed` rather than `was stuck`, which is the whole difference. |
| `mo.single.04` | R | SHORT | `A narrow fortnight.` | Three words. Reflective here means brief, not wistful. |
| `mo.single.05` | P | SHORT | `Mostly {areaName}, for two weeks.` | A fragment with a comma in the middle of it. |
| `mo.single.07` | R | SHORT | `One area, most of the time.` | Six words, two fragments, no verb. |
| `mo.single.08` | N | MEDIUM | `Most of the fortnight landed in a single area.` | The fortnight lands. Nobody put it anywhere. |
| `mo.single.10` | R | SHORT | `Deep rather than broad.` | Four words, and it declines to say which is better. |
| `mo.single.11` | P | SHORT | `{areaName}, mostly.` | Two words. The shortest line in the family and one of the best. |
| `mo.single.12` | N | MEDIUM | `One area holds {pct} of the fortnight.` | Neutral agent with a number in it, which is the register's most useful shape. |

### balancedWeek, Momentum, 849 firings a year, 12 lines, 10 listed

| key | tag | band | line | what the voice carries |
|---|---|---|---|---|
| `mo.bal.01` | P | SHORT | `{areaCount} areas moved this fortnight.` | A count and a window, five words. |
| `mo.bal.02` | O | SHORT | `Attention spread across {areaCount} areas.` | `spread` with no adverb attached to it. |
| `mo.bal.03` | O | SHORT | `No single area has taken over.` | A negation, and `taken over` is the strongest verb the family uses. |
| `mo.bal.04` | R | SHORT | `A wide fortnight.` | Three words, and it mirrors `A narrow fortnight` in the opposite family. That mirroring is intentional and is the only pair like it. |
| `mo.bal.06` | O | MEDIUM | `Every area has seen something in the last two weeks.` | `something` refuses to quantify where a number would grade. |
| `mo.bal.07` | R | SHORT | `Evenly spread, across two weeks.` | Five words, one comma, no verb. |
| `mo.bal.08` | O | MEDIUM | `{areaName} and {otherArea} have moved about the same amount.` | Two named areas and an approximation. |
| `mo.bal.10` | R | SHORT | `Broad rather than deep.` | The reverse of `mo.single.10`, word for word in structure. |
| `mo.bal.11` | O | MEDIUM | `The gap between your busiest and quietest area is small.` | Describes a gap rather than praising a balance. |
| `mo.bal.12` | R | SHORT | `Nothing has dominated.` | Three words. The whole fortnight. |

### comeback, Momentum, 608 firings a year, 12 lines, 11 sayable, 10 listed

| key | tag | band | line | what the voice carries |
|---|---|---|---|---|
| `mo.come.01` | P | SHORT | `Back after {ageDays}.` | Three words, no subject at all. |
| `mo.come.02` | O | MEDIUM | `Activity picked up again after {ageDays} of nothing.` | Activity is the subject. `of nothing` describes time, not a person. |
| `mo.come.04` | R | SHORT | `A return, after a gap.` | Five words and two commas doing the pacing. |
| `mo.come.05` | P | SHORT | `{ageDays} quiet, then this week.` | The pivot shape the Pulse rebalance family also uses, kept identical. |
| `mo.come.06` | O | MEDIUM | `The last few days have been busier than the fortnight before.` | A comparison inside the same window, which is all this surface has. |
| `mo.come.07` | R | SHORT | `Something restarted.` | Two words. The most restrained thing the family says. |
| `mo.come.08` | O | SHORT | `{areaName} came back first.` | `first` implies others followed without claiming they did. |
| `mo.come.09` | P | SHORT | `Moving again.` | Two words, no subject, present participle. |
| `mo.come.10` | R | SHORT | `The gap ended.` | Three words, and the gap is the subject. |
| `mo.come.12` | R | SHORT | `Back in motion.` | Three words. Note that none of these twelve congratulates anybody. |

### steadyStretch, Momentum, 343 firings a year, 18 lines, 10 listed

| key | tag | band | line | what the voice carries |
|---|---|---|---|---|
| `mo.steady.01` | P | MEDIUM | `Active {dayCount} of the last 14 days.` | The bare count. Note there is no word for a run anywhere in this family, and that `14` is a numeral because `{dayCount}` is one on five of the six values it takes here. |
| `mo.steady.03` | O | MEDIUM | `A steady stretch, {dayCount} of the last 14 days.` | `stretch` is the house word where another app would say streak. |
| `mo.steady.04` | O | MEDIUM | `Most days in the last fortnight had something in them.` | `something` again, because the count is already in the line above. |
| `mo.steady.07` | R | SHORT | `A consistent stretch.` | Three words, and `stretch` again rather than any word for a run. |
| `mo.steady.08` | R | MEDIUM | `The last two weeks have been fairly even.` | `fairly` is a hedge that stops the line becoming a standard to keep. |
| `mo.steady.10` | P | SHORT | `{dayCount} of 14.` | Three words. A ratio with no noun, and two numerals rather than a numeral beside a number word. |
| `mo.steady.12` | N | SHORT | `14 days, {dayCount} with activity.` | Neutral agent: the days are the subject and they simply have activity in them. |
| `mo.steady.13` | R | SHORT | `A rhythm, more than a run.` | The family saying out loud what it refuses to be. |
| `mo.steady.16` | R | SHORT | `Even going, two weeks running.` | `running` in its spatial sense, which is allowed, unlike the banned noun. |
| `mo.steady.18` | O | MEDIUM | `A stretch with very few gaps in it.` | Counts the gaps instead of the days, which is the same fact from the other side. |

### firstDays, Momentum, 139 firings a year, 62 lines, 10 listed

| key | tag | band | line | what the voice carries |
|---|---|---|---|---|
| `mo.first.01` | P | SHORT | `{dayCount} days in.` | Three words and no welcome. The app does not greet anybody. |
| `mo.first.02` | O | SHORT | `Early days. {n} things so far.` | Two beats, and `so far` keeps the count from reading as a total. |
| `mo.first.03` | O | MEDIUM | `{n} completions in your first {dayCount} days.` | A count with the window that makes it small, so it cannot read as a target. |
| `mo.first.04` | R | SHORT | `Just getting going.` | Three words, and the simulator caught this exact line firing on two consecutive days, which is why this family is being grown. |
| `mo.first.05` | P | SHORT | `Your first week is taking shape.` | Present continuous, so nothing is finished and nothing is owed. |
| `mo.first.06` | O | SHORT | `{areaCount} areas, {n} moves so far.` | Two counts and a comma. The shortest way to describe a week nobody has finished. |
| `mo.first.07` | R | SHORT | `The beginning of a picture.` | The one image the family uses. |
| `mo.first.08` | O | MEDIUM | `This page fills out as the days do.` | Describes the app rather than the person, which is the right subject in week one. |
| `mo.first.09` | P | SHORT | `{dayCount} days, {n} completions.` | Two numbers and one comma. There is no verb to make either of them into an achievement. |
| `mo.first.10` | R | SHORT | `Early, but it is starting.` | The only `but` in the family, and it does not turn against the reader. |

### quietStretch, Momentum, 153 firings a year, 66 lines, 10 listed

| key | tag | band | line | what the voice carries |
|---|---|---|---|---|
| `mo.quiet.01` | P | MEDIUM | `Active {dayCount} of the last fourteen days.` | The near twin of `mo.steady.01`, carrying the opposite reading in its number alone. It keeps the word `fourteen` where the steady line takes `14`, because `{dayCount}` never reaches ten in this family and a word beside a word is what 7.2 asks for. |
| `mo.quiet.03` | N | SHORT | `A quiet fortnight.` | Three words. The neutral agent register with nothing to be neutral about but the time. |
| `mo.quiet.04` | O | MEDIUM | `The last two weeks have been mostly still.` | `still` rather than empty, and never inactive. |
| `mo.quiet.06` | R | SHORT | `A slow stretch.` | Three words, and `slow` is as far as it goes. |
| `mo.quiet.07` | N | MEDIUM | `Most of the last fortnight passed without a move here.` | The fortnight passed. Nobody let it. |
| `mo.quiet.08` | R | SHORT | `Quiet, across two weeks.` | Four words paced by a comma, which is how this surface slows a line down without adding one. |
| `mo.quiet.09` | P | SHORT | `{dayCount} active days in fourteen.` | Counts the active days rather than the quiet ones, which is the kinder arithmetic and the same number. |
| `mo.quiet.11` | R | SHORT | `A still fortnight.` | Three words. This is the line the fourteen day window exists to allow. |
| `mo.quiet.12` | N | SHORT | `Little has moved in two weeks.` | `little`, not nothing, because nothing is almost never true. |
| `mo.quiet.13` | O | MEDIUM | `{areaName} took more of the fortnight than any other.` | Finds the one thing that did happen and names it. `more than any other` rather than `most`, because `dominantAreaId` is a strict maximum and four areas splitting a quiet fortnight would make `most` false. |

### weekMixed, Areas banner, 1,241 firings a year, 77 lines, 10 listed

The highest firing bench in the app, and phase 9 took it from eight lines to seventy seven. The last two rows are from that growth, and they are here because the bench had no reflective line at all before it and the realizer asks for that voice first.

| key | tag | band | line | what the voice carries |
|---|---|---|---|---|
| `bn.mixed.01` | P | SHORT | `An uneven week so far.` | `so far` appears throughout this surface, because the week is not over and the banner must never sound final. |
| `bn.mixed.02` | O | SHORT | `Busy in places.` | Three words. A caption, not a sentence. |
| `bn.mixed.03` | N | MEDIUM | `Some areas have moved, some have not.` | Two clauses, perfectly balanced, no preference between them. |
| `bn.mixed.04` | O | SHORT | `A mixed week.` | Three words, and the adjective is the flattest one available for the shape. |
| `bn.mixed.05` | P | SHORT | `Concentrated in {areaName} this week.` | The only line here that names an area. |
| `bn.mixed.06` | N | MEDIUM | `The week has had a shape to it.` | Says almost nothing on purpose, which a caption under a headline is allowed to do. |
| `bn.mixed.07` | O | SHORT | `Movement in {areaCount} of {m} areas.` | Two numbers and no verb, which is what a caption under a headline should be. |
| `bn.mixed.08` | P | SHORT | `Patchy so far.` | Three words, and `patchy` is the strongest adjective this surface uses. |
| `bn.mixed.15` | R | SHORT | `Tilted, so far.` | Three words and a comma. The reflective voice on this surface is the plain one with the sentence taken away. |
| `bn.mixed.13` | R | MEDIUM | `A week with a center of gravity in it.` | The family's one image, and it describes a distribution rather than grading it. |

### weekStarting, Areas banner, 732 firings a year, 62 lines, 10 listed

The bench with the least to say: one or two days into a week with under three completions is almost no fact at all. The last two rows are from the phase 9 growth, for the same reason as `weekMixed`.

| key | tag | band | line | what the voice carries |
|---|---|---|---|---|
| `bn.start.01` | P | SHORT | `Your week is just getting started.` | Six words, present continuous, nothing expected. |
| `bn.start.02` | O | SHORT | `Early in the week.` | Four words. A position in time and nothing else. |
| `bn.start.03` | P | SHORT | `The week is young.` | Four words, and the one mild figure of speech. |
| `bn.start.04` | N | SHORT | `Two days in.` | Three words and no subject. The reader supplies who is two days in. |
| `bn.start.05` | O | MEDIUM | `Not much yet, which is normal for a Monday.` | Names the emptiness and disarms it in the same line. |
| `bn.start.06` | P | SHORT | `A fresh week.` | Three words. `fresh` is the warmest adjective the banner uses anywhere. |
| `bn.start.07` | N | SHORT | `The week has just opened.` | The week opens itself, which keeps the person out of a sentence about time passing. |
| `bn.start.08` | O | SHORT | `Starting out.` | Two words, and it is a complete caption. |
| `bn.start.15` | R | SHORT | `Nothing settled yet.` | Three words. `yet` is what keeps a description of emptiness from being a verdict on it. |
| `bn.start.22` | R | MEDIUM | `There is more week ahead than behind.` | `behind` in its spatial sense, and the line points at what is left rather than at what is missing. |

### weekBuilding, Areas banner, 331 firings a year, 72 lines, 10 listed

| key | tag | band | line | what the voice carries |
|---|---|---|---|---|
| `bn.build.01` | P | SHORT | `The week is building.` | Four words, present continuous. |
| `bn.build.02` | O | SHORT | `Things are moving.` | Three words. Things move on their own. |
| `bn.build.03` | P | SHORT | `A few things done so far.` | `a few` where a number would set a pace. |
| `bn.build.04` | O | SHORT | `Steady so far this week.` | Five words, and `steady` is the house word for this shape everywhere it appears. |
| `bn.build.05` | N | SHORT | `{n} through, midweek.` | Three words. The only line here with a number in it. |
| `bn.build.06` | P | SHORT | `Making progress this week.` | Four words, and progress is stated rather than praised. |
| `bn.build.07` | O | SHORT | `On pace with last week.` | The person's own last week is the only reference point available. |
| `bn.build.08` | P | SHORT | `A working week.` | Three words, and `working` describes the week rather than praising the worker. |
| `bn.build.09` | R | SHORT | `A week under way.` | Four words. The bench had no reflective line before phase 9, and this surface's reflective voice is the plain one with the sentence taken away. |
| `bn.build.29` | R | MEDIUM | `Some days gone, some things done, the week still open.` | Three balanced clauses, the last of which is what keeps a midweek reading from sounding final. |

### weekQuiet, Areas banner, 234 firings a year, 60 lines, 10 listed

**The bench that went from silent to the deepest debt in the corpus in one change, and was
paid in the next.** This family fired zero times in every measurement before the register
pass: all eight of its lines were `[N]`, nothing could ask for the neutral agent register on
this surface, and the realizer answered `NotProducible` every time the rule qualified. 7.4
now marks it unflattering, it takes 240 banner windows a year, and the batch that followed
took it from eight lines to 11.1's hot floor of sixty.

**All sixty are `[N]`, and no line here will ever be anything else.** 7.4 step 1 offers the
neutral agent register as a tier of one, `Realizer.realize` leaves a tier only when nothing
in it can be filled, and not one line in this bench carries a slot. So the neutral agent
tier fills on every firing and the open tier is never reached: a plain, observational or
reflective line written here would be unreachable for the same structural reason the `[N]`
lines used to be. Authoring rule 5 and the engine want the same thing on this one bench.

The first eight rows are the whole approved bench the batch was written to. The last two are
from the batch, and they are here because the bench now reaches a second length band: it is
26 `SHORT` lines and 34 `MEDIUM` ones, and a `MEDIUM` line has to hold the same flatness
over twice the words. What every line in this bench has to be: no second person, no verb the
reader could have performed, and a word in every one that keeps the week open.

| key | tag | band | line | what the voice carries |
|---|---|---|---|---|
| `bn.quiet.01` | N | SHORT | `A quiet week so far.` | `so far` does all the work, turning a state into a point in a week that is still running. |
| `bn.quiet.02` | N | SHORT | `Not much has moved this week.` | Things move or do not move. The person is not in the sentence at any point. |
| `bn.quiet.03` | N | SHORT | `Still, so far.` | Three words. `still` as an adjective describing the week, never as an adverb about a person. |
| `bn.quiet.04` | N | SHORT | `The week has been quiet here.` | `here` narrows the claim to this screen rather than letting it reach a life. |
| `bn.quiet.05` | N | SHORT | `Little movement this week.` | Four words and no verb anybody could have performed or failed to perform. |
| `bn.quiet.06` | N | SHORT | `Nothing completed yet this week.` | The closest this bench comes to saying zero, and `yet` is the whole reason it can. |
| `bn.quiet.07` | N | SHORT | `A slow week here.` | `slow` rather than bad, empty or wasted, and it is the week that is slow. |
| `bn.quiet.08` | N | SHORT | `Quiet, for now.` | Three words, and `for now` is the argument for this family being sayable at all. |
| `bn.quiet.25` | N | MEDIUM | `The week has run quietly, with days still to come.` | The `MEDIUM` shape: a flat clause, a comma, and a second clause whose only job is to stop the first one sounding final. |
| `bn.quiet.48` | N | MEDIUM | `Things have stayed where they are this week.` | Things stay. Nobody kept them there, and the sentence has no room for anyone who might have. |

### weekStrong, Areas banner, 65 firings a year, 62 lines, 10 listed

| key | tag | band | line | what the voice carries |
|---|---|---|---|---|
| `bn.strong.01` | P | SHORT | `A strong week so far.` | `so far` again, so a good week is never closed off early either. |
| `bn.strong.02` | O | SHORT | `Ahead of your usual pace.` | Compares to the person's usual, never to anybody else's. |
| `bn.strong.03` | P | SHORT | `A lot has moved this week.` | Six words, and `a lot` is deliberately unquantified. |
| `bn.strong.04` | O | MEDIUM | `More than most weeks, and it is not over.` | The clause after the comma is what keeps this from being a verdict. |
| `bn.strong.05` | P | SHORT | `Picking up pace.` | Three words, present participle, and nothing about who is picking it up. |
| `bn.strong.06` | N | SHORT | `{n} through already.` | Three words. `already` is the only place this surface comes close to enthusiasm. |
| `bn.strong.07` | O | SHORT | `Your busiest week in a while.` | A superlative bounded by `in a while`, which makes it checkable and modest. |
| `bn.strong.08` | P | SHORT | `Plenty done this week.` | Four words. Compare this to what another app would write about a good week. |
| `bn.strong.09` | R | SHORT | `More than usual.` | Three words, and the whole fact. `usual` is this person's own recent weeks and nobody else's. |
| `bn.strong.15` | R | MEDIUM | `Plenty already, with the week still open.` | `already` and `still open` in one line, which is this bench refusing to close a good week early. |

---

## The families that had almost nothing to say

**This table is empty, and the last row to leave it is the one worth reading.**

`comebackPattern` fired seventy times a year against **one** sayable line. Three of its five
lines made a claim no fact supports and are held out by `SlotBindings.EXCLUDED`, and the
fourth needed a marker nothing binds, so a person whose areas move in bursts read
`{areaName} moves in bursts, with weeks of nothing between them` every time the pattern
fired, for as long as they used the app. A bench of one cannot avoid repeating: 7.6 excludes
a variant used inside ninety days and then reuses an exhausted bench without the line seen
most recently, and with one line there is no other line. It carries sixty now.

Three benches left this table before it. `reportedVsActual` fired 173 times a year against 2
sayable lines and now carries 60; `areaRevival` fired 219 times against 7 and now carries 80;
`growingQueues` and `consistentRhythm` each had 7. **Not one of them was fixed by binding a
marker.** Every one was grown inside the facts it already had, which is why
`reportedVsActual` never names the subject it is about and why `comebackPattern` never says
how long a silence lasted.

The full list is in `CorpusGateBaseline.UNRENDERABLE`, which records eighty six keys with
the marker that stops each one; the render gate measures eighty two of them as unrenderable
today, so four have become fillable since the map was written and the map is the wider net
of the two. They are waiting on a binding rather than on an author, and **no line phase 9
wrote is among them.**
