package com.kamsiob.claritynow.ui.nav

import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput

/**
 * Consumes every pointer event that reaches this element, so nothing drawn beneath it
 * can be touched.
 *
 * Consuming in the initial pass is what makes it total: the initial pass runs from the
 * root down, so by the time a change has been consumed here, no node below this one in
 * the tree and no sibling behind it will act on it. That is also why an element wearing
 * this must never be an ancestor of something that needs the pointer: **the only correct
 * use is a full size sibling drawn behind the surface it protects**, never a scrim over
 * it, or the surface's own controls go dead.
 *
 * **Three surfaces in this app cover another one and need it**, the Focus surface in
 * [ClarityShell], the re-entry state in `ui/reentry` and the archive in `ui/areas`,
 * which is why it is here rather than private to any of them. All three leave the thing
 * they cover composed underneath, for their own reasons: the Contemplative surface fades
 * in over the room it replaces, per design-v3.md 8.2 item 6, the re-entry state has the
 * app behind it so that a conflict card waits rather than being dropped, per
 * MASTER_BUILD_PROMPT 14b.4, and the archive is a pushed screen over the Areas tab that
 * has to hand that list back exactly where it was left. Composed and untouchable is the
 * state all three want, and this is that state.
 *
 * `OnboardingRoute` holds a third copy for the same job and is deliberately left alone:
 * it belongs to a phase that is closed and shipped, and moving it is an edit to a file
 * that nothing else in this change touches.
 */
internal fun Modifier.swallowsPointerInput(): Modifier = pointerInput(Unit) {
    awaitPointerEventScope {
        while (true) {
            awaitPointerEvent(PointerEventPass.Initial).changes.forEach { it.consume() }
        }
    }
}
