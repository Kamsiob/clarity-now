package com.kamsiob.claritynow.domain.replay

import com.kamsiob.claritynow.data.event.ItemStatus

/**
 * The things that must be true of any state the reducer produces.
 * MASTER_BUILD_PROMPT 6.2.
 *
 * Checked in tests after every generated scenario, and available from the debug
 * menu against real data. Not checked on every write in a release build, because a
 * check that runs on the hot path either gets weakened or gets removed.
 *
 * **Every area scoped rule below is scoped to an area, which is not the tautology
 * it sounds like.** Since Addendum 01 an item may have no area at all, and an
 * unfiled item sits outside all of them: it is in no area's queue, it is not the
 * one active thing anywhere, and it shares an order key space with nothing. 6.2
 * states that qualifier in writing rather than by implication, because an invariant
 * that is silently conditional is an invariant nobody can check. What replaces the
 * area scoped rules for an unfiled item is one rule of its own: it is never ACTIVE
 * and never COMPLETED. DECISIONS.md C8.
 */
data class InvariantViolation(val rule: String, val detail: String)

object ClarityInvariants {

    fun check(state: ClarityState): List<InvariantViolation> {
        val violations = mutableListOf<InvariantViolation>()

        val liveAreaIds = state.areas.values
            .filter { it.deletedAt == null && !it.archived }
            .map { it.id }
            .toSet()

        for (areaId in liveAreaIds) {
            val active = state.items.values.filter {
                it.areaId == areaId && it.status == ItemStatus.ACTIVE && it.deletedAt == null
            }
            if (active.size > 1) {
                violations += InvariantViolation(
                    rule = "one active item per area",
                    detail = "area $areaId has ${active.size} active items: " +
                        active.joinToString { it.id },
                )
            }

            val queued = state.items.values.filter {
                it.areaId == areaId && it.status == ItemStatus.QUEUED && it.deletedAt == null
            }
            val keys = queued.map { it.orderKey }
            if (keys.size != keys.toSet().size) {
                violations += InvariantViolation(
                    rule = "queued items have distinct order keys",
                    detail = "area $areaId has duplicate order keys among ${keys.size} queued items",
                )
            }
        }

        for (item in state.items.values) {
            if (item.status == ItemStatus.COMPLETED && item.activeSince != null) {
                violations += InvariantViolation(
                    rule = "a completed item is never active",
                    detail = "item ${item.id} is completed and still carries activeSince",
                )
            }
            val itemAreaId = item.areaId
            // An unfiled item is not an item with a broken reference. It is the
            // inbox, and the only thing to check about it is that it has not
            // reached a state that only an area can give it.
            if (itemAreaId == null) {
                if (item.status == ItemStatus.ACTIVE || item.status == ItemStatus.COMPLETED) {
                    violations += InvariantViolation(
                        rule = "an unfiled item is never active or completed",
                        detail = "item ${item.id} has no area and is ${item.status}",
                    )
                }
            } else if (itemAreaId !in state.areas) {
                violations += InvariantViolation(
                    rule = "every filed item belongs to a known area",
                    detail = "item ${item.id} references unknown area $itemAreaId",
                )
            }
            if (!OrderKey.isValid(item.orderKey)) {
                violations += InvariantViolation(
                    rule = "order keys are well formed",
                    detail = "item ${item.id} has order key ${item.orderKey}",
                )
            }
        }

        for (area in state.areas.values) {
            if (!OrderKey.isValid(area.orderKey)) {
                violations += InvariantViolation(
                    rule = "order keys are well formed",
                    detail = "area ${area.id} has order key ${area.orderKey}",
                )
            }
        }

        // A deleted or archived entity never appears in a live projection.
        val projected = state.liveAreas.map { it.id }.toSet()
        val hidden = state.areas.values
            .filter { it.deletedAt != null || it.archived }
            .map { it.id }
            .toSet()
        val leaked = projected intersect hidden
        if (leaked.isNotEmpty()) {
            violations += InvariantViolation(
                rule = "hidden areas never appear in a live projection",
                detail = "leaked: ${leaked.joinToString()}",
            )
        }

        for (session in state.focusSessions.values) {
            if (session.outcome != FocusOutcome.RUNNING && session.endedAt == null) {
                violations += InvariantViolation(
                    rule = "an ended session records when it ended",
                    detail = "session ${session.id} is ${session.outcome} with no endedAt",
                )
            }
        }

        for ((dateKey, pulse) in state.pulses) {
            if (pulse.dateKey != dateKey) {
                violations += InvariantViolation(
                    rule = "pulses are keyed by their own dateKey",
                    detail = "entry under $dateKey carries ${pulse.dateKey}",
                )
            }
        }

        return violations
    }

    fun assertHolds(state: ClarityState) {
        val violations = check(state)
        check(violations.isEmpty()) {
            "state violates ${violations.size} invariant(s):\n" +
                violations.joinToString("\n") { "  ${it.rule}: ${it.detail}" }
        }
    }
}
