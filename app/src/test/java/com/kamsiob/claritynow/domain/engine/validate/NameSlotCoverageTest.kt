package com.kamsiob.claritynow.domain.engine.validate

import com.kamsiob.claritynow.domain.engine.catalog.SlotProduction
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * The two ends of the slot table, checked against each other.
 * CLARITY_LOGIC_ENGINE.md 8, checks 5 and 6.
 *
 * `SlotProduction` declares every slot the corpus uses and the fact behind it. A slot with
 * no `FactRef` there is a string rather than a number: an area name, an item title, or a
 * quoted response label. Those are precisely the slots checks 5 and 6 read, and this
 * asserts the validator's three sets account for all of them and for nothing else.
 *
 * **Without this, a new name slot fails silently in the worst direction.** Add
 * `{fourthArea}` to a corpus line and wire it into `SlotProduction`, and every check in
 * section 8 still passes: check 5 skips a slot key it does not recognize, so the one place
 * a stale or invented name would have been caught waves it through. Nothing fails, nothing
 * looks wrong, and the app has quietly lost a guard. A test comparing the two lists is the
 * only thing that notices.
 */
class NameSlotCoverageTest {

    @Test
    fun `every slot with no fact behind it is one the validator classifies`() {
        val textSlots = SlotProduction.SOURCES.filter { it.factRef == null }.map { it.slot }.toSet()
        val classified = ClarityValidator.AREA_NAME_SLOTS +
            ClarityValidator.ITEM_TITLE_SLOTS +
            ClarityValidator.RESPONSE_LABEL_SLOTS
        assertEquals(
            "SlotProduction and the validator disagree about which slots carry a string. A slot " +
                "on the left is one checks 5 and 6 would skip; one on the right no longer exists",
            textSlots,
            classified,
        )
    }
}
