package com.kamsiob.claritynow.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import com.kamsiob.claritynow.ui.theme.LocalClarityColors
import com.kamsiob.claritynow.ui.theme.LocalClarityTypography

/**
 * design-v3.md 10.12. A sentence case label followed by a hairline running to the
 * trailing edge, vertically centered on the label.
 *
 * Sentence case, never all caps. All caps section labels are on the tell list in
 * design-v3.md 15.1 and were removed from this design on purpose.
 */
@Composable
fun Sidehead(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = LocalClarityColors.current.inkSecondary,
    ruleColor: Color = LocalClarityColors.current.hairline,
) {
    val type = LocalClarityTypography.current
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start,
    ) {
        Text(text = text, style = type.sidehead, color = color)
        Box(
            modifier = Modifier
                .padding(start = 10.dp)
                .weight(1f)
                .height(1.dp)
                .drawBehind { drawRect(ruleColor) },
        )
    }
}

/**
 * The fixed width numeral treatment, design-v3.md 5.2.
 *
 * Hanken Grotesk ships no `tnum` feature, which the build time check anticipated,
 * so every updating numeric display lays its digits out in slots of the widest
 * digit's width instead of trusting the font. Non digits keep their natural width,
 * so a colon in a countdown still sits tight.
 *
 * The whole string is announced once to a screen reader rather than digit by digit.
 */
@Composable
fun TabularNumber(
    text: String,
    style: TextStyle,
    color: Color,
    modifier: Modifier = Modifier,
    contentDescription: String = text,
) {
    val measurer = rememberTextMeasurer()
    val density = LocalDensity.current
    val digitWidth = remember(style, density, measurer) {
        val widest = (0..9).maxOf { digit ->
            measurer.measure(digit.toString(), style).size.width
        }
        with(density) { widest.toDp() }
    }

    Row(
        modifier = modifier.clearAndSetSemantics { this.contentDescription = contentDescription },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        text.forEach { character ->
            if (character.isDigit()) {
                Box(modifier = Modifier.width(digitWidth), contentAlignment = Alignment.Center) {
                    Text(text = character.toString(), style = style, color = color, maxLines = 1)
                }
            } else {
                Text(text = character.toString(), style = style, color = color, maxLines = 1)
            }
        }
    }
}
