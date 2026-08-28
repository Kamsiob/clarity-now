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
