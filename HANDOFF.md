# Handoff

Things only the owner can physically do, and what is waiting on each.

This file exists because the build runs unattended. When a builder hits something it
cannot do itself, it writes the exact steps here and keeps going rather than stopping.
Nothing in this file blocks any other work.

**Read `docs/BUILD_STATE.md` first.** That is what is built. This is what is stuck.

---

## BLOCKED

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

Drafted in the repository and ready to paste, but only the owner can publish it.
`MASTER_BUILD_PROMPT.md` 16.11 holds the permitted keywords, the forbidden claim words
verbatim, and the disclaimer sentence required in both the listing and About.

### Real device screenshots for the listing

`MASTER_BUILD_PROMPT.md` 16.6 requires screenshots generated from the real app rather
than mockups. The build can take them on the connected Pixel 8, but choosing which
screens represent the app in a store listing is an owner's call.

### The Buy Me a Coffee link

`MASTER_BUILD_PROMPT.md` 14.5. The support block is built and points at a placeholder
URL. The real one has to come from the owner's account.

---

## Not blocked, recorded here so it is not lost

Nothing currently. Items move here from BLOCKED when the owner has done their part and
the build can pick the work back up.

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
