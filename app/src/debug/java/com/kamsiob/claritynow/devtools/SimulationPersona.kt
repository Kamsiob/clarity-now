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
 * **There is a twelfth and it is deliberately outside [ALL].** [SimulationPersona.CYCLICAL]
 * is asked for by `MASTER_BUILD_PROMPT.md` 14b.9 rather than by section 12, and it is the
 * proof of a gate rather than a measurement. Every number this project has recorded is
 * quoted against the eleven, so a twelfth inside that list would move all of them. Its own
 * declaration carries the argument.
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
 * ## Writing something down and getting it done are separate acts
 *
 * They were not, for the first four phases. Every persona reached the log through [work],
 * [work] takes a capture count and a completion count side by side, and every call site
 * wrote a completion count no greater than its capture count. Nobody chose that. It is what
 * two adjacent parameters invite, and the result was a set of eleven lives in which
 * `additions >= completions` held on every single day and therefore on every single week.
 * A person who clears a backlog on a Sunday is completely ordinary and the instrument could
 * not represent one, which made every silence reading taken through it a reading of the
 * instrument as much as of the thing measured.
 *
 * [clearOut] is the act [work] could not express: a sitting down that finishes and captures
 * nothing, whose size comes from what had piled up rather than from a literal. Four
 * personas have one, each shaped to that life. **The one that must never have one is
 * `acceptsEveryPlan`,** whose whole value is that it never completes.
 *
 * ## Nothing happens on a day nobody was there
 *
 * A persona used to be asked whether it opened the app and then asked what it did, and the
 * second question was asked every day regardless of the answer to the first. Two lives
 * wrote captures and completions onto days carrying no `APP_OPENED`, **which the real app
 * cannot produce.** [isPresentOn] is the one gate now, it covers the install day as well
 * as the ordinary ones, and every driver of a persona applies it: the simulator,
 * `ReportPersonaTest` and `CapacityGatePersonaTest`. Read that method for the whole
 * argument. Nothing in an [act] needs to repeat the test.
 *
 * ## What a persona is not
 *
 * It is not a test fixture aimed at a family. Nothing here reaches for a rule key or a
 * threshold, and no persona is shaped to make a particular sentence appear. A persona is a
 * plausible year of somebody's behavior, and which rules fire is the question the simulator
 * exists to answer rather than something arranged in advance. A persona built to trigger
 * `persistence` would prove that `persistence` can be triggered and nothing else.
 *
 * That applies to the clearing sessions above as strictly as to anything else, and it is
 * the reason none of them completes a count written here. `burst` wants three in an area in
 * a day and no session was sized to three; each is sized to what its person gets through in
 * one sitting, and what that produces is the measurement.
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

    /**
     * Whether the app is opened on this local day, as this life decides it.
     *
     * **Never called by a driver.** [isPresentOn] is the gate, it calls this, and the two
     * are separate so that the install day and the days before it are answered in one
     * place rather than in eleven overrides that each forget a different half.
     */
    open fun opensOn(day: Int): Boolean = true

    /**
     * Whether this person is there on this local day at all. **The one gate every driver
     * of a persona applies before writing anything into the log.**
     *
     * ## Why this is not just [opensOn]
     *
     * A persona used to be asked whether it opened the app and then asked what it did, and
     * the second question was asked on every day of the year regardless of the answer to
     * the first. So `sporadic` and `abandoning` wrote `ITEM_ADDED` and `ITEM_COMPLETED` on
     * days carrying no `APP_OPENED`, **which the real app cannot produce**: nothing is
     * captured, promoted, completed or focused on except through a screen, and the shell
     * writes the open marker on the first foreground of the day. Every measurement this
     * project has recorded was read through an instrument that could do that.
     *
     * It is the same class of defect as the persona set that could not finish a backlog,
     * and it is fixed the same way, in the instrument rather than in each life: a driver
     * that has to remember to ask two questions in the right order is a driver that will
     * one day ask one.
     *
     * ## The install day is always present, and that is the second half of the fix
     *
     * [setUp] writes `AREA_CREATED`, which is a screen gesture like any other, so an
     * install day the persona happened not to open would put the same impossible event one
     * line earlier than the ones this method exists to stop. **Setting the app up is an app
     * session**, so the install day answers true whatever [opensOn] says about it, and a
     * persona whose own plan had nothing for that day simply does nothing after the areas
     * exist. That is what installing an app and not adding anything looks like.
     */
    fun isPresentOn(day: Int): Boolean = when {
        day < installDay -> false
        day == installDay -> true
        else -> opensOn(day)
    }

    /**
     * What the person did on this local day, written into [log].
     *
     * **Called only on a day [isPresentOn] answers true for**, so nothing here needs to
     * repeat that test and nothing here may write an event the app could not have written.
     */
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
     * A sitting down to finish what piled up. No capture, only completion.
     *
     * **This exists because [work] could not express it.** Every call site of [work] passed
     * a completion count no greater than its capture count, so `additions >= completions`
     * held on every simulated day and therefore on every simulated week, and no simulated
     * life ever finished more than it wrote down. That was never a decision anybody made.
     * It is a shape of the writer rather than of the person: a completion count written as
     * a literal beside a capture count invites the two to be written together, and they
     * were, in all eleven personas.
     *
     * So this one takes its count from the world instead. The person works through what is
     * waiting, which is the queue plus whatever is already active, up to [upTo]. Nothing is
     * captured, because writing a new thing down is not part of clearing a backlog, and the
     * count is not a literal, because the whole defect was literals.
     *
     * [upTo] is how much of it this person gets through in one sitting, and it is stated at
     * every call site: a session with no bound is a person with no evening.
     */
    protected fun clearOut(log: SimulatorLog, day: Int, areaId: String, upTo: Int) {
        val active = if (log.activeItem(areaId) == null) 0 else 1
        val waiting = log.queueSize(areaId) + active
        if (waiting == 0) return
        work(log, day, areaId, completions = minOf(waiting, upTo))
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

        /**
         * The twelfth persona, and it is deliberately **not** in [ALL].
         * `MASTER_BUILD_PROMPT.md` 14b.9.
         *
         * [ALL] is section 12's enumeration and every measurement this project has ever
         * recorded is quoted against those eleven years: five silence readings, five
         * family coverage readings, the variant repeat baseline phase 9 is judged by, and
         * the pattern section's concentration. A twelfth life in that list would move
         * every one of those numbers, and a reader comparing the sixth measurement against
         * the fifth would be comparing two different instruments without being told.
         *
         * This persona is not a measurement. It is the proof of a gate, asked for by
         * 14b.9 and listed in section 17, and it belongs to the test that reads it.
         */
        val CYCLICAL: SimulationPersona = CyclicalDips

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

/**
 * Bursts with long gaps. The `quietDay`, `quietWeek` and `burst` shape.
 *
 * **Writing something down and getting something done are separate acts in this life, and
 * that separation is what makes a burst possible at all.** The old mood 0 captured two and
 * finished two, which is a small balanced day rather than the day somebody sat down, so the
 * persona named for `burst` peaked at two completions and could never reach the three its
 * own family asks for. Intake here is frequent and cheap, a thought written down when it
 * arrives; output is rare and expensive, an afternoon where the list is dealt with. How
 * much the afternoon gets through is whatever had accumulated, which is why [clearOut]
 * reads the queue rather than taking a number from here.
 *
 * **A session that falls on a day this person is not there does not happen, and is not
 * moved.** This life opens the app on 249 days of the 365 and its session roll comes up 50
 * times; 11 of those land on a day it is not there, so it has 39 clearing afternoons rather
 * than 50 once [isPresentOn] is applied. The eleven that went are afternoons somebody spent
 * on their list without opening the app, which the log has no way to hold. Deferring one to
 * the next day opened was the alternative and it was rejected: [roll] is a hash of the day
 * and of nothing else, which is the property this whole file rests on and the reason a
 * seeded generator was refused, and a pending session carried forward would make what
 * happens on a day depend on the days before it.
 */
private object Sporadic : SimulationPersona(
    key = "sporadic",
    title = "Sporadic",
    why = "Long gaps, then an afternoon where the list is dealt with. Feeds quietDay, " +
        "quietWeek, burst and comeback.",
) {
    override val areas = listOf(
        SimulatedArea("work", "Work"),
        SimulatedArea("home", "Home"),
    )

    override fun opensOn(day: Int): Boolean = roll(day, "open", 3) != 0

    override fun act(log: SimulatorLog, day: Int) {
        if (roll(day, "session", SESSION_ODDS) == 0) {
            clearOut(log, day, "work", upTo = SESSION_ITEMS)
            return
        }
        when (roll(day, "mood", 7)) {
            0 -> work(log, day, "work", captures = 2)
            1 -> work(log, day, "home", captures = 1, completions = 1)
            2 -> work(log, day, "work", captures = 1)
            else -> Unit
        }
    }

    /** About every ninth day. Often enough to be a rhythm, rare enough to still be a session. */
    private const val SESSION_ODDS = 9

    /** What one afternoon gets through. Usually more than piled up, so the list ends empty. */
    private const val SESSION_ITEMS = 5
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
        if (day == installDay) {
            log.createArea(day, FIRST_AREA_HOUR, "home", "Home", "#3E8E6E")
        }
        work(log, day, "work", captures = 1, completions = if (roll(day, "done", 3) == 0) 0 else 1)
        if (roll(day, "home", 3) == 0) work(log, day, "home", captures = 1)
    }

    private const val FIRST_AREA_HOUR = 9
}

