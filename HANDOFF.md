# Handoff

Things only the owner can physically do, and what is waiting on each.

This file exists because the build runs unattended. When a builder hits something it
cannot do itself, it writes the exact steps here and keeps going rather than stopping.
Nothing in this file blocks any other work.

**Read `docs/BUILD_STATE.md` first.** That is what is built. This is what is stuck.

---

## What is left, in one paragraph

**Everything that can be built is built, green, and checked on the phone.** Thirteen
phases plus 3b, 3c, 12b and 12c, the polish pass and the deferred issue pass;
`verifyClarity` at 1,125 tests; a release APK that builds under R8 at 5.9 MB with a
baseline profile generated on a real phone, **signed with the release key and verifying
under the v3 scheme**. What remains is one thing and it is not code: a Play Console
account, and the four owner decisions that go with it. All of it is below, with the exact
steps.

## BLOCKED

### RESOLVED, 2026-09-02: release signing

Done in the deferred issue pass, and recorded here rather than deleted because the shape
of it is worth keeping.

A 4096 bit RSA keystore was generated at `~/.clarity-keys/clarity-now-release.jks`, valid
for 10,000 days, with its password in `~/.clarity-keys/.password` at mode 600 and the four
properties in `~/.gradle/gradle.properties`. **None of that is in this repository and none
of it may ever be.** `app/build.gradle.kts` reads the four properties and degrades to
unsigned when they are absent, so a clone without the keystore still builds.

`apksigner verify -v` reports **v3 only**, and that is correct rather than a gap: the
Android Gradle Plugin drops v1 and v2 at minSdk 31, so v3 is the only scheme it emits and
the only one a device at that level needs.

**Two things the owner still has to do.** Back the keystore up somewhere that survives this
machine, because losing it means never updating this app under the same package name
again. And know that anybody who installed the debug signed v0.13.0 APK has to uninstall
before installing a real release: a debug signed build cannot be upgraded by a properly
signed one.

### RESOLVED, 2026-09-01: the phone was disconnected at the start of the polish pass

Left here as a record rather than deleted, because the recovery is worth keeping.

`adb devices` reported nothing and `lsusb` showed no Google device on the bus, so a fresh
debug APK was built and a watcher armed that ran `adb wait-for-device` and installed the
moment the cable went in. It landed a few minutes later, so nothing was lost and the
polish pass ran on the real Pixel 8 throughout. An emulator was started and immediately
stopped when the owner asked for the real device.

If it happens again: build first with `./gradlew :app:assembleDebug`, then arm
`adb wait-for-device && adb install -r app/build/outputs/apk/debug/app-debug.apk` in the
background and keep working.

### Play Console setup, phase 13

Only the account holder can do these, and none of them affects the app itself.

1. Create the app entry in Play Console under the `com.kamsiob.claritynow` package.
2. Complete the **Health Apps Declaration**. `MASTER_BUILD_PROMPT.md` 16.11 records why
   this should certify cleanly: zero data collection, no accounts, no health data
   access, no network permission. **Verify the current requirements in Play Console
   rather than trusting that note**; the policy changed through 2025 and adds medical
   device labeling in January 2026.
3. Complete the Data Safety form. The answer to every collection question is no, which
   is checkable: `verifyNoInternetPermission` fails the build if a network permission
   ever reaches the merged manifest, on either variant.
4. Upload the release keystore, or create one. **It must never be committed**;
   `.gitignore` and `MASTER_BUILD_PROMPT.md` 16.3 both cover this.

### The store listing text, phase 13

**Written and final. It is in `docs/STORE_LISTING.md`.** The app name, the short
description, the full description, the Health Apps Declaration answers, the Data Safety
answers and a pre-submission checklist are all there as text to be pasted.

**Paste it, do not retype it, and do not improve it in the console text box.** Every
sentence was checked against the forbidden claim list in `MASTER_BUILD_PROMPT.md` 16.11
and against this repository's language gate. Words typed into a Play Console field are
outside every check this project has. The one that catches people is `manages`, which
is forbidden by the health policy and is also the most ordinary verb in this category.

