package com.kamsiob.claritynow.domain.engine

import kotlinx.serialization.Serializable

/**
 * Names the fact a rendered number came from, so the validator can re-read that
 * fact and compare. CLARITY_LOGIC_ENGINE.md 2.1.
 *
 * A number without one of these never reaches a template. That is the whole
 * defense against an invented count, and it is checked rather than trusted.
 */
@Serializable
data class FactRef(val category: String, val path: String) {
    override fun toString(): String = "$category.$path"
}
