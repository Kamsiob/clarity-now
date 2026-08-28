# The Play Store listing

Phase 13. Written to `MASTER_BUILD_PROMPT.md` 16.11, which is the section of this
project where a mistake gets the app removed rather than reviewed.

**Everything below is final text to be pasted, not a draft to be improved in the
console.** Every sentence here was checked against the forbidden list in 16.11 and
against the language gate. Editing it in the Play Console text box puts words into the
listing that no gate in this repo can see.

The steps for actually filing it are in `HANDOFF.md`, because they need a Play Console
account and are the owner's to perform.

---

## 1. The rule this listing turns on

**Use the words people search for. Do not make a claim.** Google Play's health policy
triggers on a claim, not on a keyword. `Built for people who find long lists paralyzing`
is a keyword sentence and is permitted. `Reduces task paralysis` is a claim and is not.

The difference is whether the sentence says something about **the app** or something
about **the person after using the app**. Say what the app is. Never say what it does
to anyone.

### Words that may appear

ADHD, autism, executive function, executive dysfunction, task paralysis, time
blindness, brain fog, neurodivergent, overwhelm, procrastination, focus, one thing at
a time.

### Words that may never appear

treats, manages, cures, therapy, therapeutic, clinically proven, medically, symptoms,
diagnosis, disorder used as a claim, and any sentence saying the app improves or
reduces anything clinical.

`diagnosis` appears exactly once in the permitted text below, inside the required
disclaimer, where it is a denial rather than a claim. That is the only instance
allowed.

Note that **`manages` is on the forbidden list and is also the most ordinary verb in
this app's category.** Every phrase of the form `manage your tasks` had to be written
another way. Anything added to this listing later has to clear the same bar.

---

## 2. Store presence

| field | value |
|---|---|
| App name | `Clarity Now` |
| Package | `com.kamsiob.claritynow` |
| Category | Productivity |
| Tags | Task management is not selectable without the word; choose `To-do lists`, `Notes`, `Planning` |
| Content rating | Everyone. No user generated content, no sharing, no ads, no purchases |
| Contains ads | No |
| In-app purchases | No |
| Target audience | 18 and over. Not designed for children, no child directed content |
| Free or paid | Free |

---

## 3. Short description

80 characters maximum. This is the line under the icon in search results and it is the
only copy most people will ever read.

**Final:**

```
One thing at a time. A queue based to do app for brains that stall on lists.
```

75 characters.

**Considered and rejected.** `The to do app that shows you one thing` (38) is shorter
and clearer but says nothing about who it is for, which is the entire positioning.
`Built for ADHD, autism and executive dysfunction` (47) front loads the keywords but
reads as a category the app belongs to rather than a description of the app, and
listings written that way get skimmed past by the people who do not use those words
about themselves. The chosen line describes the mechanism first and names the audience
by the problem rather than by the label, which reaches both.

---

## 4. Full description

4000 characters maximum. Paste exactly this, including the line breaks.

