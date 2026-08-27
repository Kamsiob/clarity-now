package com.kamsiob.claritynow.domain.replay

import com.kamsiob.claritynow.data.event.AreaCreated
import com.kamsiob.claritynow.data.event.ClarityEvent
import com.kamsiob.claritynow.data.event.EventPayload
import com.kamsiob.claritynow.data.event.ItemAdded
import com.kamsiob.claritynow.data.event.ItemEdited
import com.kamsiob.claritynow.data.event.ItemEstimated
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The two optional fields, through the log. Addendum 01 4b and 4c, issues #25 and #26.
 *
 * The golden fixture already carries an item created with a first step and an estimate
 * and an estimate revised three days later, because those are part of the small life
 * it describes. **The three cases it does not reach are here**, and they are the ones
 * that would fail quietly: a first step added after the fact, a first step cleared, and
 * an estimate cleared. Each of the three is a null arriving where a value used to be,
 * which is the shape a projection gets wrong by not writing the field at all.
 *
 * Every case is asserted through a full replay rather than against the reducer
 * directly, and the last test replays the same events in four arrival orders, because
 * these fields will eventually cross a merge and a field that only survives one
 * ordering is not a field, it is a coincidence.
 */
class OptionalFieldsReplayTest {

    private var lamport = 0L

    private fun event(payload: EventPayload): ClarityEvent {
        lamport += 1
        return ClarityEvent.of(
            id = "evt-$lamport",
            wallClock = 1_772_000_000_000L + lamport,
            lamport = lamport,
            originId = "01947b3f-0000-4000-8000-00000000000b",
            payload = payload,
        )
    }

    private fun area() = event(AreaCreated("area-work", "Work", "#2D7FF9", "a0"))

    private fun added(firstStep: String? = null, estimateMinutes: Int? = null) = event(
        ItemAdded(
            itemId = "item-1",
            areaId = "area-work",
            title = "Rewrite the proposal intro",
            note = null,
            orderKey = "a0",
            areaNameSnapshot = "Work",
            estimateMinutes = estimateMinutes,
            firstStep = firstStep,
        ),
    )

    private fun edited(previousFirstStep: String?, newFirstStep: String?) = event(
        ItemEdited(
            itemId = "item-1",
            previousTitle = "Rewrite the proposal intro",
            newTitle = "Rewrite the proposal intro",
            previousNote = null,
            newNote = null,
            previousFirstStep = previousFirstStep,
            newFirstStep = newFirstStep,
        ),
    )

    private fun item(vararg events: ClarityEvent) =
        ClarityReplay.replay(events.toList()).items.getValue("item-1")

    // The first step ---------------------------------------------------------

    @Test
    fun `an item created with a first step keeps it`() {
        val state = item(area(), added(firstStep = "Open the doc and read what is there"))
        assertEquals("Open the doc and read what is there", state.firstStep)
    }

    @Test
    fun `an item created without one has none, and nothing invents one`() {
        // The absent case is the common one and it has to stay absent. A blank string
        // instead of a null would put an empty line on the card, which design-v3.md
        // 10.3 forbids by name: no placeholder, no dash, no reserved row.
        assertNull(item(area(), added()).firstStep)
    }

    @Test
    fun `a first step added later by an ordinary edit`() {
        val state = item(
            area(),
            added(),
            edited(previousFirstStep = null, newFirstStep = "Find last winter heating bill"),
        )
        assertEquals("Find last winter heating bill", state.firstStep)
    }

    @Test
    fun `a first step cleared by an ordinary edit`() {
        // Addendum 01 4b: it is deletable, and clearing it is an ordinary edit writing
        // an ordinary event. Nothing special marks the deletion, which is the point.
        val state = item(
            area(),
            added(firstStep = "Find last winter heating bill"),
            edited(previousFirstStep = "Find last winter heating bill", newFirstStep = null),
        )
        assertNull("clearing a first step left the old value behind", state.firstStep)
    }

    // The estimate -----------------------------------------------------------

    @Test
    fun `an estimate set at creation`() {
        assertEquals(90, item(area(), added(estimateMinutes = 90)).estimateMinutes)
    }

    @Test
    fun `an estimate changed later reads the new value and not the old`() {
        // The event carries the previous value for the record and the reducer does not
        // consult it, exactly as it does not consult a previous title. A projection
        // that read the before value would be right on the first revision and wrong on
        // every one after it.
        val state = item(
            area(),
            added(estimateMinutes = 90),
            event(ItemEstimated("item-1", previousEstimateMinutes = 90, newEstimateMinutes = 45)),
        )
        assertEquals(45, state.estimateMinutes)
    }

    @Test
    fun `an estimate cleared writes a null and the projection drops it`() {
        val state = item(
            area(),
            added(estimateMinutes = 90),
            event(ItemEstimated("item-1", previousEstimateMinutes = 90, newEstimateMinutes = null)),
        )
        assertNull("clearing an estimate left the old number behind", state.estimateMinutes)
    }

    @Test
    fun `an estimate set after capture, with no estimate at creation`() {
        val state = item(
            area(),
            added(),
            event(ItemEstimated("item-1", previousEstimateMinutes = null, newEstimateMinutes = 25)),
        )
        assertEquals(25, state.estimateMinutes)
    }

    // Arrival order ----------------------------------------------------------

    @Test
    fun `both fields reach the same value in any arrival order`() {
        // Straight, reversed, and delivered twice. The total order in the log decides
        // the result, never the order a merge happened to hand the events over in.
        val log = listOf(
            area(),
            added(firstStep = "Find last winter heating bill", estimateMinutes = 90),
            event(ItemEstimated("item-1", 90, 45)),
            edited(previousFirstStep = "Find last winter heating bill", newFirstStep = null),
        )
        val deliveries = listOf(log, log.reversed(), log.shuffled(), log + log)

        val states = deliveries.map { ClarityReplay.replay(it).items.getValue("item-1") }
        assertTrue(
            "the same log reached different states depending on arrival order",
            states.all { it == states.first() },
        )
        assertNull(states.first().firstStep)
        assertEquals(45, states.first().estimateMinutes)
    }
}
