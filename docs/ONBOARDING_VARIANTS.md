# Onboarding copy, three variants and the one that shipped

Part 3 of the polish pass asked for three tone variants, argued for by three different
members of the team, with the least marketing-sounding one chosen and then tested against
a focus group. This file is the record.

## The five things onboarding has to convey, without jargon

1. Most task apps give you an infinite list and ask you to prioritize. Prioritizing is the
   part that is hard. This app removes that step.
2. One thing per area is active. Everything else waits. You cannot accidentally see forty
   things at once.
3. Nothing here can break. No streaks, no scores, no badges. Leaving for a month costs
   nothing.
4. It notices patterns in what you actually do and says them plainly. It never invents
   anything and never judges.
5. Nothing leaves your phone. No account, no internet permission at all.

## The tone rules these were written against

Describe the problem, never the person. Never explain someone to themselves. No
inspiration, no encouragement. Never imply the person is broken or that the app fixes
them. Never claim to treat, manage or help with any condition. Do not use the word
neurodivergent. Plain short sentences, written for somebody reading at low capacity. Say
what the app does not do as clearly as what it does.

---

## Variant A, argued by the UX writer: the plain instrument

Neutral and factual throughout. Says what the thing does and stops. No metaphor, no
reassurance, nothing that could be read as a promise.

| slot | copy |
|---|---|
| beat 1 line | Each area holds one item. The rest wait in line behind it. |
| beat 2 question | How do you want to start? |
| just start | One area called Today. Names and colors can come later. |
| pick areas | Two to four parts of your life to keep track of. |
| beat 3 line | That is the whole setup. |
| beat 4 opener | Every area holds one thing. The rest wait. |
| beat 4 second | Two more things worth knowing. |
| Pulse caption | One question a day. One tap. You can turn it off. |
| Momentum caption | What you did, counted. Nothing scored. |
| Report caption | Every Sunday, a page about your week. Written on this phone. |

**The writer's case.** Every line is checkable. Nothing here can be experienced as a
promise the app then fails to keep, which matters most for the person who has already been
let down by four other apps. The register is the same one the rest of the interface
speaks, so onboarding is not a different product from the app it introduces.

**Against it.** It never says the thing that would actually make somebody stay, which is
that this one cannot make them feel behind.

## Variant B, argued by the clinical psychologist: the relief

Same facts, ordered so the first thing a person learns is what cannot go wrong.

| slot | copy |
|---|---|
| beat 1 line | Each area holds one item. The rest wait in line behind it. |
| beat 3 line | Nothing here can break. |
| beat 4 opener | Most lists ask you to choose. This one already did. |
| beat 4 second | Two more things, then you are done. |
| Pulse caption | One question a day. One tap. Skip as many as you like. |
| Momentum caption | A record, not a scorecard. |
| Report caption | Every Sunday, what the week actually held. |

**The psychologist's case.** The single largest reason this audience abandons a
productivity app is the moment it makes them feel behind, and that moment usually arrives
in week three. Saying "nothing here can break" on day one is the only line in any variant
that pre-empts it, and it is a fact rather than a reassurance: there is no streak to lose
and no score to drop.

**Against it.** `A record, not a scorecard` and `Most lists ask you to choose. This one
already did.` are both the X-not-Y shape, which is the cadence of advertising. Two of them
in four screens is a voice.

## Variant C, argued by the product critic: the contract

Leads with the refusals, on the grounds that this audience has been sold to before and is
right to be suspicious.

| slot | copy |
|---|---|
| beat 3 line | No account. No internet. No streaks. |
| beat 4 opener | There is nothing to keep up. |
| beat 4 second | Two more things. |
| Pulse caption | One question a day. One tap. Off in Settings. |
| Momentum caption | Counted, never scored. |
| Report caption | Every Sunday. Written here, sent nowhere. |

**The critic's case.** Everything else is a claim about how the app will feel. These are
the only statements a person can verify, two of them in Android settings within a minute.
An app that opens by telling you what it will not do has spent its first screen on the
only currency it has.

**Against it.** Three sentence fragments in a row is a slogan, and a first run that opens
on negatives reads as defensive, which is its own kind of pitch. It also buries the
mechanic, which is the thing a person actually has to understand to use the app at all.

---

## What shipped, and why

**Variant A, with exactly one line taken from B.**

A is chosen because the brief's test is "the one that sounds least like marketing", and
that is a test about *shape* rather than about sentiment. B and C both reach for the two
constructions advertising is built from: the antithesis (`a record, not a scorecard`,
`counted, never scored`) and the triad (`No account. No internet. No streaks.`). A uses
neither anywhere. Read aloud, A is the only one of the three that sounds like a person
explaining a tool rather than a brand introducing itself.

**The graft is `Nothing here can break.`** It replaces A's `That is the whole setup.` at
beat 3. Two reasons it survives the same test that removed the rest of B. It is a plain
declarative with no antithesis in it, and it is **a fact rather than a promise**: the app
has no streak, no score and no badge, so there is literally nothing in it that a person
can lose. It is also item 3 of the five things this sequence must convey, and A had no
line carrying it.

**One line from C is kept in substance and not in shape.** The privacy fact is required,
and C's version is a triad. It moves to the Report caption instead, where `Written on this
phone` says the same thing in the same breath as something a person cares about more.

Beat 1 and beat 2 are unchanged in all three variants because `MASTER_BUILD_PROMPT` 13.1
states their copy verbatim and the fork's two panels are deliberately written to the same
length and shape, which is half of what makes `Just start` a genuine equal rather than an
escape hatch.


---

## What focus group B changed, after the fact

All three variants above kept beat 1's line unexamined, because
`MASTER_BUILD_PROMPT` 13.1 stated it verbatim and this exercise was about the lines that
were open. Focus group B was not bound by that and named it twice: once under
condescension and once under **felt described**, which is the failure the whole tone brief
exists to prevent.

> `One thing at a time` is the phrase used to talk somebody down, and `The next one is
> ready when you are` presupposes that the reader often is not.

Two of the six had heard that register immediately before a streak in some other app.
Susan, who does not think of herself as having any difficulty at all, heard an app
assuming she was flustered.

It also carried none of the mechanic. The word queue appears in no onboarding string, and
beat 1 draws four cards each holding one line with nothing visibly behind them, so a person
could finish the entire sequence able to say that one thing is active and unable to say
where everything else goes. The replacement, `Each area holds one item. The rest wait in
line behind it.`, says the mechanic and makes no claim about the reader.

The specification is amended in place rather than carrying both statements.

Group B also removed three lines that had survived the variant test above by being inside
the chosen variant rather than by passing it: `What you did, counted. Nothing scored.` is
the X-not-Y antithesis that `A record, not a scorecard` was rejected for, and
`One question a day. One tap. You can turn it off.` is the three fragment triad that
`No account. No internet. No streaks.` was rejected for. Choosing a variant is not the same
as auditing it, and the audit found the same two shapes had walked back in.
