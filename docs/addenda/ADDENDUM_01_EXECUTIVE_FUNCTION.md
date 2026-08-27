<!--
    Addendum 01, transcribed into the repository on August 27, 2026.

    This file is provenance, not authority. It records what was asked for on one
    date. What the project will actually build lives in MASTER_BUILD_PROMPT.md,
    design-v3.md, DECISIONS.md and the GitHub issues, which is where a session
    should read from. Where this document and those disagree, those win, and
    DECISIONS.md records why.

    Two kinds of normalization were applied. Nothing else was changed and no
    sentence was reworded. The original is held outside the repository.

    1. STYLE. The source used em dashes and thirteen British spellings, both of
       which verifyLanguageHygiene fails the build on. Em dashes became commas.
       The spellings were nine of the British form of "color", two of "honor",
       and one each of "paralyze" and "behavior", and one of "labeling". Those forms are described here
       rather than quoted, because the gate scans this file too and a quotation
       would fail it.

    2. FILE NAMES. The source named four files under names this repository does
       not use. They were replaced with the real ones so that no alias exists
       anywhere in the repo:

         MASTER_SPEC dot md  ->  MASTER_BUILD_PROMPT.md
         DESIGN dot md       ->  design-v3.md
         tools/audit.py      ->  audit.py

       DECISIONS.md was created under the name the source used, so it needed no
       replacement. The two old names are spelled with "dot" above so that they
       do not read as live references to files that do not exist. See DECISIONS.md
       conflict C1 for the reasoning, and note that audit.py previously carried a
       hard-coded whitelist for one of them, which has been removed.
-->

# Addendum 01: Executive Function Support

**Status:** Directive. Read fully before acting.
**Supersedes:** nothing. Adds to `MASTER_BUILD_PROMPT.md`, `design-v3.md` and the corpora.
**Date raised:** this addendum was produced from research and user-panel work on serving people with executive function challenges.

---

## STOP

**Do not write any implementation code in response to this document.**

This adds a substantial set of changes to Clarity Now, coming out of research on serving people with executive function challenges: ADHD, autism, brain fog from long COVID or ME/CFS, cognitive changes in perimenopause, TBI recovery, depression and anxiety, and burnout.

**None of this is a rebuild.** The core mechanic is unchanged and is already well suited to this audience. These are additions and refinements.

Your first task is record-keeping, not building. Everything below must be written into the project's permanent documents and GitHub issues before any of it is implemented, so that a disconnected session or a cold start picks up exactly where this left off. **Treat the record as the deliverable for this document.**

---

## STEP 1: RECORD EVERYTHING. DO THIS FIRST AND COMPLETELY.

**1a.** Add a section to `MASTER_BUILD_PROMPT.md` titled "Executive function support" covering every functional change below, written into the phase plan at the phase where each belongs. Anything not yet built is marked pending, never described as built.

**1b.** Add to `design-v3.md` every visual, motion and language change below, including calm mode, the analog countdown, the platform-first rule, the Live Updates surface, and the widget family.

**1c.** Add to `DECISIONS.md` a dated entry explaining **why** this direction was taken, the market and evidence reasoning, and the four things deliberately ruled out in Step 9. Future sessions must be able to see the reasoning, not only the instruction.

**1d.** Open a GitHub issue for every numbered item in Steps 3 through 8. Label them. Reference the phase each belongs to. These issues are the authoritative record of what remains.

**1e.** Confirm back to the owner, in plain language, what you recorded and where, before you build anything.

---

## STEP 2: SCHEMA CHANGES. URGENT. BEFORE ANY USER DATA EXISTS.

Event payload changes are nearly free now and painful later. Do these in the current phase regardless of where the matching UI lands.

**2a.** `ITEM_ADDED` payload gains two optional nullable fields:
```
estimateMinutes: Int?
firstStep: String?
```

**2b.** New event `ITEM_ESTIMATED`, so an estimate can be added or changed after creation without editing history.
```
payload: itemId, previousEstimateMinutes, newEstimateMinutes
```

