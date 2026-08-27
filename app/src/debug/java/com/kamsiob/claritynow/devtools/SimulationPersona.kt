package com.kamsiob.claritynow.devtools

import com.kamsiob.claritynow.domain.engine.StableHash

/**
 * The synthetic histories of CLARITY_LOGIC_ENGINE.md 12, one class per persona.
 *
 * Section 12 names eleven: heavy single area, balanced across four, sporadic, abandoning,
 * high focus, low focus, brand new, long dormant with a revival, queue hoarder, fast
 * completer, and **a persona who accepts every plan and completes none**. All eleven are
 * in [ALL] and the last one is not optional: it exists to prove the engine cannot turn
 * into a scold, and its dump is the evidence for the non-compliance check in
 * [SimulationChecks].
 *
 * ## Determinism, without a random number generator
 *
 * A persona decides what happens on a day with [roll], which is `StableHash` of the
 * persona key, the day and a label. FNV-1a is the only hash this project permits for
 * anything two runs have to agree about, per CLARITY_LOGIC_ENGINE.md 7.6, and a seeded
 * `Random` would have been the obvious choice and the wrong one: a stream depends on how
 * many times it was drawn from, so adding one call anywhere upstream would shift every
 * later day and the year would silently become a different year. A hash of the day depends
 * on nothing but the day.
 *
 * ## What a persona is not
 *
 * It is not a test fixture aimed at a family. Nothing here reaches for a rule key or a
 * threshold, and no persona is shaped to make a particular sentence appear. A persona is a
 * plausible year of somebody's behavior, and which rules fire is the question the simulator
 * exists to answer rather than something arranged in advance. A persona built to trigger
 * `persistence` would prove that `persistence` can be triggered and nothing else.
 */
