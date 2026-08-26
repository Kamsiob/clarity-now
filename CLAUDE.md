# Working on Clarity Now

Read this first. It is the entry point for a session that starts with no context.

Clarity Now is a queue based personal productivity app for Android, built by one
person who does not write code and will not read the source to check it. That puts
the burden of correctness here.

---

## Start here, every session

1. **Read the issue board.** `gh issue list --state open`. Every remaining build
   phase is an issue with acceptance criteria. Work the lowest numbered open
   `phase:tracking` or `engine` issue unless told otherwise.
2. **Read `docs/BUILD_STATE.md`.** What is done, what is half done, what is known
   broken, and what the last session left behind.
3. **Read the specification for the phase you are about to build.** The authority
   order is below. Do not build from memory of a summary.
4. **Verify the toolchain before writing code.** See Toolchain. The default JDK on
   this machine is not the one this project builds with.

At the end of a phase, apply the closing step in `MASTER_BUILD_PROMPT.md` 16.8:
commit, push, build, install on the phone, and report the version name, version
code and that the install succeeded. Then update `docs/BUILD_STATE.md` and close
the phase issue.

---

## The documents, and which one wins

| file | authority over |
|---|---|
| `MASTER_BUILD_PROMPT.md` | behavior, data, build order, how to operate the engine |
| `design-v3.md` | everything visual and interactive |
| `CLARITY_LOGIC_ENGINE.md` | all six engine layers, including guidance |
| `CORPUS_1_PULSE.md` | Pulse language |
| `CORPUS_2_REPORT.md` | Report language and the guidance corpus |
| `CORPUS_3_MOMENTUM.md` | Momentum headline and Areas banner language |

When two disagree: `design-v3.md` wins on anything visual or interactive.
`MASTER_BUILD_PROMPT.md` wins on behavior and data. `CLARITY_LOGIC_ENGINE.md` wins
inside `domain.engine` and `domain.guidance`. The corpora win on the exact wording
of any sentence. **If a genuine contradiction survives all of that, stop and ask.**

`clarity-now-visual-reference-v2.html` and `-v3.html` are static mocks. Read them
for structure, proportion and color relationships. **Never copy them into the
project, and never take a number from them.** Their pixel values are not uniformly
scaled. Every dimension is stated in dp in `design-v3.md`, which is the only source
for a number.

`rationale/` is superseded history. Not needed to build.

---

## Toolchain

The build machine runs **Bazzite**, an immutable Fedora Atomic system. `/usr` is
read only. Never attempt an `rpm-ostree` layer. Everything lives in `$HOME`.

**The default `java` on PATH is JDK 26, which the Android Gradle Plugin does not
support.** Every Gradle invocation must set `JAVA_HOME` first:

```bash
export JAVA_HOME=/home/linuxbrew/.linuxbrew/opt/openjdk@21
```

| tool | where |
|---|---|
| JDK 21 | `/home/linuxbrew/.linuxbrew/opt/openjdk@21` |
| Android SDK | `~/Android/Sdk`, recorded in `local.properties`, never committed |
| Gradle | the wrapper in this repo, never a system Gradle |
| `adb` | `~/Android/Sdk/platform-tools/adb`, already on PATH |

**Test device: a Pixel 8 (`shiba`) over USB.** The specification names a Pixel 10
Pro XL; the connected device is a Pixel 8 and everything installs and runs on it.
Check with `adb devices -l` before assuming.

---

## Commands

```bash
export JAVA_HOME=/home/linuxbrew/.linuxbrew/opt/openjdk@21

./gradlew verifyClarity          # every automated gate that runs offline
./gradlew :app:testDebugUnitTest # unit tests
./gradlew :app:installDebug      # build and install on the connected device

# Regenerating the golden fixture is deliberate, never a side effect of a test run
./gradlew :app:testDebugUnitTest -PregenerateGolden=true
```

`verifyClarity` runs three things and all three must stay green:

- `verifyLanguageHygiene` fails on an em dash, an en dash, any character above
  U+007F, or a British spelling, across every `.kt`, `.kts`, `.xml`, `.md` and
  `.pro` file in the repo
- `verifyNoInternetPermission` fails if any variant's **merged** manifest declares
  a network permission
- the unit test suite, including the replay harness and the golden fixture

To see the app on the device after installing:

```bash
adb shell am start -n com.kamsiob.claritynow.debug/com.kamsiob.claritynow.MainActivity
adb exec-out screencap -p > /tmp/shot.png
adb logcat -d | grep -A 20 "FATAL EXCEPTION"
```

**Check logcat after any device test.** Several defects in this project were
silent app exits with no visible error, and a screenshot alone would have passed
them.

---

## Rules that fail the build

These are enforced by tests or gates, not by proofreading.

1. **No `INTERNET` permission**, in the merged manifest, not just the source. The
   privacy policy states this as a fact a person can check in Android settings.
2. **No em dashes, no en dashes, no emojis, no non ASCII** outside standard
   punctuation. In UI copy, the corpora, comments, the README and commit messages.
3. **American English.** color, license, behavior, prioritize, organize.
4. **No monetization, no accounts, no analytics, no AI or ML of any kind.** Not one
   library, not one call. The only money related element is one link to Buy Me a
   Coffee in Settings and About.
5. **`domain.engine`, `domain.guidance` and `domain.replay` are pure Kotlin.** No
   Android imports, no `System.currentTimeMillis`, no `Random`, no
   `String.hashCode()`. Use `StableHash`, which is FNV-1a 64 bit, and the injected
   `ClarityClock`.
6. **No engine state in DataStore.** Variation history, escalation stages, personal
   records, first ever flags and plan history all derive from the event log, or two
   devices silently disagree. This does not fail loudly if you get it wrong.
7. **Zero warnings.** `allWarningsAsErrors` is on.

---

## Rules that fail review

8. **Every sentence about a person's own data comes from a corpus file, through
   the engine layers in order.** No second path. Not for empty states, not for
   errors, not for one string. Fixed interface labels and direct readouts of a
   queried number live in `strings.xml`; observations never do.
9. **The event log is the truth.** Areas and items are a rebuildable cache.
   Tombstones, never row deletes. Fractional order keys, never integers.
10. **No colored stripe, bar or edge treatment on any element, ever.** The single
    most recognizable machine generated design tell, removed on purpose.
11. **Every element carries exactly one separation device.** Whitespace, or a
    lightness shift, or elevation, or a hairline. Never a hairline and a shadow on
    the same element.
12. **Delete is never committed by a full swipe.** A full left swipe commits Swap.
    Delete needs a deliberate tap, then an undo window.
13. **Layer six never injects a sentence about a plan.** Follow through is a
    priority boost on a family that must qualify independently. The mechanism must
    have no way to tell someone they broke a promise.
14. **When the design leaves a choice open, the obvious answer is not the answer.**
    `design-v3.md` section 15. Identify the statistically common option and
    deliberately choose something else that serves the brief as well or better. If
    the obvious answer is genuinely best, use it and record why.

---

## How the code is laid out

`docs/ARCHITECTURE.md` has the full map. The three things that surprise people:

- **`ClarityEvent` is plain Kotlin and `ClarityEventRow` is the Room entity.** The
  reducer must not import `androidx.room`, so the log record and the database row
  are separate types with mappers between them.
- **`ClarityRepository` is the only writer in the app.** ViewModels never touch a
  DAO, composables never touch a repository. One write path: build the event,
  assign lamport and originId, then append and project inside one transaction.
- **The in memory `ClarityState` is the projection everything reads.** The Room
  cache tables exist for cold start speed and paging, and can be dropped and
  rebuilt from the log at any time. A debug action does exactly that as a proof.

---

## Conventions

**Commits.** Present tense subject naming what changed, then a body explaining why
where it is not obvious. No em dashes, no emojis, American spelling. Trailers:

```
Co-Authored-By: Claude Opus 5 <noreply@anthropic.com>
```

**Issues.** Every issue that describes future work carries **Acceptance criteria**
as a checklist of statements that are unambiguously true or false when the work is
done, each citing the specification section that requires it. An issue without
acceptance criteria is not ready to be worked. Keep this convention for issues you
open yourself.

**Versioning.** `versionName` is semantic and chosen deliberately, with one line of
reasoning stated at each release. `versionCode` is `major * 10000 + minor * 100 +
patch` and never decreases. Both live at the top of `app/build.gradle.kts`.

**One copy of this project on the machine.** One clone, one build output tree. No
dated folders, no `-v2` directories.

---

## What is already built

See `docs/BUILD_STATE.md` for the current state, and the closed issues for what
each phase delivered. As of the last update: phases 1 and 2 of 13.