/**
 * Two months on, five months off, then back. The `comeback`, `areaRevival` and `rebalance`
 * shape.
 *
 * **Coming back is a clearing job before it is anything else.** Two months of capture and
 * five months of nothing leave a list of about thirty things, most of them stale, and a
 * person who opened that and wrote a thirty first thing down before touching any of it is
 * not the person who came back. The first days back go on the old list, and they are the
 * one stretch in this year with output and no intake at all.
 *
 * It does not empty the list, deliberately. A return that cleared thirty items would be a
 * fantasy, and the residue is what `queuePressure` and `accumulation` still have to read
 * afterward.
 */
private object LongDormantRevival : SimulationPersona(
    key = "longDormantRevival",
    title = "Long dormant, then a revival",
    why = "Two months on, five months away, then back to a list nobody kept. Feeds " +
        "comeback, areaRevival, rebalance and the clearing a return begins with.",
) {
    override val areas = listOf(
        SimulatedArea("work", "Work"),
        SimulatedArea("music", "Music"),
    )

    override fun opensOn(day: Int): Boolean = day !in DORMANT

    override fun act(log: SimulatorLog, day: Int) {
        if (day in RETURN_CLEARING) {
            clearOut(log, day, "work", upTo = CLEARED_PER_EVENING)
            return
        }
        work(log, day, "work", captures = 1, completions = roll(day, "done", 2))
        if (roll(day, "music", 4) == 0) work(log, day, "music", captures = 1, completions = 1)
    }

    private const val RETURN_DAYS = 8

    private val DORMANT = 56..250

    /**
     * The day the app is opened again, and the week after it.
     *
     * Derived from [DORMANT] rather than written out, so moving the dormancy moves the
     * return with it instead of leaving a clearing week stranded in the middle of it.
     */
    private val RETURN_CLEARING = (DORMANT.last + 1)..(DORMANT.last + RETURN_DAYS)

    /** Three in an evening. Not the whole list, because most of a year old list is stale. */
    private const val CLEARED_PER_EVENING = 3
}