abstract class SimulationPersona(
    /** Stable, and part of every [roll], so renaming one changes its whole year. */
    val key: String,
    val title: String,
    /** Why section 12 asks for this persona. Printed at the head of the dump. */
    val why: String,
) {

    /** The areas this person keeps, created on [installDay]. */
    abstract val areas: List<SimulatedArea>

    /**
     * The first day anything at all happens.
     *
     * Non zero for `brandNew`, whose whole point is that most of the simulated year is
     * before the app existed for them, so the dump ends in the first weeks rather than
     * beginning in them and the engine is seen answering an empty log for months.
     */
    open val installDay: Int = 0

    /** Whether the app is opened on this local day. A day not opened generates no Pulse. */
    open fun opensOn(day: Int): Boolean = day >= installDay

    /** What the person did on this local day, written into [log]. */
    abstract fun act(log: SimulatorLog, day: Int)

    /**
     * Whether a Pulse shown today is answered.
     *
     * Dismissing the sheet is a fully supported state, per `MASTER_BUILD_PROMPT.md` 11.6,
     * so a persona that answered everything would be an unrealistic one and would also
     * over supply the callback families with material.
     */
    open fun answersPulse(day: Int): Boolean = roll(day, "answer", ANSWER_ODDS) != 0

    /**
     * Whether this person accepts every plan they are offered. One persona does.
     *
     * Layer 6 is phase 9b and produces nothing yet, so the simulator writes the
     * `PLAN_OFFERED` and `PLAN_ACCEPTED` pair itself for this persona. See
     * [ClaritySimulator] for what it writes and what it deliberately does not.
     */
    open val acceptsEveryPlan: Boolean = false

    /** Creates this persona's areas. Called once, on [installDay]. */
    open fun setUp(log: SimulatorLog) {
        areas.forEachIndexed { index, area ->
            log.createArea(installDay, SETUP_HOUR, area.id, area.name, PALETTE[index % PALETTE.size])
        }
    }

    /** A stable value in `0 until bound`, from this persona, this day and a label. */
    protected fun roll(day: Int, label: String, bound: Int): Int =
        StableHash.bucket("$key|$day|$label", bound)

    /** A title from the shared pool, chosen stably. */
    protected fun itemTitle(day: Int, label: String): String =
        TITLES[StableHash.bucket("$key|$day|$label", TITLES.size)]

    /**
     * One ordinary day's work in one area: captures, then completions, then focus.
     *
     * The order is the order a person does these things in, and it matters to the facts:
     * an item captured this morning and completed this afternoon has an active duration of
     * zero days, which is what `fastCompleter` is for, and an item captured today and
     * completed in three weeks is what makes `persistence` have anything to describe.
     *
     * An area with nothing active promotes the head of its queue, because a queue with
     * nobody working on it is not a shape this app produces: `ClarityRepository` promotes
     * on completion and on the first item into an empty area.
     */
    protected fun work(
        log: SimulatorLog,
        day: Int,
        areaId: String,
        captures: Int = 0,
        completions: Int = 0,
        focusMinutes: Int = 0,
        focusFinished: Boolean = true,
        label: String = areaId,
    ) {
        var step = 0
        repeat(captures) {
            log.capture(day, hourOf(step++), areaId, itemTitle(day, "$label|capture|$it"))
        }
        if (log.activeItem(areaId) == null) log.promoteNext(day, hourOf(step++), areaId)
        repeat(completions) {
            if (log.completeActive(day, hourOf(step++), areaId) != null) {
                log.promoteNext(day, hourOf(step++), areaId)
            }
        }
        if (focusMinutes > 0) log.focusRun(day, FOCUS_HOUR, areaId, focusMinutes, focusFinished)
    }

    /**
     * A deliberate change of what is being worked on, which is what a swap is.
     *
     * Promoting over something that is still active is the only thing that writes a
     * `demotedItemId`, and that field is the whole definition of a swap, per
     * `TrailQueries.swapsBetween`.
     */
    protected fun swap(log: SimulatorLog, day: Int, areaId: String) {
        if (log.activeItem(areaId) != null && log.queueSize(areaId) > 0) {
            log.promoteNext(day, SWAP_HOUR, areaId)
        }
    }

    /** Waking hours, so no event lands at an hour that does not exist. */
    private fun hourOf(step: Int): Int = (FIRST_HOUR + step).coerceAtMost(LAST_HOUR)

    companion object {

        private const val SETUP_HOUR = 8
        private const val FIRST_HOUR = 9
        private const val LAST_HOUR = 21
        private const val FOCUS_HOUR = 14
        private const val SWAP_HOUR = 11

        /** One Pulse in five goes unanswered, which is an ordinary rate rather than a flaw. */
        private const val ANSWER_ODDS = 5

        /** Area colors. Never read by the engine; carried because the payload has the field. */
        private val PALETTE = listOf("#2D7FF9", "#3E8E6E", "#B4643C", "#6B5BA8", "#4C6B8A", "#8A6B4C")

        /**
         * The pool every item title is drawn from.
         *
         * Ordinary errands and ordinary work, deliberately. A title is interpolated into a
         * sentence about somebody's week and reaches validator check 9, which measures the
         * rendered length, so a pool of unusually long titles would fail lines that are fine
         * in practice and a pool of one word titles would pass lines that are not.
         */
        private val TITLES = listOf(
            "Rewrite the proposal intro",
            "Book the dentist",
            "Renew the car registration",
            "Draft the quarterly summary",
            "Clear the garage shelf",
            "Call the bank",
            "Fix the leaking tap",
            "Plan the trip route",
            "Sort the photo backlog",
            "Reply to the landlord",
            "Update the resume",
            "Read the design chapter",
            "Repot the balcony plants",
            "Cancel the old subscription",
            "Schedule the eye test",
            "Write the release notes",
            "Order new running shoes",
            "Label the storage boxes",
            "Finish the tax folder",
            "Set up the new laptop",
            "Review the insurance quote",
            "Tidy the reading list",
            "Prep the interview questions",
            "Patch the bike tire",
        )

        /** Every persona section 12 names, in the order it names them. */
        val ALL: List<SimulationPersona> = listOf(
            HeavySingleArea,
            BalancedAcrossFour,
            Sporadic,
            Abandoning,
            HighFocus,
            LowFocus,
            BrandNew,
            LongDormantRevival,
            QueueHoarder,
            FastCompleter,
            AcceptsEveryPlanCompletesNone,
        )
    }
}

/** One area a persona keeps. */
data class SimulatedArea(val id: String, val name: String)

/**
 * Nearly everything in one area, with three others kept and barely touched.
 *
 * The shape `concentration` and `singleFocus` describe, and the one that puts the other
 * three areas into `neglectedArea` territory without any of them being archived, which is
 * the case prohibition 3 of 1.1 is about: the quiet areas are still live and still
 * nameable, so the rules have to be right rather than structurally prevented from firing.
 */