**2c.** Items gain an unfiled state. An item may exist with `areaId = null`. `ITEM_ADDED`'s `areaId` becomes nullable. New event `ITEM_FILED` moves an unfiled item into an area.
```
payload: itemId, areaId, orderKey, areaNameSnapshot
```

**2d.** New event `APP_OPENED` recording the date key only. No time, no count. This is what makes gap detection possible without any tracking. Written at most once per calendar day.

**2e.** New event `FOCUS_EXTENDED` for adding time to a running session.
```
payload: sessionId, addedSeconds, newPlannedSeconds
```

**2f.** Add all of these to `docs/EVENT_FORMAT.md` and to the golden fixture. The replay harness must cover unfiled items, the filing transition, and session extension.

---

## STEP 3: PLATFORM FIRST. THIS GOVERNS EVERYTHING BELOW.

A standing architectural rule, not a preference. Record it in `design-v3.md` and `MASTER_BUILD_PROMPT.md` and apply it to every subsequent decision.

### 3a. Google's components and assets are the default. Custom is the fallback, not a failure.

**Check for a platform solution first. Use it when it fits. Build custom when it does not.**

In order:

1. The official Material 3 Expressive component, unmodified
2. The official component, themed with our tokens
3. The official component, extended
4. **Custom**, a legitimate and expected outcome when steps 1 to 3 do not fit

Reasons the platform comes first: platform components get correct accessibility, RTL, dynamic type, predictive back and motion physics for free; they are hardware-accelerated and tested at a scale we cannot match; and they keep the codebase small enough for one person to maintain.

**When to go custom, without hesitation:**

- **No platform equivalent exists.** Several things in this app have none: the depleting focus ring, the week ribbon in the Report, the 14-day rhythm dot row, the area color wash, the two-stage color picker, the tutorial spotlight. Do not contort a platform component into these shapes. Build them properly
- **The platform component carries meaning the app rejects.** Anything implying achievement, scoring, celebration or progress toward a target is wrong here regardless of how well it is built
- **The platform component fights a design rule.** If using it would require two separation devices on one element, a colored edge treatment, or an all-caps label, the rule wins
- **The platform component is worse for this audience.** Excessive motion or saturation that cannot be tamed through theming is a reason to build something calmer

**What is not a reason to go custom:** preferring the look of something you would draw yourself. That instinct belongs in the polish pass described in 3c, which works through theming rather than replacement.

**Record the decision either way.** When you go custom, note in `DECISIONS.md` what you checked and which of the four reasons above applied. One line is enough. This exists so a later session knows the platform was considered and does not redo the analysis, not to discourage building what the app needs.

**Custom components inherit the platform's obligations.** Anything you build yourself must handle accessibility, RTL, dynamic type, reduce-motion and calm mode to the same standard the platform component would have. That work is the real cost of going custom, and it is why the platform comes first when it fits.

### 3b. Specific platform sources to use

- **Material 3 Expressive components** from `androidx.compose.material3`, including the expressive motion physics system and the shape morphing library, rather than hand-rolled springs
- **Material Symbols for iconography wherever a symbol fits.** The custom SVG icons in the visual reference mocks are illustrative only and are not shipping assets. Map each one to its Material Symbols equivalent and record the mapping in `design-v3.md`. Where no symbol carries the right meaning, draw one in the same style, weight and optical size as the set around it, and note it in the mapping table as custom. A drawer of mismatched stock icons is worse than one well-drawn addition
- **androidx.glance** for all widgets
- **`Notification.ProgressStyle`** for Live Updates, per Step 5
- **androidx.core shortcuts APIs** for app shortcuts
- **The platform `TileService`** for the quick settings tile
- **Google Fonts** for both typefaces, downloaded and bundled as files in `res/font`. **Never the Downloadable Fonts API**, which fetches over the network, and this app has no `INTERNET` permission

### 3c. The polish layer sits on top, not instead

After the Material 3 Expressive layer is working and verified on device, a separate pass adds the distinctive Kamsiob look. That pass works through theming, tokens, typography, spacing and considered motion choices.

