# The Play Store listing

**Everything in a fenced block below is final text to be pasted, not a draft to be
improved in the console.** Every sentence was checked against the vocabulary rules in
section 1, against `ADDENDUM_01_EXECUTIVE_FUNCTION.md` Step 10, and against this
repository's language gate. Words typed into a Play Console text box are outside every
check this project has.

`StoreTextTest` holds the three claims this file makes about the app: that the
disclaimer is character for character `R.string.about_disclaimer`, that no forbidden
word appears anywhere in it, and that the title and short description are inside Play's
limits. It also fails if a second listing file appears, which is how the two that
existed until 0.14.0 came to disagree.

The steps for filing it need a Play Console account and are the owner's, in
`HANDOFF.md`.

---

## 1. The rule this listing turns on

**Use the words people search for. Do not make a claim.** Play's health policy triggers
on a claim, not on a keyword. `Built for people who find long lists paralyzing` is a
keyword sentence and is permitted. `Reduces task paralysis` is a claim and is not.

The difference is whether the sentence says something about **the app** or something
about **the person after using the app**. Say what the app is. Never say what it does to
anyone.

**Words that may appear.** ADHD, autism, executive function, executive dysfunction, task
paralysis, time blindness, brain fog, neurodivergent, overwhelm, procrastination, focus,
one thing at a time.

**Words that may never appear.** treats, manages, cures, therapy, therapeutic, clinically
proven, medically, symptoms, diagnosis, disorder used as a claim, and any sentence saying
the app improves or reduces anything clinical.

`diagnosis` appears exactly once in the permitted text below, inside the required
disclaimer, where it is a denial rather than a claim. That is the only instance allowed,
and `StoreTextTest` allows it only there.

Note that **`manages` is forbidden and is also the most ordinary verb in this app's
category.** Every phrase of the form `manage your tasks` had to be written another way.
Anything added later has to clear the same bar.

---

## 2. Store presence

| field | value |
|---|---|
| Package | `com.kamsiob.claritynow` |
| Category | Productivity |
| Tags | `To-do lists`, `Notes`, `Planning`. Task management is not selectable without the forbidden word |
| Content rating | Everyone. No user generated content, no sharing, no ads, no purchases |
| Contains ads | No |
| In-app purchases | No |
| Target audience | 18 and over. Not designed for children, no child directed content |
| Free or paid | Free |
| Privacy policy URL | See section 8 |

---

## 3. Title

30 characters maximum. **This is the shipped title, 28 characters:**

```
Clarity Now: One Thing First
```

`Clarity Now: One Thing at a Time` is the phrase used everywhere else in the product and
is 32 characters, which is two over. It is recorded here so that a future limit change
restores it rather than reinventing it.

---

## 4. Short description

80 characters maximum. **Final, 76 characters:**

```
One active thing per area. Everything else waits. Free, private, no account.
```

**Considered and rejected.** `The to do app that shows you one thing` is shorter and says
nothing about who it is for. `Built for ADHD, autism and executive dysfunction` front
loads the keywords but reads as a category the app belongs to rather than a description
of the app, and listings written that way get skimmed past by the people who do not use
those words about themselves. The chosen line describes the mechanism and names the
price, the privacy and the absence of an account, which are the three things this
audience checks first.

---

## 5. Full description

4000 characters maximum. Paste exactly this, including the line breaks.

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


ON YOUR HOME SCREEN

Six widgets, because a widget cannot be swiped away the way a notification can. Next Up,
First Step, Quick Capture, Focus Countdown, All Areas and Rhythm. Three shortcuts on the
app icon and a quick settings tile, so a session can start without opening the app.


BUILT TO BE READABLE

Large text support to 200 percent without clipping, generous touch targets, full
TalkBack labels on every control, a reduce motion setting the app always respects, and a
calm mode that takes the color down without taking the meaning out.


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

**Two sections were added to the owner's copy** when the two listing files were merged
in 0.14.0: `ON YOUR HOME SCREEN` and `BUILT TO BE READABLE`. Both describe things the app
does and neither says anything about a person. Strike either one if it is not wanted;
nothing else in the listing depends on them.

The closing sentence is character for character identical to `R.string.about_disclaimer`.
**If one is ever edited the other has to be edited in the same commit**, and
`StoreTextTest` fails until it is.

