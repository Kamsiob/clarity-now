package com.kamsiob.claritynow.domain.engine

import com.kamsiob.claritynow.domain.engine.realize.Candidate

/**
 * A candidate that passed all ten checks in CLARITY_LOGIC_ENGINE.md 8.
 * Declared in `domain.engine` per 2.1, in the validation slice because layer 5 is the only
 * thing that may construct one.
 *
 * **Nothing else in the engine makes one of these.** That is the whole value of the type.
 * Layer 6 takes a `List<Validated>` so that it cannot advise on a sentence that was vetoed,
 * and 10.4 rule 2 turns on exactly that: a plan may only be produced when the observation
 * motivating it **actually appeared**, which is enforced by passing only the validated
 * observations in rather than by a check somebody has to remember to write.
 */
data class Validated(val candidate: Candidate)