private object HeavySingleArea : SimulationPersona(
    key = "heavySingleArea",
    title = "Heavy single area",
    why = "One area holds the week. Feeds concentration, singleFocus and neglectedArea.",
) {
    override val areas = listOf(
        SimulatedArea("work", "Work"),
        SimulatedArea("home", "Home"),
        SimulatedArea("health", "Health"),
        SimulatedArea("learning", "Learning"),
    )

    override fun act(log: SimulatorLog, day: Int) {
        work(log, day, "work", captures = 1, completions = roll(day, "done", 2))
        if (roll(day, "swap", 9) == 0) swap(log, day, "work")
        if (roll(day, "home", 11) == 0) work(log, day, "home", captures = 1, completions = 1)
        if (roll(day, "health", 17) == 0) work(log, day, "health", captures = 1)
        if (roll(day, "learning", 23) == 0) work(log, day, "learning", captures = 1)
    }
}

/**
 * Four areas, none of them dominant. The `balanced`, `areaBalance` and `spread` shape.
 *
 * Which area moves is a rotation over the day rather than a per area roll, so no area can
 * accidentally accumulate a share by chance over a long run. A persona meant to be balanced
 * that drifts into dominance would quietly stop testing the thing it is named for.
 */
private object BalancedAcrossFour : SimulationPersona(
    key = "balancedAcrossFour",
    title = "Balanced across four",
    why = "Four areas, none dominant. Feeds balanced, spread and areaBalance.",
) {
    override val areas = listOf(
        SimulatedArea("work", "Work"),
        SimulatedArea("home", "Home"),
        SimulatedArea("health", "Health"),
        SimulatedArea("personal", "Personal"),
    )

    override fun act(log: SimulatorLog, day: Int) {
        val order = areas.indices.map { areas[(day + it) % areas.size].id }
        order.take(TOUCHED_PER_DAY).forEachIndexed { position, areaId ->
            work(
                log,
                day,
                areaId,
                captures = 1,
                completions = roll(day, "done$position", 2),
                label = "$areaId$position",
            )
        }
    }

    /** Two of four a day, so each area rotates through and none is dominant or neglected. */
    private const val TOUCHED_PER_DAY = 2
}

/** Bursts with long gaps. The `quietDay`, `quietWeek` and `burst` shape. */
private object Sporadic : SimulationPersona(
    key = "sporadic",
    title = "Sporadic",
    why = "Long gaps with occasional bursts. Feeds quietDay, quietWeek, burst and comeback.",
) {
    override val areas = listOf(
        SimulatedArea("work", "Work"),
        SimulatedArea("home", "Home"),
    )

    override fun opensOn(day: Int): Boolean = roll(day, "open", 3) != 0

    override fun act(log: SimulatorLog, day: Int) {
        when (roll(day, "mood", 7)) {
            0 -> work(log, day, "work", captures = 2, completions = 2)
            1 -> work(log, day, "home", captures = 1, completions = 1)
            2 -> work(log, day, "work", captures = 1)
            else -> Unit
        }
    }
}

/**
 * Strong for the first months, then trailing away. The `decliningActivity` and
 * `abandonmentPattern` shape, and the one `hardStretch` was added to the corpus for.
 *
 * Focus sessions are ended early rather than completed once the decline sets in, which is
 * what `focusAbandonment` counts. Nothing here is written as a failure and no sentence the
 * engine produces about this year may read as one, which is what the mirror test in 11.3
 * asks of the whole dump.
 */
private object Abandoning : SimulationPersona(
    key = "abandoning",
    title = "Abandoning",
    why = "Strong for two months, then trailing away. Feeds decliningActivity, " +
        "focusAbandonment, hardStretch and the difficulty register in 6.4.",
) {
    override val areas = listOf(
        SimulatedArea("work", "Work"),
        SimulatedArea("side", "Side project"),
        SimulatedArea("health", "Health"),
    )

    override fun opensOn(day: Int): Boolean = day < FADE_START || roll(day, "open", 3) == 0

    override fun act(log: SimulatorLog, day: Int) {
        if (day < FADE_START) {
            work(log, day, "work", captures = 1, completions = 1, focusMinutes = 25)
            if (roll(day, "side", 2) == 0) work(log, day, "side", captures = 1, completions = 1)
            if (roll(day, "health", 5) == 0) work(log, day, "health", captures = 1, completions = 1)
            return
        }
        if (day < QUIET_START) {
            if (roll(day, "late", 3) == 0) {
                work(log, day, "work", captures = 1, focusMinutes = 25, focusFinished = false)
            }
            return
        }
        if (roll(day, "residual", 9) == 0) work(log, day, "work", captures = 1)
    }

    private const val FADE_START = 60
    private const val QUIET_START = 150
}