```
Most to do apps show you everything you have to do. If long lists stall you rather than
help you, that is the whole problem, and adding features to the list does not fix it.

Clarity Now works the other way around. Your life has a small number of areas of focus.
Each area has exactly one active item. Everything else waits in a queue behind it, out
of sight. Finish the active item and the next one steps forward on its own.

That is the entire mechanic. There is nothing to configure before it works.


ONE THING AT A TIME

Open the app and you see one item per area, and nothing else. No inbox count, no
overdue red, no list of forty things you have not done. Reordering the queue is a swipe.
Swapping what is active is a swipe. Adding something takes one tap and one line of text,
and you never have to say when it is due or how important it is.


FOCUS SESSIONS

Pick an item, pick a length, and the screen goes dark and quiet for the duration. It
shows an arc rather than a countdown, because a shape reads before digits do. Ending
early is a normal way to finish a session, not a failure, and the app records it that
way.


THREE QUIET REFLECTIONS

Clarity Pulse asks you one short question a day about something it noticed in your own
history, and takes one tap to answer.

Momentum is a calm mirror of the last two weeks. It counts days you did something. It
never counts a streak, because a streak turns one missed day into a reason to stop.

The Clarity Report is a short weekly narrative about your own week, in plain sentences.

Every sentence in all three comes from arithmetic over your own event log and a library
of hand written lines. There is no model on your phone and no call to one anywhere else.


WIDGETS THAT DO THE REMEMBERING

Six home screen widgets, because a widget cannot be swiped away the way a notification
can. Next Up, First Step, Quick Capture, Focus Countdown, All Areas and Rhythm. Three
app shortcuts and a quick settings tile for starting a session without opening the app.


BUILT FOR PEOPLE WHO FIND LONG LISTS PARALYZING

Designed with ADHD, autism, executive dysfunction, task paralysis, time blindness and
brain fog in mind. Every screen is built so that the next action is obvious and the
amount of unfinished work is not on display. Large text, generous touch targets, full
TalkBack labels, a reduce motion setting the app always respects, and a calm mode that
takes the color down without taking the meaning out.


WHAT THIS APP DOES NOT DO

These are checkable facts, not promises.

No internet permission at all. Open Android settings, look at this app's permissions,
and there is no network access listed. It cannot send your data anywhere because the
operating system will refuse.

No account. No sign in. No cloud. No sync server.

No analytics, no telemetry, no crash reporting, no advertising, no third party SDKs.

No subscription, no in app purchase, no locked features, no upgrade prompt. Every
feature is available to everyone immediately.

No streaks, no badges, no points, no red numbers, no guilt.

Your areas, items, history and reports live in one file inside this app's private
storage. Erase all data in Settings removes it permanently, and so does uninstalling.
Backups are yours to make and yours to keep.

Open source, AGPL-3.0.


Clarity Now is a productivity tool. It does not provide medical advice, diagnosis or
treatment.
```

The closing sentence is verbatim from 16.11 and is character for character identical to
`R.string.about_disclaimer` in the app. **If one is ever edited the other has to be
edited in the same commit.**

---

## 5. Health Apps Declaration

Play Console asks this because the listing contains health adjacent keywords. The
answers below are all `no`, and each one is true because of something enforced in this
repository rather than because of a policy the app follows.

| question | answer | why it is true |
|---|---|---|
| Does the app access health or fitness data? | No | No Health Connect dependency, no sensor permission, no permission of any kind beyond notifications |
| Does it provide medical device functionality? | No | No measurement of any physiological quantity |
| Does it make claims about a health condition? | No | Section 1 above, enforced by review of this file |
| Does it handle sensitive health information? | No | The app has no network permission and no account, so nothing leaves the device |
| Is it intended for clinical use? | No | It is a productivity tool, stated in the listing and in About |

**Verify the current form in Play Console rather than trusting this table.** 16.11 says
the policy moved through 2025 and added medical device labeling in January 2026, and
`MASTER_BUILD_PROMPT.md` 3.3 applies to a policy exactly as it applies to a library
version. If the console asks a question this table does not have, answer it from the
facts and add the row here.

---

## 6. Data Safety form

Every answer is `No data collected`. The form has a `no data collected` path; take it.

| section | answer |
|---|---|
| Does your app collect or share any of the required user data types? | No |
| Is all of the user data encrypted in transit? | Not applicable, no data leaves the device |
| Do you provide a way for users to request that their data is deleted? | Yes, Erase all data in Settings, and uninstalling |
| Data types collected | None |
| Data types shared | None |

The app declares no permission that would make any other answer possible. The build
fails if an internet permission ever reaches the merged manifest, so this form cannot
silently go out of date.

---

## 7. Before submitting, check

- [ ] The full description was pasted, not retyped, and no console spell checker
      changed an American spelling
- [ ] The disclaimer sentence in the listing is character for character identical to
      the one in About
- [ ] No word from the forbidden list appears anywhere in the listing, including the
      screenshot captions and the feature graphic
- [ ] Screenshots are captures from a real device, not mockups
- [ ] The privacy policy URL resolves and its text matches the in app sheet
- [ ] Contains ads is No and in-app purchases is No
- [ ] The Buy Me a Coffee link in the app points at a real page