The disclaimer sentence at the foot of the full description is character for character
identical to `R.string.about_disclaimer`, which now renders on the About screen. If
either is ever changed, both change in the same commit.

### Real device screenshots for the listing

`MASTER_BUILD_PROMPT.md` 16.6 requires screenshots generated from the real app rather
than mockups. **Five exist and are committed**, in `docs/screenshots/`, taken on the Pixel
8 from one coherent state rather than five unrelated ones: Areas, an area sheet, the
Report, Momentum and the Trail. They are the README's, and they are the obvious candidates
for the listing.

**Retaking them is reproducible rather than an afternoon of tapping.** The state is a
fixture:

```
./gradlew :app:testDebugUnitTest --tests '*EdgeCaseFixtures*' \
    -PwriteFixtures=true -PfixtureAnchor=$(date +%F)
adb push /tmp/clarity-fixtures/readme.json /sdcard/Download/
```

then Settings, Import from a file, Replace everything. The anchor has to be the day the
screenshots are taken, because the banner and the Report both describe a window ending
today.

What is still an owner's call is which of them represent the app in a store listing, and
in what order.

### The Buy Me a Coffee link

`MASTER_BUILD_PROMPT.md` 14.5. The support block is built and points at a placeholder
URL. The real one has to come from the owner's account.

---

## Not blocked, recorded here so it is not lost

**Release signing.** Moved out of BLOCKED on 2026-09-02 and done; the record of it is in
the RESOLVED entry above.

Items move here from BLOCKED when the owner has done their part and the build can pick the
work back up.

---

## Decisions taken unattended, August 27 to 28, 2026

Recorded here as well as in `DECISIONS.md` because they were taken without asking,
under the owner's standing instruction to decide, prefer the simpler and more
reversible option, log it, and continue.

### The persona set was measuring the wrong thing

The rules pass found that **seven of the nine families that never fire are dark
because of the simulator's personas, not the catalog.** Every persona reaches the log
through one helper, and every call site passes completions no greater than captures. So
`additions >= completions` on every simulated day and every simulated week, which makes
`throughput`, `netOutflow` and `intakeVsOutput` stage 3 mathematically impossible and
caps per area daily completions at two, killing `burst` and `queueDrain`.

**A person who clears a backlog on a Sunday is completely ordinary and no persona in
the set could do it.** Every silence figure taken so far was read through that
instrument.

Decision: fix the instrument before authoring against it. Extend existing personas to
cover the behavior rather than adding a twelfth, since the set was chosen to cover a
space and a new archetype changes what the aggregate means. The persona who accepts
every plan and completes none is untouched: it exists to prove the engine cannot become
a scold, and never completing is the whole point of it.

### The owner's diagnosis of the nine triggers did not hold, and was checked rather than assumed

The owner's instruction was to look for a weekly shape assigned to a one day Pulse
window before inventing any new trigger. That was checked family by family and **not
one of the nine is that error**. The three Pulse families date themselves in their own
corpus lines seventeen times between them, and the six Report families already run over
the week or the four weeks they describe. No family moved purpose.

One genuine window defect did exist and is unrelated to the diagnosis: `queueDrain`,
`clearing` and `queueDrained` measure a drain against the queue length at the window
boundary, while every sentence they license describes a transition. That needed a fact
rather than a criterion, and it is being added.

### Widget preview images

`design-v3.md` 12.1 requires a preview image per widget in the picker, **generated from
the real widget and never from a mockup**. That cannot be produced without running the
widget on a device and capturing it, and hand writing a preview layout would be exactly
the mockup the rule forbids.

Until one is captured the launcher shows the icon and the label, which is degraded and
not broken. The capture is a device task and belongs to the closing device pass.

### Widget deep links were built without a receiver, and are being fixed

Phase 12 defined five widget actions with full predicates and extractors and routed
none of them, because the agent that owned the intents deliberately stayed out of
`MainActivity`, `ClarityShell` and `ui/areas` while a second agent was editing them.
That was the right call and it left the widgets opening the app at whatever tab it was
on. A follow up is routing all of them, plus `PulseIntents.opensPulse`, which phase 6
left unrouted for the same kind of reason.