The distinction that matters: **3a governs what to build when the platform has no answer. 3c governs what not to rebuild when it does.** A polish pass should not reimplement a working platform component to change its appearance, because theming can almost always get there. If a polish idea genuinely cannot be reached through theming, raise it as a decision rather than doing it silently.

### 3d. Verify versions at build time

Do not trust any library version named in this document or any other. Check the current stable releases and the current recommended integration path before integrating, and record what you chose and why.

---

## STEP 4: FUNCTIONAL ADDITIONS

### 4a. Unfiled inbox, Phase 2

Capture must never require a decision. Adding an item does not require choosing an area. Unfiled items live in an inbox reachable from the Areas screen. Filing is a separate, later, optional act.

An unfiled item can be filed, edited or deleted. It cannot be active or completed until filed.

The inbox count is shown quietly. Never a badge, never a red dot.

### 4b. First step field, Phase 2

One optional line on an item: the first physical action. Shown on the active item card at caption weight when present, hidden when absent. Never required, never prompted for, deletable.

### 4c. Time estimate, Phase 2 capture, Phase 8 observation

Optional estimate on an item. Trail already records when an item became active and when it completed, so the actual comes free. Never a required field. Never shown as a countdown against the item.

### 4d. Re-entry state, detection Phase 2, surface Phase 6

**This is the highest-stakes screen in the app.**

This audience leaves and returns. That is normal use, not failure.

When opened after a gap of 14 or more days since the last `APP_OPENED`, the first screen is a distinct welcome-back state.

- It does **not** state the length of the gap
- It does **not** count anything
- It does **not** ask where the user has been
- It offers to keep everything exactly as it was, and that is the default
- It offers, secondarily, to clear active items and start fresh
- Pulse generates nothing for the first two days back
- The Report suppresses every decline, neglect and gap observation for the first full week back

**A returning user must never be greeted by a measurement of their absence.**

### 4e. Early ending is a success state, Phase 4

A focus session ended early is a completed short session. The completion screen says so. The word "abandoned" appears nowhere the user can see it, including in Trail. Rename the event if necessary.

Fourteen minutes is fourteen minutes.

### 4f. Add time to a running session, Phase 4

An "add ten minutes" control that extends the current session without resetting it or starting a new one. Writes `FOCUS_EXTENDED`. Ending a timer should not have to break flow.

### 4g. Transition warning, Phase 4

Optional, **off by default**: a quiet "five minutes left" signal before a session ends. User-controlled in Settings. Never a full notification unless the app is backgrounded.

### 4h. Backup, export and import, Phase 11

Because everything is local, the user's data has exactly one copy unless they make another. Export is a **safety feature**, not a convenience feature, and must be treated with that seriousness.

- Export writes the **entire** database, all events and all derived state, to one portable file through the Storage Access Framework
- Export offers an **optional password**. If set, the file is encrypted with a key derived from the password using a modern KDF at current recommended parameters. If not set, the file is readable, and the export screen says so plainly rather than implying safety
- The file carries a schema version, a creation date, an item and event count, and a checksum
- Import validates schema version, checksum and the internal integrity of the event log **before touching anything**. A corrupt, truncated or foreign file is refused with one plain sentence saying what was wrong
- Import offers replace or merge. Merge uses the same deterministic event union the sync design already specifies
- A wrong password fails clearly and destroys nothing
- An automated test performs a full round trip, encrypted and unencrypted, and asserts byte-identical state. A second test feeds it deliberately corrupted files and asserts clean refusal
- Settings shows the date of the last export. If more than 30 days have passed **and** real data exists, one quiet line appears in Settings only. Never a notification, never a nag, never a badge

---

## STEP 5: LIVE UPDATES, Phase 4, extended in Phase 12

A focus session is exactly the user-initiated, start-to-end, time-bound task that Android's Live Updates were designed for. On a Pixel this surfaces as a status bar chip that expands; on Samsung devices it appears in the Now Bar. For an audience with time blindness, having the session visible outside the app is not a nicety, it is the point.

### 5a. Implementation

Use the platform API, not a custom foreground notification dressed up to look like one.