---

## 6. Health Apps Declaration

Play asks this because the listing contains health adjacent keywords. Every answer is
`no`, and each one is true because of something enforced in this repository rather than
because of a policy the app promises to follow.

| question | answer | why it is true |
|---|---|---|
| Does the app access health or fitness data? | No | No Health Connect dependency, no sensor permission, no permission of any kind beyond notifications |
| Does it provide medical device functionality? | No | No measurement of any physiological quantity |
| Does it make claims about a health condition? | No | Section 1 above, enforced by `StoreTextTest` |
| Does it handle sensitive health information? | No | No network permission and no account, so nothing leaves the device |
| Is it intended for clinical use? | No | It is a productivity tool, stated in the listing and in About |

**Verify the current form in Play Console rather than trusting this table.** The policy
moved through 2025 and adds medical device labeling in January 2026. If the console asks
a question this table does not have, answer it from the facts and add the row here.

---

## 7. Data Safety form

Every answer is `No data collected`. The form has a `no data collected` path; take it.

| section | answer |
|---|---|
| Does your app collect or share any of the required user data types? | No |
| Is all of the user data encrypted in transit? | Not applicable, no data leaves the device |
| Do you provide a way for users to request that their data is deleted? | Yes, Erase all data in Settings, and uninstalling |
| Data types collected | None |
| Data types shared | None |

The app declares no permission that would make any other answer possible.
`verifyNoInternetPermission` fails the build if a network permission ever reaches the
merged manifest of either variant, so this form cannot silently go out of date.

---

## 8. Privacy policy URL

Play requires a URL, not in-app text. The policy is `PRIVACY.md` in this repository, so
one already exists:

```
https://github.com/kamsiob/clarity-now/blob/main/PRIVACY.md
```

`PRIVACY.md` is generated from the app's own strings and `StoreTextTest` fails if the two
ever differ, so the hosted policy and the one in the app cannot come apart.

**If a page under `kamsiob.com` is preferred**, which is what the note above the privacy
strings in `strings.xml` anticipates, publish that same text there and put that URL here
instead. Whatever is used, the two have to match.

---

## 9. Graphics

All of it is generated by `store-assets/make_assets.py` from this repository's own
sources, so a color that moves in the app moves here on the next run. Nothing is drawn
by hand.

| asset | file | notes |
|---|---|---|
| App icon | `store-assets/icon-512.png` | 512 by 512. The same three rounded rectangles `ic_launcher_foreground.xml` declares, on `ic_launcher_background` |
| Feature graphic | `store-assets/feature-graphic-1024x500.png` | 1024 by 500, the app's own two fonts, 93px clear on each side because Play crops the edges on some surfaces |
| Screenshots | `store-assets/screenshots/*.png` | 1150 by 2300, exactly 2:1 |

**The screenshots are the committed captures with the phone's own status bar removed.**
That takes the owner's clock, battery and every other app's notification icons out of the
listing, and it is what brings them inside Play's aspect ratio cap: the raw captures are
1080 by 2400, which is 2.22 to 1. The **width** is padded in the page's own ground color
rather than the height cropped, because cropping to the same ratio would take the tab bar
off the bottom. Every pixel of the app is still there.

| file | caption |
|---|---|
| `01-areas-light.png` | One active thing per area, and how much is waiting behind it. |
| `02-area-sheet.png` | Open an area to see its queue, and make any of it active in one tap. |
| `03-momentum.png` | What you actually did, counted. Nothing scored. |
| `04-report.png` | A page about your week, written on your phone every Sunday. |
| `05-trail.png` | Everything you have done, back to the day you installed it. |

All five come from one state rather than five unrelated ones, and retaking them is
reproducible: the fixture and the two commands are in `HANDOFF.md`.

---

## 10. Before submitting, check

- [ ] The full description was pasted, not retyped, and no console spell checker changed
      an American spelling
- [ ] The disclaimer in the listing is character for character the one in About
- [ ] No word from section 1's forbidden list appears anywhere, including the screenshot
      captions and the feature graphic
- [ ] Screenshots are captures from a real device, not mockups
- [ ] The privacy policy URL resolves and its text matches the in-app sheet
- [ ] Contains ads is No and in-app purchases is No
- [ ] The six links in `ClarityLinks` all resolve, including the support one
