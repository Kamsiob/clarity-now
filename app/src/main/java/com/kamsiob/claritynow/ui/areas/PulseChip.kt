package com.kamsiob.claritynow.ui.areas

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.kamsiob.claritynow.R
import com.kamsiob.claritynow.ui.components.ClarityChip
import com.kamsiob.claritynow.ui.theme.LocalClarityColors

/**
 * The Pulse chip in the Areas header. design-v3.md 10.1, and 13 for the second signal.
 *
 * An ordinary [ClarityChip], the same one the Focus chip is, with one thing added: the
 * 6dp `warnAmber` dot at its top right on a day the Pulse is ready and unanswered.
 * design-v3.md 3.1 scopes that token to "the Pulse ready dot, nothing else", and this
 * is that one use. It is also the only dot in this row: the inbox chip carries a count
 * in its label and never a badge, per 10.16 and Addendum 01 4a.
 *
 * **The dot is not the signal on its own, and the label is not decoration.**
 * design-v3.md 13 requires both, in the same sentence that says color is never the only
 * signal: a person who cannot separate amber from the chip behind it, and a person
 * listening to TalkBack, both read the same state off the label. So the chip reads
 * `Pulse` at rest and `Today's Pulse` when there is one waiting.
 *
 * **Why `Today's Pulse` rather than `Pulse ready`.** design-v3.md 15 asks for the
 * obvious answer to be identified and then beaten. The obvious answers are a status
 * word appended to the label, `Pulse ready`, or a count, `Pulse 1`, which is the shape
 * the inbox chip uses. Both report on the person rather than name the destination, and
 * a count of one is a number nobody needs. `Today's Pulse` is the name
 * `MASTER_BUILD_PROMPT.md` 13.5 already gives this destination for the app shortcut
 * that opens it, so the two entry points into the same surface say the same words, and
 * it reads as an invitation rather than as a notice that something is outstanding.
 *
 * **Nothing here says whether the person has answered**, beyond the presence of the
 * dot that 10.1 specifies. There is no content description added over the label for the
 * same reason: any wording for the dot would be a sentence about an unanswered
 * question, and `MASTER_BUILD_PROMPT.md` 11.6 says not answering is never chased, never
 * counted and never mentioned. The changed label is the whole of what is said.
 *
 * **The chip is permanent and it never disappears.** 10.1 names two permanent chips.
 * On a silent day, and before the first Pulse has ever been generated, this is an
 * ordinary chip with no dot that opens the Pulse surface in whatever state it is in,
 * which is what 13.5 asks of the shortcut beside it.
 */
@Composable
internal fun PulseChip(ready: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = LocalClarityColors.current
    Box(modifier = modifier) {
        ClarityChip(
            label = stringResource(
                if (ready) R.string.areas_chip_pulse_ready else R.string.areas_chip_pulse,
            ),
            onClick = onClick,
        )

        if (ready) {
            // **Deliberately not animated in.** The dot and the label are one signal
            // stated twice, per design-v3.md 13, and the label cannot fade without a
            // crossfade of the whole chip. Fading one and snapping the other would
            // split a single state change into two events a quarter of a second apart,
            // which is the reading a person then has to reconcile. Both arrive in the
            // same frame instead, which is also the frame the generation on first
            // foreground finishes in, and by then the header has already entered.
            //
            // `warnAmber` is taken straight from the token and not through
            // `calmed`: design-v3.md 16.2's exclusion list names it, along with
            // actionBlue, positiveGreen, deleteMuted and the two area places, because
            // each of those is scoped to exactly one job and desaturating it would
            // make that job harder to see.
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = DOT_TOP_INSET, end = DOT_END_INSET)
                    .size(DOT_SIZE)
                    .clip(CircleShape)
                    .background(colors.warnAmber),
            )
        }
    }
}

/** design-v3.md 10.1 states the size. */
private val DOT_SIZE = 6.dp

/**
 * Where the dot sits, measured against the chip's touch target rather than its pill.
 *
 * [ClarityChip] draws a 38dp pill centered inside a 48dp touch target, which
 * design-v3.md 13 requires and 10.8 sizes, so the pill's own top edge is 5dp below the
 * top of the box this aligns against. These two insets put the dot's center about 4dp
 * inside the pill's upper right arc, which is where "at its top right" lands on a shape
 * with no corner, and they keep the whole dot inside the chip's bounds so a
 * horizontally scrolling row cannot clip it.
 *
 * At a large font scale the pill grows past 48dp and fills the box, and the dot stays
 * where it is: near the top right, further from the arc, still inside. Nothing here
 * assumes the pill is exactly 38dp tall.
 */
private val DOT_TOP_INSET = 6.dp
private val DOT_END_INSET = 8.dp