- `Notification.ProgressStyle`, introduced in Android 16, API 36, `Build.VERSION_CODES.BAKLAVA`
- Declare `POST_PROMOTED_NOTIFICATIONS` in the manifest
- Check `NotificationManager.canPostPromotedNotifications()` before attempting to post, and degrade silently if false
- Verify current API details and any changes since Android 16 before implementing, per rule 3d

### 5b. What it shows

The area name, the item title, and the remaining time as a progress track that depletes. Use the segment and point features only if they genuinely add clarity; a single depleting track is likely correct here and simpler is better.

If a transition warning is enabled, the track changes state at the five minute mark.

### 5c. Actions

Two at most: **Add 10 min** and **End**. Both must work without opening the app. Tapping the body opens the focus screen.

### 5d. Graceful degradation, required

Below API 36, or where promoted notifications are unavailable or denied, fall back to the existing ongoing notification with a chronometer. The app must be fully usable with no Live Update at all. Never gate a feature behind it and never tell the user their device is missing out.

### 5e. This is the only Live Update the app will ever post

Live Updates are for user-initiated ongoing tasks. Do not use them for Pulse, the Report, reminders, or anything the user did not just start. **Record this constraint in `design-v3.md`** so a later session does not extend it.

### 5f. Respect the existing notification rules

The Live Update is silent, is dismissed when the session ends, and never re-engages. It is not a marketing surface.

---

## STEP 6: WIDGETS. EIGHT SPECIFIED, SIX REQUIRED IN V1., Phase 12

For this audience widgets matter more than notifications, and the reason is specific: **a widget is persistent and cannot be dismissed**, so it works with "out of sight, out of mind" rather than against it. A notification is a one-time event that gets swiped away and forgotten. A widget is still there tomorrow.

The design goal for every widget below is **zero taps to see**, and where an action exists, **one tap to act**.

Build all with `androidx.glance`. All share one design DNA: 16dp padding, serif for the single largest element, sans for everything else, area tints at 3 to 5 percent light and 5 to 7 percent dark, 8dp inner radii, no borders, no colored edges, no badges. Every one must render correctly in dark mode, respect calm mode, scale text without clipping at the smallest grid size, and show a sensible state when its configured area no longer exists.

### Required in v1

**6a. Next Up**, small, 2x2
One active item. Area dot, area name, item title in serif. Below it, a count of what waits behind it. Configurable to a pinned area, or automatic, which shows the least recently touched active area and rotates daily.
Tap opens that area.

**6b. First Step**, small, 2x2
Shows the active item's **first step**, not its title. If no first step is set, shows the title and a quiet prompt to add one.

This exists because the hardest moment is starting, and the title of a task is often the thing that is intimidating about it. *"Rewrite the proposal intro"* is a wall. *"Open the doc and read what's there"* is not. Putting the smallest possible action on the home screen removes the activation barrier at the exact moment it bites.

Tap starts a focus session on that item.

**6c. Quick Capture**, small, 2x2 or 1x1
A single large tap target that opens capture directly into the unfiled inbox, with the keyboard already up and no area to choose.

The whole design principle: the quicker and simpler the action, the smaller the wall. Every decision between the thought and the record is a place the thought is lost.

Shows the inbox count quietly beneath, as plain text, never a badge.

**6d. Focus Countdown**, small, 2x2
Live during a session. An analog depleting arc as the primary carrier with the digits secondary, matching the in-app ring. Time must read as a shrinking shape before it reads as a number.

When no session is running, shows a "Start focus" tap target. Tap during a session opens the focus screen.

Update at a cadence that is visibly alive but does not drain battery; Glance updates are throttled by the system, so choose the cadence deliberately and record the reasoning.

**6e. All Areas**, medium, 4x2
Every non-archived area as a row: dot, name, active item or "Idle". Configurable to all areas or a chosen subset.

Color carries area identity so the user can parse it without reading, which matters when reading is expensive.

Tap a row opens that area.

**6f. Rhythm**, medium, 4x2
The 14-day dot row, exactly as Momentum renders it. Below it, one plain line: *"Active 11 of the last 14 days."*

