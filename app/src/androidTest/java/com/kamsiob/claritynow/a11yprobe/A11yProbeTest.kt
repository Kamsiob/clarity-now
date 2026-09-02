package com.kamsiob.claritynow.a11yprobe

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.printToString
import androidx.compose.ui.unit.dp
import com.kamsiob.claritynow.ui.components.ClarityTextField
import com.kamsiob.claritynow.ui.components.UndoRequest
import com.kamsiob.claritynow.ui.components.UndoSnackbar
import com.kamsiob.claritynow.ui.components.clarityClickable
import org.junit.Rule
import org.junit.Test

class A11yProbeTest {

    @get:Rule
    val rule = createComposeRule()

    @Test
    fun cardShape() {
        rule.setContent {
            Box(
                modifier = Modifier.semantics(mergeDescendants = true) {
                    customActions = listOf(
                        CustomAccessibilityAction("Complete") { true },
                        CustomAccessibilityAction("Swap") { true },
                        CustomAccessibilityAction("Delete") { true },
                    )
                },
            ) {
                Box(modifier = Modifier.clarityClickable(haptic = null) { }) {
                    Column(
                        modifier = Modifier.clearAndSetSemantics {
                            contentDescription = "Work. The one thing in Work"
                        },
                    ) {
                        Text("Work")
                        Text("The one thing in Work")
                    }
                }
            }
        }
        println("MERGED_TREE_START")
        println(rule.onRoot(useUnmergedTree = false).printToString(Int.MAX_VALUE))
        println("MERGED_TREE_END")
    }

    @Test
    fun fieldShape() {
        rule.setContent {
            var value by remember { mutableStateOf("Buy milk") }
            Column {
                ClarityTextField(value = value, onValueChange = { value = it }, label = "Title")
            }
        }
        println("FIELD_TREE_START")
        println(rule.onRoot(useUnmergedTree = false).printToString(Int.MAX_VALUE))
        println("FIELD_TREE_END")
    }

    @Test
    fun undoShape() {
        rule.setContent {
            UndoSnackbar(
                request = UndoRequest(
                    id = "a",
                    message = "Completed",
                    actionLabel = "Undo",
                    onCommit = {},
                ),
                onDismiss = {},
                modifier = Modifier.fillMaxWidth(),
            )
        }
        rule.mainClock.advanceTimeBy(600)
        println("UNDO_TREE_START")
        println(rule.onRoot(useUnmergedTree = false).printToString(Int.MAX_VALUE))
        println("UNDO_TREE_END")
    }

    @Test
    fun plainClickableWithChildText() {
        rule.setContent {
            Box(modifier = Modifier.size(100.dp).clarityClickable(haptic = null) { }) {
                Text("Focus")
            }
        }
        println("PLAIN_TREE_START")
        println(rule.onRoot(useUnmergedTree = false).printToString(Int.MAX_VALUE))
        println("PLAIN_TREE_END")
    }

    @Test
    fun bareFieldWithContentDescription() {
        rule.setContent {
            var value by remember { mutableStateOf("Buy milk") }
            BasicTextField(
                value = value,
                onValueChange = { value = it },
                modifier = Modifier.semantics { contentDescription = "Title" },
            )
        }
        println("BARE_FIELD_START")
        println(rule.onRoot(useUnmergedTree = false).printToString(Int.MAX_VALUE))
        println("BARE_FIELD_END")
    }
}
