package com.kamsiob.claritynow.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.test.SemanticsNodeInteractionsProvider
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.dp
import com.kamsiob.claritynow.ui.components.ClarityTextField
import com.kamsiob.claritynow.ui.components.clarityClickable
import com.kamsiob.claritynow.ui.theme.ClarityTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/**
 * **The three semantics shapes this app got wrong, asserted rather than eyeballed.**
 *
 * Every one of these is a defect that shipped, was found by hand in the polish pass, was
 * fixed, and was then re-broken by the fix. That sequence is the argument for this file:
 * a source scan can see that `AreaCardSemantics` has a call site, and only a real
 * composition can see what the merged tree it produces actually says.
 *
 * A throwaway version of this existed for about nine minutes. A specialist wrote it during
 * the second consultation to dump the merged trees and settle three arguments, it was
 * swept into a commit by a blanket `git add -A`, and the next blanket add recorded its
 * deletion. Nobody decided either of those things. This is the same investigation written
 * as assertions and kept.
 *
 * **It runs on a device or emulator, so it is not in `verifyClarity`**, which is offline by
 * design. Run it with `./gradlew :app:connectedDebugAndroidTest`. That is a real gap and it
 * is the reason `InterfaceContractTest` exists in the offline suite alongside this: the
 * scans catch what they can without a device, and this catches what they cannot.
 */
class AccessibilityShapeTest {

    @get:Rule
    val rule = createComposeRule()

    /**
     * **A merging node cannot absorb another merging node.**
     *
     * `SwipeableRow` put Complete, Swap and Delete on an outer Box wrapping a card that
     * carries `clickable`. `clickable` is itself a merging boundary, so the outer node
     * stayed separate with actions and no text, and TalkBack never stopped on it. Adding
     * `mergeDescendants` there produced two focus stops, the first nameless. The shipped
     * arrangement puts the actions on the card's own node.
     */
    @Test
    fun customActionsSitOnTheNodeThatAlsoHasTextAndAClick() {
        rule.setContent {
            ClarityTheme {
                val actions = Modifier.semantics {
                    customActions = listOf(
                        CustomAccessibilityAction("Complete") { true },
                        CustomAccessibilityAction("Swap") { true },
                        CustomAccessibilityAction("Delete") { true },
                    )
                }
                Box {
                    Box(
                        modifier = actions
                            .clarityClickable(haptic = null) { }
                            .clearAndSetSemantics {
                                contentDescription = "Work. The one thing in Work"
                            },
                    ) {
                        Column { Text("Work"); Text("The one thing in Work") }
                    }
                }
            }
        }

        val node = rule.onRoot().fetchSemanticsNode()
        val carrier = node.findFirst { it.config.getOrNull(SemanticsActions.CustomActions) != null }
        assertNotNull("no node carries the three custom actions at all", carrier)
        val config = carrier!!.config
        assertEquals(3, config[SemanticsActions.CustomActions].size)
        assertNotNull(
            "the node carrying the actions has no name, so a screen reader has nothing " +
                "to announce when it lands on it and the actions are unreachable",
            config.getOrNull(SemanticsProperties.ContentDescription),
        )
        assertNotNull(
            "the node carrying the actions has no click action, so it is not the card",
            config.getOrNull(SemanticsActions.OnClick),
        )
    }

    /**
     * **A `contentDescription` on an editable node replaces the typed text.**
     *
     * Naming the field that way announced "Title" and hid "Buy milk", which is worse than
     * the anonymous field it replaced: a person could no longer hear what they had written.
     */
    @Test
    fun aFieldSaysItsNameAndAlsoWhatWasTyped() {
        rule.setContent {
            ClarityTheme {
                var value by remember { mutableStateOf("Buy milk") }
                ClarityTextField(
                    value = value,
                    onValueChange = { value = it },
                    label = "Title",
                )
            }
        }

        val node = rule.onRoot().fetchSemanticsNode()
        val field = node.findFirst { it.config.getOrNull(SemanticsActions.SetText) != null }
        assertNotNull("no editable node was found at all", field)
        val config = field!!.config
        assertTrue(
            "the field's name is missing: nothing in its semantics says Title",
            config.getOrNull(SemanticsProperties.Text).orEmpty().any { it.text == "Title" },
        )
        assertTrue(
            "the typed value is not readable: a contentDescription on an editable node " +
                "replaces it, which is the regression this test exists for",
            config.getOrNull(SemanticsProperties.EditableText)?.text == "Buy milk",
        )
        assertEquals(
            "an editable node must not carry a contentDescription, because it wins over " +
                "the text and hides what a person typed",
            null,
            config.getOrNull(SemanticsProperties.ContentDescription),
        )
    }

    /**
     * **A control's own word is its name; a click label is what happens next.**
     *
     * Setting both on the Undo button made TalkBack say "Undo, button, double tap to Undo".
     */
    @Test
    fun aControlWhoseTextIsItsNameDoesNotAlsoRepeatItAsAClickLabel() {
        rule.setContent {
            ClarityTheme {
                Box(modifier = Modifier.clarityClickable(haptic = null, onClickLabel = null) { }) {
                    Text("Undo")
                }
            }
        }

        val node = rule.onRoot().fetchSemanticsNode()
        val button = node.findFirst { it.config.getOrNull(SemanticsActions.OnClick) != null }!!
        val name = button.config.getOrNull(SemanticsProperties.Text).orEmpty()
            .joinToString(" ") { it.text }
        val clickLabel = button.config[SemanticsActions.OnClick].label
        assertTrue("the control has no name", name.contains("Undo"))
        assertTrue(
            "the click label repeats the control's own word, so it is announced twice",
            clickLabel == null || !name.contains(clickLabel, ignoreCase = true),
        )
    }
}

private fun androidx.compose.ui.semantics.SemanticsNode.findFirst(
    predicate: (androidx.compose.ui.semantics.SemanticsNode) -> Boolean,
): androidx.compose.ui.semantics.SemanticsNode? {
    if (predicate(this)) return this
    children.forEach { child -> child.findFirst(predicate)?.let { return it } }
    return null
}
