# Google Play listing

Every string here is the copy to paste into Play Console. Nothing in this file is a
placeholder. Character counts are stated where Play enforces one.

Follow `ADDENDUM_01_EXECUTIVE_FUNCTION.md` Step 10 exactly: **keywords are permitted,
claims are not.** The distinction is what Play's health policy turns on.

---

## Title

30 characters maximum. This is 32, so the shipped title is the second line.

```
Clarity Now: One Thing at a Time
```

**Shipped title, 30 characters:**

```
Clarity Now: One Thing First
```

Verified: `Clarity Now: One Thing First` is 28 characters. The longer form is kept above
because it is the phrase used everywhere else and a future title limit change should
restore it.

## Short description

80 characters maximum. The requested line is 88, so it is trimmed to the same meaning.

```
One active thing per area. Everything else waits. Free, private, no account.
```

76 characters. Verified by count.

## Long description

```
Most task apps hand you a list of forty things and ask you to pick. Picking is the
hard part.

Clarity Now gives each area of your life one active item. Everything else waits in a
queue behind it. You only ever see what's next.

Built for people who find long lists paralyzing. Designed with ADHD, autism, and brain
fog in mind.


HOW IT WORKS

Areas are the parts of your life you want to keep track of. Work, Health, the move,
whatever they are. Each one holds exactly one active item.

Swipe an area card right to complete what is on it. The next thing in that area's queue
takes its place. Swipe left to swap in something else instead.

Everything you are not doing right now sits in a queue you do not have to look at.


FOCUS SESSIONS

Put one item on a timer with nothing else on the screen. Add time without stopping.
End early whenever you want: a session that ran for six minutes is recorded as six
minutes of focus, and the app does not comment on it.


ONE QUESTION A DAY

The Pulse is a single observation about what you have actually been doing, and one
question with two answers. One tap answers it. You can turn it off.


A WEEKLY PAGE

Every Sunday the app writes a page about your week from what is in your own log. It is
written on your phone, and it says what happened. It never invents anything and it
never grades you.


WHAT THIS APP DOES NOT DO

No account. You never sign in.
No subscription, no in-app purchases, no ads.
No tracking, no analytics, no crash reporting.
No streaks, no scores, no badges, nothing to keep up.
No internet permission at all. Not "we promise not to". The permission is absent from
the app, and you can check that yourself in Android settings.
No AI, no machine learning, no cloud anything.

Leaving for a month costs nothing. There is nothing here that can break.


YOUR DATA

Everything lives in one file on your phone. Export it whenever you like, encrypted with
a password or not, and import it back on any device. That is the whole backup story and
there is no other copy anywhere.

Clarity Now is free and open source under the AGPL-3.0.


Clarity Now is a productivity tool. It does not provide medical advice, diagnosis or
treatment.
```

## Keywords for the listing and store metadata

Permitted by Step 10, and used in the long description above or in this metadata block:

```
ADHD, autism, executive function, executive dysfunction, task paralysis, time blindness,
brain fog, neurodivergent, overwhelm, procrastination, focus, one thing at a time
```

## Forbidden anywhere in the listing, the app or any marketing

```
treats, manages, cures, therapy, therapeutic, clinically proven, medically, symptoms,
diagnosis, disorder used as a claim, and any statement that the app improves or reduces
anything clinical
```

Checked against the long description above: none of these appears. `ADHD`, `autism` and
`brain fog` appear once each, as the audience the app was designed with in mind, which is
the exact phrasing Step 10 gives as permitted.

## Required disclaimer

Present verbatim at the end of the long description, and also in the About screen:

```
Clarity Now is a productivity tool. It does not provide medical advice, diagnosis or
treatment.
```

## Category and content rating

- Category: Productivity
- Content rating: Everyone
- Contains ads: No
- In-app purchases: No
- Data safety: no data collected, no data shared. The app declares no `INTERNET`
  permission, which is verified on every build by `verifyNoInternetPermission` against
  the merged manifest rather than the source.

## Health Apps Declaration

Complete it in Play Console. Zero data collection, no accounts, no health data access and
no network permission should certify cleanly. **Verify the current requirements in Play
Console rather than trusting this note**: the policy changed through 2025 and adds medical
device labeling in January 2026.

## Screenshots

Real captures from a Pixel 8, committed under `docs/screenshots/`. Never mockups.

| file | caption |
|---|---|
| `01-areas-light.png` | One active thing per area. Everything else waits. |
| `02-area-sheet.png` | Open an area to see its queue, and make any of it active in one tap. |
| `03-momentum.png` | What you actually did, counted. Nothing scored. |
| `04-report.png` | A page about your week, written on your phone every Sunday. |
| `05-trail.png` | Everything you have done, back to the day you installed it. |