**This must never become a streak.** There is no consecutive count, no fire icon, no "don't break the chain." A gap is just a lighter dot.

Tap opens Momentum.

### Optional, build if Phase 12 has room

**6g. This Week**, small, 2x2
Three numbers from Momentum: completed, focus minutes, reflections. Typographic, no chart, no gauge, no progress ring toward a target, because there is no target.

**6h. One Thing**, medium, 4x2
If the user accepted a plan from the most recent Report, shows it in its first-person committed form. If not, shows the Report headline.

This is the only place guidance appears outside the Report, and it appears only because the user chose it. Never shows an unaccepted plan. Never shows a declined one.

### Widget rules

- Data comes from the widget snapshot written on every meaningful change, plus the periodic refresh. **Widgets never read a corpus and never run the engine**
- Deep links open the right surface, except while a focus session is running, when any widget tap goes to the focus screen
- Provide a preview image for each in the widget picker, generated from the real widget, never a mockup
- Every widget must be usable with TalkBack, with sensible content descriptions

### Also in Phase 12

**6i. App shortcuts**, long-press the app icon
Three static shortcuts: Quick capture, Start focus, Today's Pulse. Same reasoning as the widgets: fewer steps between intention and action. Use the androidx.core shortcuts APIs.

**6j. Quick settings tile**
One tile that starts or ends a focus session from the notification shade. Uses the platform `TileService`. Reflects live session state.

---

## STEP 7: ENGINE AND CORPUS CHANGES

### 7a. Estimates are calibration, never error, Phase 8, corpus Phase 9

**Hard rule, enforced in the validator:** no rendered sentence may state a delta between an estimate and an actual.

- **Permitted:** *"Things you estimate at an hour tend to take about three."*
- **Forbidden:** *"You underestimated by two hours."*
- **Forbidden:** *"You were off by 140 percent."*

Only ratios and tendencies. Requires at least five completed items carrying estimates before any estimate observation can fire.

Add a new observation family and a veto test proving the forbidden form cannot render.

### 7b. Capacity-aware decline detection, Phase 8

**This is a correctness fix, not politeness.**

A fluctuating condition looks identical to decline in the data. Without this, the app will tell someone with a cyclical or relapsing condition that they are deteriorating, on a fixed schedule, forever, and it will be technically accurate every time.

Before any decline, neglect or fading family can fire, the engine must check whether this shape has occurred before in the user's history. If a comparable dip has happened previously, it is a **rhythm**, not a decline, and a different family fires with different language.

Add a fact for this. Add rules for both cases. Add tests.

### 7c. RSD tone pass, Phase 9

Rejection sensitivity is common in this population. An observation read as a verdict is how someone deletes the app.

- Widen the `unflattering` flag to cover every rule concerning a decline, a gap, a neglect, an imbalance or an unmet expectation
- Where a family lacks a `NEUTRAL_AGENT` variant at a stage that can now fire unflattering, author them
- **Soften `pt.gone`.** *"Personal is not on pause. It is stopped."* is the strongest line in the corpus for a general reader and the worst for this one. Replace the flagship with a factual form, and keep the pointed version only where the user has previously indicated the area is deliberately paused
- Ship the `hardStretch` family with every constraint already specified. Nothing in the corpus can currently acknowledge that a hard month was hard

Present these as normal corpus batches for the owner's approval.

### 7d. First weeks are honest, Phases 6, 7, 8

The reflective layer needs history and is therefore empty exactly when a user decides whether to keep the app. Every reflective surface states plainly what it needs and roughly when it becomes useful.

> "Patterns show up after about three weeks."
> "This fills in as the days do."

Never an empty chart. Never a zero with no explanation.

---

## STEP 8: FIRST RUN, VISUAL AND SENSORY

### 8a. Zero-decision path, Phase 10

Current onboarding asks for two to four area names plus colors before anything happens. That is up to twelve decisions demanded from people whose central difficulty is deciding.

Add a **"Just start"** option, offered as a genuine equal alternative and not buried. It creates one area named Today with a default color and drops the user straight into adding their first item.

Areas, names and colors become things discovered later.