### The anti-slop sweep, `design-v3.md` 15.2

**This is a release gate and it cannot be performed from inside this repository.**

15.1 is a dated record of what the industry currently produces. Re-swearing it requires
looking at what is being shipped now, and a model's memory of the year before the list
was written is not that. Phase 12b reached the same conclusion and deliberately did not
re-date the list rather than stamp a date on a sweep that did not happen, and this run
agrees rather than overruling it.

**What has honestly been done:** every entry of 15.1 and 15.3 was checked, one at a
time, against everything phases 3c, 12, 12b and 13 built. Nothing built appears on the
list and nothing built is refused by 15.3. That is a narrower claim than a fresh sweep,
and it is the one that can be made.

**What is owed:** a look at current work, and either a re-dated 15.1 or a note that the
list still holds. Until then 15.1 reads as August 2026 and should be treated as such.

### Widget preview captures

Six widgets ship with no preview image, so the picker shows the loading layout.
`design-v3.md` 12.1 requires the preview be generated from the real widget, and a hand
written preview layout is exactly the mockup that rule forbids. Each widget's resource
file says so at the place the attribute would go.

The steps: install the release build, add each widget to a home screen, capture it with
`adb exec-out screencap`, crop to the widget, and set `android:previewImage` on each of
the six `res/xml/widget_*.xml` files. It is a device task and it is the last thing
between phase 12 and closed.

### Release signing

**The release build works.** `assembleRelease` succeeds with R8 and resource shrinking
on, and produces a **5.6 MB** APK against the debug build's 44 MB. That is worth knowing
because R8 is the step most likely to break an app that compiles: it can strip code only
reflection reaches, and this app has Room, Compose, Glance, kotlinx serialization and a
corpus parser reading from assets. Nothing needed a keep rule beyond the defaults.

**What is not proved by that** is that the shrunk APK runs. An assembled APK is not a
working one, and the check that matters is installing a signed release build on the phone
and walking the four tabs. That needs the keystore below.

`app/build.gradle.kts` has no `signingConfig` on the release build type, so
`assembleRelease` produces an unsigned APK. That is correct for now: a keystore is a
secret and this repository is public.

Before the first upload, create an upload keystore, keep it and its passwords somewhere
that survives this machine, and wire it through `gradle.properties` in `$HOME` rather
than through anything in the repository. Losing it means never being able to update the
app under the same listing.


### The two device checks nobody has made

**A signed release build, walked.** `assembleRelease` succeeds and produces an unsigned
APK. What that does not prove is that the shrunk APK runs: R8 can strip code only
reflection reaches, and this app has Room, Compose, Glance, kotlinx serialization and a
parser reading a corpus out of assets. Install a signed release build and walk the four
tabs. It needs the keystore above.

**The re-entry screen.** 14b.4's screen appears only after fourteen days without opening
the app, so a fresh install cannot reach it and neither can `adb`. Its language and its
routing are held by twenty unit tests, one of them mutation checked, and the clearing path
returns a type that will not compile into a delete. What no test can answer is the one
thing its own report flags: on a phone whose log takes a moment to load, does the Areas
screen show for a perceptible beat before the surface fades over it.

### Three decisions recorded as open, with the recommendation stated and not taken

Each is in `DECISIONS.md` with its measurement. None blocks a release.

- **Removing the Pulse repeat filter entirely.** Worth 3.8 further points of silence. It is
  a deliberate behavior rule and behavior is `MASTER_BUILD_PROMPT.md`'s authority
- **Exempting `hardStretch` from the four observation cap.** A qualified `hardStretch` is
  crowded out twice in eleven persona years, which means the hardest week of somebody's
  year can stop looking heavy. The remedy overturns a reading 10.4 argues for in writing
- **Whether the Trail should show which plans were accepted.** It renders a row for
  `PLAN_OFFERED` and `PLAN_ACCEPTED`, and declining writes nothing, so a left offer is a
  row with no acceptance beside it. 10.5 says the plan exists in the report and nowhere
  else. The precedent both ways is in the file
