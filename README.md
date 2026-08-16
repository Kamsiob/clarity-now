# Clarity Now

A queue based personal productivity app for Android. Your life has a small number of
areas of focus, each area has exactly one active item, and everything else waits in a
queue. Complete the active item and the next one is promoted.

Around that sit three reflective features powered by one deterministic engine:
**Clarity Pulse**, a daily behavioral observation with a one tap answer, **Momentum**,
a calm progress mirror, and the **Clarity Report**, a weekly narrative. **Trail**, a
chronological event log, is the source of truth from which every piece of state and
every sentence derives.

Built by Kamsiob.

---

## What this app does not do

These are checkable facts rather than promises, and most of them are enforced by a
build test rather than by good intentions.

- **No internet permission.** The merged manifest is checked on every build. Android
  will refuse any attempt this app makes to open a connection, and you can verify that
  yourself in the app info screen
- **No accounts, no cloud, no servers.** There is nowhere for your data to go
- **No analytics, no telemetry, no crash reporting, no advertising, no third party
  SDKs**
- **No AI, no model, no inference.** Every sentence the app writes about your week
  comes from arithmetic over your own event log and a library of hand written
  sentences. There is no model on the device and no call to one anywhere else
- **No subscription, no in app purchase, no locked features, no upgrade prompts.**
  Every feature is available to everyone immediately. The only money related element
  in the whole app is one link, on two screens, that you never have to press

Your areas, items, history, reflections and reports live in one database inside this
app's private storage. Erase all data in Settings removes everything permanently, and
so does uninstalling.

---

## Build state

The app is being built in thirteen phases. This is **phase 1 of 13**.

| phase | what it delivers | state |
|---|---|---|
| 1 | Foundations: theme, fonts, event log, reducer, replay harness, golden fixture | done |
| 2 | Areas, items, the queue, swipe gestures, the promotion animation | next |
| 3 | Trail | |
| 4 | Focus sessions | |
| 5 | The logic engine skeleton and the simulator | |
| 6 | Pulse | |
| 7 | Momentum | |
| 8 | Week snapshots and the Clarity Report | |
| 9 | The sentence corpus | |
| 9b | Guidance | |
| 10 | Onboarding and the tutorial | |
| 11 | Settings, About, export, import, erase | |
| 12 | Widgets and notifications | |
| 13 | Baseline profile, accessibility pass, release | |

Phase 1 ships an installable app that shows the mark, the type system and the theme.
The screens arrive in phase 2.

---

## Building it

Requires **JDK 21** and the **Android SDK**. Nothing else, and no system Gradle: the
wrapper in this repository is the build.

```
git clone https://github.com/kamsiob/clarity-now.git
cd clarity-now
echo "sdk.dir=$HOME/Android/Sdk" > local.properties
./gradlew :app:installDebug
```

If your default JDK is not 21, point the build at one:

```
JAVA_HOME=/path/to/jdk-21 ./gradlew :app:installDebug
```

The debug build installs as `com.kamsiob.claritynow.debug` and is labeled
`Clarity Now debug`, so it can sit alongside a release install.

### Verification

```
./gradlew verifyClarity
```

Runs the automated gates: the merged manifest carries no network permission, no source
or document contains an em dash, an en dash, a non ASCII character or a British
spelling, and the whole unit test suite passes. Warnings are errors in this project,
so a warning is a failed build.

---

## Repository layout

```
app/                    the Android application, one module
docs/EVENT_FORMAT.md    the event log contract, in prose
testdata/               the golden log and the exact state it must produce
rationale/              review panels and superseded drafts, not needed to build
```

The specification lives in the repository alongside the code, because the app is
almost entirely specification.

| file | what it is |
|---|---|
| `MASTER_BUILD_PROMPT.md` | behavior, data, build order, and how to operate the engine |
| `design-v3.md` | visual and interaction source of truth, every dimension in dp |
| `CLARITY_LOGIC_ENGINE.md` | all six engine layers, including guidance |
| `CORPUS_1_PULSE.md` | the Pulse language |
| `CORPUS_2_REPORT.md` | the Report and guidance language |
| `CORPUS_3_MOMENTUM.md` | the Momentum and banner language |
| `clarity-now-visual-reference-v2.html` | static mock, most screens |
| `clarity-now-visual-reference-v3.html` | static mock, the screens rebuilt in v3 |

The two HTML files are mocks. Neither is shipping code and neither may be copied into
the project.

---

## The data model, briefly

Every state change is an immutable, append only event. Areas, items, queue positions
and completion states are computed by replaying the log. Everything else in the
database is a cache that can be dropped and rebuilt with no data loss.

This exists so that a Linux desktop companion can later sync by exchanging log files
with no rework. Nothing in version 1 opens a socket. What version 1 has is the format,
versioned from the first event, tombstones everywhere, a logical clock and a device id
on every event, fractional order keys, and a replay harness that proves two devices
diverging and merging reach byte identical state.

`docs/EVENT_FORMAT.md` describes all of it. `testdata/` holds the fixture that any
second implementation must agree with.

---

## Fonts

**Newsreader** and **Hanken Grotesk**, both variable, both under the SIL Open Font
License, both committed as files in `app/src/main/res/font` with their license texts
in `app/src/main/res/raw`.

Android's Downloadable Fonts API is never used. It fetches over the network through
Play Services, and this app has no internet permission.

---

## License

AGPL-3.0. See `LICENSE`.

The source is public so that every privacy claim on this page can be checked rather
than trusted.

---

## Contact

hello@kamsiob.com
