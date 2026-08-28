package com.kamsiob.claritynow.ui.theme

/**
 * The three rules, evaluated. Every pair the audit knows how to make comes out of here,
 * so a test asserts on a list rather than rebuilding the enumeration.
 *
 * - **Rule A**, [inkOnTheLadder]: every color token, on the three ranks of its world's
 *   surface ladder. Total over the tokens, by reflection.
 * - **Rule B**, [runningTextOnEveryGround]: every ground the app can draw, against the
 *   ink its world sets running text in. Total over the grounds.
 * - **Rule C**, [namedPlacements]: the pairs that are neither, named with their file.
 */
internal object AuditRun {

    private val daylight = listOf("light" to ClarityLightColors, "dark" to ClarityDarkColors)

    private fun roleKey(world: String, name: String) = "$world.$name"

    /** See `every color token the theme declares has a role`. */
    private val UNCLASSIFIED = Role(
        textFloor = null,
        graphicFloor = null,
        isGround = false,
        why = "no role. See `every color token the theme declares has a role`.",
    )

    /**
     * Rule A. design-v3.md 3.1 and 3.2 make canvas, raise and card a ladder, and between
     * them they are the ground under almost everything the app draws, so a token that
     * can be a foreground at all is measured on all three.
     *
     * `parchment` is deliberately not in this set. It is a specialized surface with two
     * known occupants, design-v3.md 10.2's banner text and 14.5's support block, and both
     * reach it through rule B or a placement instead. Putting it here would ask
     * `deleteMuted` to be legible on a weekly banner that has no delete action on it.
     *
     * A token that is only ever a ground is skipped: measuring the page against the page
     * is noise, and the steps between the three ranks are `SurfaceLadderTest`'s subject,
     * where they are measured in L* rather than as a contrast ratio, which is the right
     * tool for two near-white or two near-black surfaces.
     */
    fun inkOnTheLadder(): List<Measured> = buildList {
        daylight.forEach { (name, c) ->
            val ladder = listOf(
                Swatch("$name canvas", c.canvas),
                Swatch("$name raise", c.raise),
                Swatch("$name card", c.card),
            )
            ContrastAudit.tokensOf(c).forEach { (token, color) ->
                // An unclassified token measures and does not assert, so the coverage
                // test is what reports it rather than an exception from here.
                val role = ROLES[roleKey(name, token)] ?: UNCLASSIFIED
                if (role.inkFloor == null && role.isGround) return@forEach
                ladder.forEach { rung ->
                    add(
                        Measured(
                            Swatch("$name.$token", color), rung, role.inkFloor,
                            "rule A, a token on the surface ladder. " + role.why,
                        ),
                    )
                }
            }
        }
        val night = listOf(
            Swatch("deepBlack", ClarityContemplativeColors.deepBlack),
            Swatch("surfaceRaised", ClarityContemplativeColors.surfaceRaised),
        )
        ContrastAudit.tokensOf(ClarityContemplativeColors).forEach { (token, color) ->
            val role = ROLES[roleKey("contemplative", token)] ?: UNCLASSIFIED
            if (role.inkFloor == null && role.isGround) return@forEach
            night.forEach { rung ->
                add(
                    Measured(
                        Swatch("contemplative.$token", color), rung, role.inkFloor,
                        "rule A, a token on the Contemplative ground. " + role.why,
                    ),
                )
            }
        }
    }

    /** Rule B. Every ground, against the ink its world sets running text in. */
    fun runningTextOnEveryGround(): List<Measured> = worlds().flatMap { world ->
        world.families.flatMap { family ->
            world.bodyInks.flatMap { bodyInk ->
                val exemption = RULE_B_EXEMPTIONS[bodyInk.name to family.name]
                family.members.map { ground ->
                    Measured(
                        bodyInk, ground,
                        if (exemption == null) ContrastAudit.TEXT_FLOOR else null,
                        "rule B, the ${family.name} family: ${family.where}" +
                            (exemption?.let { ". Not asserted: $it" } ?: ""),
                    )
                }
            }
        }
    }

    /** Rule C. */
    fun namedPlacements(): List<Measured> =
        daylight.flatMap { (name, c) -> daylightPlacements(name, c) } +
            contemplativePlacements(false) +
            contemplativePlacements(true)

    fun worlds(): List<AuditWorld> = listOf(
        daylightWorld("light", ClarityLightColors),
        daylightWorld("dark", ClarityDarkColors),
        contemplativeWorld(false),
        contemplativeWorld(true),
    )

    fun everything(): List<Measured> =
        inkOnTheLadder() + runningTextOnEveryGround() + namedPlacements()
}