/** Focus sessions every day, mostly finished. The `focusInvestment` and `focusProtected` shape. */
private object HighFocus : SimulationPersona(
    key = "highFocus",
    title = "High focus",
    why = "Daily sessions, mostly finished. Feeds focusInvestment, focusProtected and focusHabitForming.",
) {
    override val areas = listOf(
        SimulatedArea("writing", "Writing"),
        SimulatedArea("study", "Study"),
    )

    override fun act(log: SimulatorLog, day: Int) {
        val areaId = if (roll(day, "which", 3) == 0) "study" else "writing"
        work(
            log,
            day,
            areaId,
            captures = 1,
            completions = if (roll(day, "done", 3) == 0) 1 else 0,
            focusMinutes = SESSION_MINUTES,
            focusFinished = roll(day, "finish", 8) != 0,
        )
        if (roll(day, "second", 3) == 0) log.focusRun(day, SECOND_SESSION_HOUR, areaId, SESSION_MINUTES, true)
    }

    private const val SESSION_MINUTES = 50
    private const val SECOND_SESSION_HOUR = 19
}

/**
 * Ordinary queue movement and no focus session ever.
 *
 * The persona that proves the focus families stay quiet rather than reaching for a zero.
 * `MASTER_BUILD_PROMPT.md` 11.4 forbids padding a section to reach a minimum, and a report
 * about this year that mentions focus at all is a report that invented something.
 */
private object LowFocus : SimulationPersona(
    key = "lowFocus",
    title = "Low focus",
    why = "Never starts a session, and archives an area mid year. Proves the focus families " +
        "stay silent rather than say zero, and that an archived area cannot be named.",
) {
    override val areas = listOf(
        SimulatedArea("work", "Work"),
        SimulatedArea("home", "Home"),
        SimulatedArea("errands", "Errands"),
        SimulatedArea("course", "Evening course"),
    )

    override fun act(log: SimulatorLog, day: Int) {
        // The course finishes and the area is archived. Prohibition 3 of 1.1 says an
        // archived area is absent from AreaFacts entirely from that moment, so nothing
        // after this day may name it, and eight months of dump are the evidence.
        if (day == ARCHIVE_DAY) {
            log.archiveArea(day, ARCHIVE_HOUR, "course")
            return
        }
        val available = if (day < ARCHIVE_DAY) areas else areas.dropLast(1)
        val areaId = available[roll(day, "area", available.size)].id
        work(log, day, areaId, captures = 1, completions = roll(day, "done", 2))
    }

    private const val ARCHIVE_DAY = 90
    private const val ARCHIVE_HOUR = 17
}

/**
 * Installs three weeks before the year ends.
 *
 * The whole point is that the engine spends most of the run with an empty log and has to
 * stay silent through it, and that `firstWeek`, `firstDays` and `cleanSlate` arrive at the
 * end where a reader of the dump can see them in sequence. The reset virginity failure mode
 * in section 13 is exactly this shape: a fresh install must never say `since March`.
 */
private object BrandNew : SimulationPersona(
    key = "brandNew",
    title = "Brand new",
    why = "Installs three weeks before the year ends. Feeds firstWeek, firstDays, cleanSlate " +
        "and the reset virginity guard in section 13.",
) {
    override val installDay = 344

    override val areas = listOf(SimulatedArea("work", "Work"))

    override fun act(log: SimulatorLog, day: Int) {
        if (day < installDay) return
        if (day == installDay) {
            log.createArea(day, FIRST_AREA_HOUR, "home", "Home", "#3E8E6E")
        }
        work(log, day, "work", captures = 1, completions = if (roll(day, "done", 3) == 0) 0 else 1)
        if (roll(day, "home", 3) == 0) work(log, day, "home", captures = 1)
    }

    private const val FIRST_AREA_HOUR = 9
}