/**
 * Writes far more down than leaves. The `accumulation`, `queuePressure` and `growingQueues`
 * shape.
 *
 * Every capture goes into an area rather than into the inbox, deliberately. Addendum 01 4a
 * keeps unfiled captures out of `additionsPerArea` on purpose, so a hoarder who captured
 * into the inbox would produce a flat queue and none of the families this persona exists
 * to feed.
 *
 * **The pile does get dealt with sometimes, and it is still a pile afterward.** A person
 * who writes everything down and never once has the Saturday where the household list gets
 * worked through is a caricature rather than an extreme, and the purge below is the whole
 * difference between the two. It takes one area, `home`, and it takes a fraction of it:
 * `work` keeps its trickle of one at a time and `someday` is never touched, which is what
 * makes it a Someday list. Home refills faster than the purge empties it, so every family
 * this persona feeds still reads a queue that grew across the year.
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
        if (roll(day, "purge", PURGE_ODDS) == 0) clearOut(log, day, "home", upTo = PURGE_ITEMS)
    }

    /** About once a month. A person, not a caricature, but still a hoarder afterward. */
    private const val PURGE_ODDS = 26

    /** An afternoon of household jobs. The pile is much longer than this and stays longer. */
    private const val PURGE_ITEMS = 5
}

/**
 * Nothing sits at work, and the errands go in one trip. The `throughput`, `clearing` and
 * `queueDrained` shape.
 *
 * **An errand has a week in it, and that is the whole change.** Finishing each errand on
 * the day it occurred to you is not fast, it is implausible: nobody drives to the post
 * office because one letter came up. They go on the list, the list is run on Saturday, and
 * the list is empty again on Sunday. Work keeps the same day rhythm it always had, which is
 * the part of this person that is actually fast.
 *
 * How long the list is on Saturday is whatever the week put on it, at the rate it always
 * captured errands at. Some weeks that is one and the trip is barely a trip.
 */