### 8b. Announce Pulse before it appears, Phase 10

One line at the end of onboarding: once a day, one question, one tap, can be turned off in Settings. Predictability matters enormously to autistic users, and surprise interface behavior is a real cost.

### 8c. Calm mode, Phase 1, honored everywhere after

A Settings toggle, in addition to and independent of the OS reduce-motion setting, which is already honored. Calm mode:

- Reduces motion to crossfades
- Reduces color saturation of washes and accents
- Disables the staggered list entrance
- Disables the breathing glow
- Applies to widgets and the Live Update as well as the app

This is what makes Material 3 Expressive safe for this audience rather than overwhelming. **Ship the expressive direction and the exit.**

Default: follow the OS setting.

### 8d. Analog time, Phase 4

Duration reads as a shape before it reads as a number. The focus ring, the Live Update track and the Focus Countdown widget must all make the depleting arc the primary carrier, with digits secondary.

### 8e. Staggered entrance once per session, Phase 2

Entrance animation on a screen opened twenty times a day becomes noise. Fire the stagger on first open per session only, not on every return to the tab.

### 8f. Accessibility investment goes to size and contrast

Adjustable text size, generous spacing, and contrast clearing AA in every theme including calm mode. Do **not** invest in a "dyslexia-friendly" typeface; the evidence for those is thin.

---

## STEP 9: DELIBERATELY NOT DOING THESE. RECORD IN DECISIONS.md.

**9a. No body doubling** or any real-time social presence. It requires networking and would cost the no-`INTERNET`-permission guarantee, which is the app's strongest claim. Popular in the community, evidence still thin, not worth the trade.

**9b. No AI task breakdown.** Breaks both the no-AI and no-network commitments. The first step field is the deterministic version of the same idea.

**9c. No streaks, badges, XP, levels, confetti or celebration.** Streak loss is a documented abandonment trigger for this audience. Already the rule; now it has a second reason.

**9d. No Live Update for anything but a focus session.** Per 5e.

---

## STEP 10: POSITIONING AND STORE LISTING, Phase 13, record now

Use the words people actually search for. Do not make medical claims. These are different things, and the distinction is exactly what Google Play's health policy turns on. **Claims trigger it. Keywords do not.**

**Permitted and encouraged**, in the long description and keywords:

> ADHD, autism, executive function, executive dysfunction, task paralysis, time blindness, brain fog, neurodivergent, overwhelm, procrastination, focus, one thing at a time

Phrasing such as: *"Built for people who find long lists paralyzing. Designed with ADHD, autism and brain fog in mind."*

**Forbidden** anywhere in the listing, app copy or marketing:

> treats, manages, cures, therapy, therapeutic, clinically proven, medically, symptoms, diagnosis, disorder used as a claim, and any statement that the app improves or reduces anything clinical

**Required**, in the listing and in About:

> "Clarity Now is a productivity tool. It does not provide medical advice, diagnosis or treatment."

Complete Google Play's Health Apps Declaration. Given zero data collection, no accounts, no health data access and no network permission, this app should certify cleanly. Verify the current requirements in Play Console rather than trusting this note; the policy changed through 2025 and adds medical device labeling in January 2026.

---

## HOW TO PROCEED

1. Do **Step 1** completely.
2. Then **Step 2**, because the schema window closes once user data exists.
3. Then record **Step 3** as a standing rule and apply it from that point forward.
4. Then **report back and stop.**

Everything in Steps 4 through 10 is assigned to a phase and gets built when that phase arrives. **Do not attempt them now. Do not let this document pull the build out of order.**

If any item here conflicts with something already in `MASTER_BUILD_PROMPT.md`, `design-v3.md` or the corpora, do not silently pick a winner. Flag the conflict, propose a resolution, log it in `DECISIONS.md`, and continue.

Note that Step 3 is a decision procedure, not a ban on custom work. Several components this app needs have no platform equivalent and are expected to be built by hand. Reaching that conclusion for a given component is the rule working, not a conflict.

If any item is not realistically achievable in this stack, say so plainly before starting it rather than half-building it.

Run `audit.py` after editing any specification document.