/** Two months on, five months off, then back. The `comeback`, `areaRevival` and `rebalance` shape. */
private object LongDormantRevival : SimulationPersona(
    key = "longDormantRevival",
    title = "Long dormant, then a revival",
    why = "Two months on, five months away, then back. Feeds comeback, areaRevival and rebalance.",
) {
    override val areas = listOf(
        SimulatedArea("work", "Work"),
        SimulatedArea("music", "Music"),
    )

    override fun opensOn(day: Int): Boolean = day !in DORMANT

    override fun act(log: SimulatorLog, day: Int) {
        if (day in DORMANT) return
        work(log, day, "work", captures = 1, completions = roll(day, "done", 2))
        if (roll(day, "music", 4) == 0) work(log, day, "music", captures = 1, completions = 1)
    }

    private val DORMANT = 56..250
}

/**
 * Writes far more down than leaves. The `accumulation`, `queuePressure` and `growingQueues`
 * shape.
 *
 * Every capture goes into an area rather than into the inbox, deliberately. Addendum 01 4a
 * keeps unfiled captures out of `additionsPerArea` on purpose, so a hoarder who captured
 * into the inbox would produce a flat queue and none of the families this persona exists
 * to feed.
 */
private object QueueHoarder : SimulationPersona(
    key = "queueHoarder",
    title = "Queue hoarder",
    why = "Far more arrives than leaves. Feeds accumulation, queuePressure and growingQueues.",
) {
    override val areas = listOf(
        SimulatedArea("work", "Work"),
        SimulatedArea("home", "Home"),
        SimulatedArea("someday", "Someday"),
    )

    override fun act(log: SimulatorLog, day: Int) {
        work(log, day, "work", captures = 1)
        if (roll(day, "home", 2) == 0) work(log, day, "home", captures = 1)
        if (roll(day, "someday", 4) == 0) work(log, day, "someday", captures = 1)
        if (roll(day, "done", 7) == 0) work(log, day, "work", completions = 1, label = "workDone")
    }
}

/** Nothing sits. The `throughput`, `clearing` and `queueDrained` shape. */
private object FastCompleter : SimulationPersona(
    key = "fastCompleter",
    title = "Fast completer",
    why = "Captured and finished the same day. Feeds throughput, clearing and queueDrained.",
) {
    override val areas = listOf(
        SimulatedArea("work", "Work"),
        SimulatedArea("errands", "Errands"),
    )

    override fun act(log: SimulatorLog, day: Int) {
        val captures = 1 + roll(day, "captures", 2)
        work(log, day, "work", captures = captures, completions = captures)
        if (roll(day, "errands", 3) == 0) work(log, day, "errands", captures = 1, completions = 1)
    }
}

/**
 * Accepts every plan and finishes none of them. CLARITY_LOGIC_ENGINE.md 12, last bullet.
 *
 * **This persona is the test, not an example.** Section 12 requires that a reader of this
 * dump cannot tell plans were ever accepted, and that if they can, the follow-through
 * implementation is **removed rather than tuned**. 10.6 gives the mechanism that has to
 * hold: follow-through is a priority boost on a family that must qualify independently, and
 * there is no path by which an unresolved plan can produce a sentence.
 *
 * One item per week is the item a plan would be anchored to, and it is captured and never
 * promoted and never completed. Everything else in the year is ordinary. So the log carries
 * a real, visible, repeated non-compliance, and the checks read the dump for any trace of
 * it.
 */
private object AcceptsEveryPlanCompletesNone : SimulationPersona(
    key = "acceptsEveryPlan",
    title = "Accepts every plan, completes none",
    why = "The scold test. Section 12 requires a year in which no sentence references a " +
        "plan, a commitment, an intention or a failure to act.",
) {
    override val acceptsEveryPlan = true

    override val areas = listOf(
        SimulatedArea("work", "Work"),
        SimulatedArea("personal", "Personal"),
    )

    override fun act(log: SimulatorLog, day: Int) {
        // The thing a plan would be about. Captured, never promoted, never completed.
        if (day % DAYS_PER_WEEK == PLEDGE_DAY) {
            log.capture(day, PLEDGE_HOUR, "personal", itemTitle(day, "pledge"))
        }
        work(log, day, "work", captures = 1, completions = roll(day, "done", 2))
        if (roll(day, "personal", 5) == 0) work(log, day, "personal", captures = 1, label = "personalCapture")
    }

    private const val DAYS_PER_WEEK = 7
    private const val PLEDGE_DAY = 1
    private const val PLEDGE_HOUR = 10
}