private object FastCompleter : SimulationPersona(
    key = "fastCompleter",
    title = "Fast completer",
    why = "Work finished the day it was captured, errands finished in one trip on Saturday. " +
        "Feeds throughput, clearing, burst and queueDrained.",
) {
    override val areas = listOf(
        SimulatedArea("work", "Work"),
        SimulatedArea("errands", "Errands"),
    )

    override fun act(log: SimulatorLog, day: Int) {
        val captures = 1 + roll(day, "captures", 2)
        work(log, day, "work", captures = captures, completions = captures)
        if (day % DAYS_PER_WEEK == ERRAND_RUN_DAY) {
            clearOut(log, day, "errands", upTo = ERRANDS_PER_TRIP)
            return
        }
        if (roll(day, "errands", 3) == 0) work(log, day, "errands", captures = 1)
    }

    private const val DAYS_PER_WEEK = 7

    /** Saturday. Day zero of a run is a Sunday, per `ClaritySimulator.DEFAULT_START_DATE`. */
    private const val ERRAND_RUN_DAY = 6

    /** A morning of errands, which is longer than the list usually is. */
    private const val ERRANDS_PER_TRIP = 6
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

/**
 * A life that runs in cycles of its own, none of them the same size.
 * `MASTER_BUILD_PROMPT.md` 14b.9, Addendum 01 7b.
 *
 * ## Why this one exists
 *
 * A fluctuating condition and a decline are the same numbers. Both are a fall in
 * completions, a rise in idle days, an area going quiet. Without the capacity gate the app
 * tells this person that they are deteriorating **on a fixed schedule, forever, and it is
 * technically accurate every single time**: every individual report passes every integrity
 * rule, and the claim the sequence makes across the year is still false, because the shape
 * being read is a cycle and the app has read half of one.
 *
 * So this is the one persona whose value is what the engine does **not** say about it.
 * `CapacityGatePersonaTest` composes its year twice and the control run is the finding.
 *
 * ## It is a life and deliberately not a waveform
 *
 * A clean period would pass the test and prove nothing, because the precedent fact would
 * be matching on a shape no person produces. **The bad stretches here vary in depth, vary
 * in length, and start on no fixed day of any month**, the good stretches between them are
 * of different sizes, and no two are the same height. [YEAR] is fifty three weeks of
 * capacity written out one at a time for that reason: the irregularity is the thing being
 * modeled, and a generator with a period in it would have hidden the very defect this
 * persona exists to catch.
 *
 * What is not left to chance is the **band** each bad week falls in. `Precedent` compares
 * two falls by depth band rather than by exact depth, and every low week in this year is
 * under half of this person's normal, which is the band that makes them comparable at all.
 * A year of dips scattered across two bands would be a year in which half of them are
 * precedents for nothing, which is a different persona and a fair one to build later.
 *
 * ## Why the whole year is silent, which is not the same as the gate closing all year
 *
 * 14b.9 asks for a year with **no** decline, neglect or fading observation in it, and the
 * app cannot know a fall is familiar until it has seen its like. Both are true here, and
 * the reason is that the two definitions of a bad week are not the same width:
 *
 * - **`Precedent`'s low is a week under three quarters of normal.** No decline family asks
 *   that question. `quietWeek` needs a week holding fewer events than it has days,
 *   `decliningActivity` needs three weeks falling strictly, `neglectedArea` needs seven
 *   days of silence in an area with a real history behind it
 * - So a week can be squarely inside a fall by the fact's reckoning and reach no family's
 *   bar at all. **The first eleven weeks here are made of exactly those weeks**: three bad
 *   stretches, one of them three weeks long, none of them steep enough or quiet enough or
 *   long enough for anything to be said about, and no two of them adjacent in a way that
 *   makes three weeks fall in a row
 *
 * By the twelfth week, which is `Precedent.MIN_HISTORY_WEEKS` and the first week the fact
 * answers at all, this person has a fall of every length and depth the rest of the year
 * contains. Everything after it is familiar, and everything before it was beneath notice.
 *
 * **The household list carries the same story and needs it more.** `home` is created on the
 * first day and not opened until the fourth week, because the first fortnight goes entirely
 * on work, which is ordinary and is also the only window in which an area can be silent
 * without `neglectedArea` being entitled to say so: under five lifetime events and under a
 * fortnight old, both of that family's own guards are shut. That fortnight is what every
 * later quiet stretch of `home` is measured against, and it is why none of them runs longer
 * than the two weeks it holds.
 */
private object CyclicalDips : SimulationPersona(
    key = "cyclicalDips",
    title = "Cyclical, and never twice the same",
    why = "A fluctuating condition reads as a decline in the data. Proves the capacity " +
        "gate in 14b.9 removes the decline families rather than re-wording them.",
) {

    override val areas = listOf(
        SimulatedArea("work", "Work"),
        SimulatedArea("home", "Home"),
    )

    override fun opensOn(day: Int): Boolean = day % DAYS_PER_WEEK in shapeOf(day / DAYS_PER_WEEK).present

    override fun act(log: SimulatorLog, day: Int) {
        val shape = shapeOf(day / DAYS_PER_WEEK)
        val dayOfWeek = day % DAYS_PER_WEEK
        // The install day reaches here whether or not this person had anything planned for
        // it, per `isPresentOn`. Setting the app up and adding nothing that day is a real
        // first day and the areas exist either way.
        if (dayOfWeek !in shape.present) return
        work(
            log,
            day,
            "work",
            captures = 1,
            completions = if (dayOfWeek in shape.finishing) 1 else 0,
            focusMinutes = if (dayOfWeek in shape.sittings) SITTING_MINUTES else 0,
        )
        if (dayOfWeek in shape.household) work(log, day, "home", captures = 1, completions = 1)
        // The last day of a good week is when the list that piled up gets worked through.
        // `clearOut` takes its size from what is waiting, per the note on that method.
        if (shape.clearsUpTo > 0 && dayOfWeek == shape.lastPresentDay) {
            clearOut(log, day, "work", shape.clearsUpTo)
        }
    }

    /**
     * How much of a week this person has, and what they spend it on.
     *
     * The weekly event count each of these produces is stated because it is the quantity
     * every fact in 14b.9 is computed from, and a reader changing one of these numbers is
     * changing where a week falls against `Precedent`'s bands. Normal across this year is
     * about twenty four events a week, so **everything from [TAPER] down is a low week and
     * everything from [EBB] down is a deep one**, and `quietWeek` reaches the ones under
     * seven.
     */
    private enum class Capacity(
        val presentDays: Int,
        val finishingDays: Int,
        val sittings: Int,
        val householdDays: Int,
        val clearsUpTo: Int,
        /** Whether the week's size moves. False for the low weeks, whose depth is the point. */
        val varies: Boolean = true,
    ) {

        /** About 32 to 39 events. Everything gets done and the list gets cleared. */
        PEAK(presentDays = 6, finishingDays = 4, sittings = 3, householdDays = 2, clearsUpTo = 4),

        /** About 25 to 29, and not one of them in the house. The first three weeks. */
        HEADS_DOWN(presentDays = 7, finishingDays = 5, sittings = 2, householdDays = 0, clearsUpTo = 2),

        /** About 28 to 33. A good week without the clear out being a big one. */
        FULL(presentDays = 6, finishingDays = 4, sittings = 2, householdDays = 2, clearsUpTo = 2),

        /** About 24 to 25. The ordinary week this person's normal is made of. */
        STEADY(presentDays = 6, finishingDays = 4, sittings = 2, householdDays = 2, clearsUpTo = 0),

        /** About 21 to 22. A quieter ordinary week, still nowhere near low. */
        PLAIN(presentDays = 5, finishingDays = 3, sittings = 2, householdDays = 2, clearsUpTo = 0),

        /** 13. Under three quarters of normal and over half of it: low, and not deep. */
        TAPER(
            presentDays = 4, finishingDays = 2, sittings = 1, householdDays = 1,
            clearsUpTo = 0, varies = false,
        ),

        /** 8. Deep, and still well clear of the bar `quietWeek` sets. */
        EBB(
            presentDays = 5, finishingDays = 0, sittings = 0, householdDays = 1,
            clearsUpTo = 0, varies = false,
        ),

        /** 7. The same, one lower. Things get written down and none of them get done. */
        LULL(
            presentDays = 4, finishingDays = 0, sittings = 0, householdDays = 1,
            clearsUpTo = 0, varies = false,
        ),

        /** 4. Quiet by `quietWeek`'s reckoning, and the house does not get looked at. */
        SPARSE(
            presentDays = 4, finishingDays = 0, sittings = 0, householdDays = 0,
            clearsUpTo = 0, varies = false,
        ),

        /** 2. Two days in the whole week, one thought written down on each. */
        FLAT(
            presentDays = 2, finishingDays = 0, sittings = 0, householdDays = 0,
            clearsUpTo = 0, varies = false,
        ),

        /** 1. One day, one thing. Never nothing: a week with nothing in it is its own band. */
        STILL(
            presentDays = 1, finishingDays = 0, sittings = 0, householdDays = 0,
            clearsUpTo = 0, varies = false,
        ),
    }

    /** Which days of one week are which. Built once per week and read on each of its days. */
    private class WeekShape(
        val present: Set<Int>,
        val finishing: Set<Int>,
        val sittings: Set<Int>,
        val household: Set<Int>,
        val clearsUpTo: Int,
        val lastPresentDay: Int,
    )

    /**
     * Fifty three weeks of capacity, written out rather than generated.
     *
     * Read it as a sequence of twelve episodes. **Two of them fall inside the first eleven
     * weeks**, where nothing the app has can be said out loud: one bad week on its own, and
     * three weeks later a stretch of three. Those two are what the whole rest of the year
     * is compared against, and they are the reason a fall of any length this year holds has
     * a twin behind it.
     *
     * The rest run for one, two or three weeks, arrive after gaps of two to six good weeks,
     * and bottom out anywhere between thirteen events in the week and one. No two recoveries
     * are the same height either: a stretch is followed by a peak in one place and by an
     * ordinary week in another.
     *
     * **What is deliberately regular, and what it costs.** An episode begins on a week
     * boundary, because a seven day bucket anchored on the window end is the grain every
     * fact in `CLARITY_LOGIC_ENGINE.md` 3.1 is computed at, and an episode that straddled
     * one would reach the precedent walk as two short falls where the person had one long
     * one. That is a real shape and a real risk, and it belongs to a persona of its own
     * rather than to this one: the reading it would test is whether a fall is recognized
     * when it is out of phase with the grid, which is a question about the fact and not
     * about the gate. What does move inside a week is which days the person is there, so
     * two weeks of the same capacity are the same size and never the same week.
     */
    private val YEAR: List<Capacity> = listOf(
        // The first three weeks go entirely on work. The household list is created and not
        // opened, which is the only fortnight in which an area may be silent without
        // `neglectedArea` being entitled to say so, and is what its later quiet stretches
        // are measured against.
        Capacity.HEADS_DOWN, Capacity.HEADS_DOWN, Capacity.HEADS_DOWN,
        // A strong week, then one bad week on its own, then back.
        Capacity.PEAK, Capacity.EBB, Capacity.STEADY,
        // Three weeks under, the longest stretch of the year, and the shallowest.
        Capacity.PEAK, Capacity.LULL, Capacity.EBB, Capacity.EBB, Capacity.FULL,
        // Week eleven. From here a precedent is answerable and every fall has one behind it.
        Capacity.PEAK, Capacity.STEADY, Capacity.SPARSE, Capacity.FULL,
        Capacity.PLAIN, Capacity.FULL,
        // The first one with a slope into it rather than a step.
        Capacity.TAPER, Capacity.FLAT, Capacity.STEADY, Capacity.PEAK,
        Capacity.PLAIN, Capacity.EBB, Capacity.STEADY, Capacity.FULL,
        // Three weeks, and this time each one lower than the last.
        Capacity.LULL, Capacity.SPARSE, Capacity.FLAT, Capacity.PLAIN,
        Capacity.PEAK, Capacity.STEADY,
        Capacity.TAPER, Capacity.FULL,
        // The deepest single week in the year, and it comes out of a good one.
        Capacity.STILL, Capacity.PLAIN, Capacity.PEAK,
        // A fortnight, flat rather than falling.
        Capacity.EBB, Capacity.EBB, Capacity.STEADY, Capacity.FULL,
        Capacity.PLAIN, Capacity.SPARSE, Capacity.PEAK, Capacity.STEADY,
        // The last long one, and the recovery from it is the smallest of the year.
        Capacity.TAPER, Capacity.FLAT, Capacity.SPARSE, Capacity.FULL,
        Capacity.PLAIN, Capacity.PEAK, Capacity.LULL, Capacity.STEADY, Capacity.PLAIN,
    )

    /**
     * Every week's shape, built once.
     *
     * A shape is read on each of the seven days of its week and again by [opensOn] for
     * each of them, so building it per call would be fourteen sorts a day for a year. It
     * is a pure function of [YEAR] and of [key], so holding it changes no answer.
     */
    private val shapes: List<WeekShape> by lazy { YEAR.indices.map { buildShape(it) } }

    /** A run longer than [YEAR] holds ends the way the year ended rather than repeating it. */
    private fun shapeOf(week: Int): WeekShape = shapes[week.coerceAtMost(shapes.lastIndex)]

    /**
     * Which days of [week] this person is there, finishes something, sits down to focus,
     * and does something in the house.
     *
     * **The days move, and that is the half of the brief a level table cannot carry.** A
     * capacity says how much of a week there is; where in the week it falls is a hash of
     * the week, so no episode starts on the same weekday and none lands on the same day of
     * a month. Two weeks of the same capacity are the same size and are not the same week.
     */
    private fun buildShape(week: Int): WeekShape {
        val capacity = YEAR[week]
        val stretch = StableHash.bucket("$key|w$week|stretch", STRETCH_OUTCOMES)
        val presentCount = if (capacity.varies && stretch == STRETCH_A_DAY) {
            (capacity.presentDays + 1).coerceAtMost(DAYS_PER_WEEK)
        } else {
            capacity.presentDays
        }
        val finishingCount = if (capacity.varies && stretch == STRETCH_A_FINISH) {
            capacity.finishingDays + 1
        } else {
            capacity.finishingDays
        }
        val present = pick(week, "present", (0 until DAYS_PER_WEEK).toList(), presentCount)
        val finishing = pick(week, "finishing", present, finishingCount)
        val unfinished = present.filterNot { it in finishing }
        return WeekShape(
            present = present.toSet(),
            finishing = finishing.toSet(),
            // A sitting goes on a day nothing is finished, because `work` runs the session
            // after the completions and a completion that empties the queue leaves the area
            // with nothing active for a session to be about.
            sittings = pick(week, "sitting", unfinished, capacity.sittings).toSet(),
            household = pick(week, "household", present, capacity.householdDays).toSet(),
            clearsUpTo = if (capacity.clearsUpTo == 0) {
                0
            } else {
                capacity.clearsUpTo + StableHash.bucket("$key|w$week|clear", CLEAR_SPREAD)
            },
            lastPresentDay = present.lastOrNull() ?: -1,
        )
    }

    /** The [count] days of [pool] this week gives to [label], in calendar order. */
    private fun pick(week: Int, label: String, pool: List<Int>, count: Int): List<Int> =
        pool.sortedBy { StableHash.spread("$key|w$week|$label$it") }
            .take(count.coerceAtMost(pool.size))
            .sorted()

    private const val DAYS_PER_WEEK = 7

    /** One sitting, the same length whatever the week, so the count is what varies. */
    private const val SITTING_MINUTES = 30

    /**
     * A week of a given capacity is one of three sizes: itself, itself with a day in it,
     * or itself with one more thing finished. One roll, three outcomes, so no two weeks of
     * the same capacity read as a repeat of each other in the dump.
     */
    private const val STRETCH_OUTCOMES = 3
    private const val STRETCH_A_DAY = 0
    private const val STRETCH_A_FINISH = 2

    /** How much a clearing evening varies. Three sizes, none of them a threshold. */
    private const val CLEAR_SPREAD = 3
}
