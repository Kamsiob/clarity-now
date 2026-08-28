package com.kamsiob.claritynow.ui.pulse

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.kamsiob.claritynow.R
import com.kamsiob.claritynow.ui.components.ClarityIcon
import com.kamsiob.claritynow.ui.components.ClarityIcons
import com.kamsiob.claritynow.ui.components.clarityClickable
import com.kamsiob.claritynow.ui.components.clarityFocusRing
import com.kamsiob.claritynow.ui.components.clarityPressScale
import com.kamsiob.claritynow.ui.theme.ClaritySpacing
import com.kamsiob.claritynow.ui.theme.LocalCalmMode
import com.kamsiob.claritynow.ui.theme.LocalClarityTypography
import com.kamsiob.claritynow.ui.theme.LocalContemplativeColors
import com.kamsiob.claritynow.ui.theme.PulsePalette
import com.kamsiob.claritynow.ui.theme.calmed
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Past pulses. design-v3.md 10.15, "the History row in Pulse ambient mode", left by back.
 *
 * Every day the app has spoken, newest first: the date, the observation, the question,
 * and the answer where there was one. **Every string on this page came out of the engine
 * and was stored on an event.** The observation and the question are the rendered strings
 * on `PULSE_GENERATED`; the answer is the label stored verbatim on `PULSE_ANSWERED`.
 * Nothing here is composed, summarized or re-rendered, which is what lets a page that
 * shows a year of the app's own sentences exist at all under `MASTER_BUILD_PROMPT.md`
 * 11.4.
 *
 * **A day with no answer shows no answer, and nothing else.** Not a placeholder, not a
 * dash, not the word unanswered, and there is no string in the app that could supply one.
 * 11.6: never chased, never counted against the user, never mentioned. A list view is
 * where that rule is easiest to break by being tidy, so the row simply ends early.
 *
 * **Silent days are not in this list.** They wrote nothing, so there is nothing to show,
 * and a row saying a day was quiet would be the count of silences that design-v3.md 14
 * and 14b.4 both exist to prevent. The rhythm row is where a silent day appears, as a
 * faint mark.
 *
 * **Dates are absolute, including today's.** The obvious answer is `Today`, `Yesterday`
 * and then dates, which is what the Trail does, and design-v3.md 15 asks for the choice
 * to be made rather than inherited. The Trail is read for recency and this is read for
 * pattern: a list whose first two rows are labeled differently from the rest breaks the
 * one column a person is scanning down. So every row is a date, and the newest one is at
 * the top where it always is.
 */
@Composable
internal fun PulseHistoryPage(
    entries: List<PulsePastEntry>,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val contemplative = LocalContemplativeColors.current
    val type = LocalClarityTypography.current

    Column(modifier = modifier.fillMaxSize()) {
        PulseHistoryHeader(onBack = onBack)

        if (entries.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = ClaritySpacing.screenPadding),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = stringResource(R.string.pulse_history_empty_title),
                        style = type.bodySerif,
                        color = contemplative.textBright,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(Modifier.height(ClaritySpacing.scaled(8.dp)))
                    Text(
                        text = stringResource(R.string.pulse_history_empty_body),
                        style = type.body,
                        color = contemplative.textDim,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.widthIn(max = HISTORY_MEASURE),
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentPadding = PaddingValues(
                    start = ClaritySpacing.screenPadding,
                    end = ClaritySpacing.screenPadding,
                    top = 8.dp,
                    bottom = 40.dp,
                ),
                verticalArrangement = Arrangement.spacedBy(ClaritySpacing.sectionGap),
            ) {
                items(items = entries, key = { it.dateKey }) { entry ->
                    PulseHistoryRow(entry)
                }
            }
        }
    }
}

/**
 * The page's own top: a back control and the title.
 *
 * The control is here as well as on the system back, because this page is drawn inside
 * the Pulse sheet rather than pushed onto the shell's back stack, which is a consequence
 * of the surface owning its own navigation. design-v3.md 10.15 requires every screen to
 * have an obvious way out, and a visible one is the way out that does not depend on which
 * window a back dispatcher happens to be attached to.
 */
@Composable
private fun PulseHistoryHeader(onBack: () -> Unit, modifier: Modifier = Modifier) {
    val contemplative = LocalContemplativeColors.current
    val type = LocalClarityTypography.current
    val interaction = remember { MutableInteractionSource() }
    val label = stringResource(R.string.cd_pulse_history_back)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = ClaritySpacing.scaled(4.dp)),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(ClaritySpacing.minTouchTarget)
                .clip(CircleShape)
                .clarityFocusRing(interaction, CircleShape)
                .clarityClickable(
                    interactionSource = interaction,
                    role = Role.Button,
                    onClickLabel = label,
                    onClick = onBack,
                ),
            contentAlignment = Alignment.Center,
        ) {
            ClarityIcon(
                icon = ClarityIcons.back,
                contentDescription = label,
                tint = contemplative.textBright,
                modifier = Modifier
                    .clarityPressScale(interaction, label = "pulseHistoryBack")
                    .size(BACK_GLYPH),
            )
        }
        Spacer(Modifier.size(4.dp))
        Text(
            text = stringResource(R.string.pulse_history_title),
            style = type.displayTitle,
            color = contemplative.textBright,
        )
    }
}

/**
 * One past Pulse.
 *
 * The date, then the observation in the serif the surface reads in, then the question,
 * then the answer in the amber the rhythm row uses for an answered day. Whitespace is the
 * only separation device, 6.1, which is also the reason the rows are 28dp apart rather
 * than ruled.
 */
@Composable
private fun PulseHistoryRow(entry: PulsePastEntry, modifier: Modifier = Modifier) {
    val contemplative = LocalContemplativeColors.current
    val type = LocalClarityTypography.current
    val accent = PulsePalette.accent.calmed(LocalCalmMode.current)

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = entry.date?.let { DATE_FORMAT.format(it) } ?: entry.dateKey,
            style = type.caption,
            // The date is what tells one past Pulse from another, and whitespace is
            // already this row's separation device, 6.1. design-v3.md 13: 32 percent
            // measures 2.637 to one on `deepBlack` against a floor of 4.5.
            color = contemplative.textDim,
        )
        Spacer(Modifier.height(ClaritySpacing.scaled(6.dp)))
        Text(
            text = entry.observation,
            style = type.bodySerif,
            color = contemplative.textBright,
        )
        entry.question?.let { question ->
            Spacer(Modifier.height(ClaritySpacing.scaled(4.dp)))
            Text(text = question, style = type.body, color = contemplative.textDim)
        }
        entry.answerLabel?.let { label ->
            Spacer(Modifier.height(ClaritySpacing.scaled(10.dp)))
            Text(text = label, style = type.bodyStrong, color = accent)
        }
    }
}

/**
 * `Monday, August 24`, matching the Trail's day headers so two lists of the same days
 * read the same way. `Locale.US` for the same reason the Trail pins it: this app ships
 * one language and a formatter that quietly switched would produce a string nobody had
 * checked against the language hygiene gate.
 */
private val DATE_FORMAT: DateTimeFormatter =
    DateTimeFormatter.ofPattern("EEEE, MMMM d", Locale.US)

private val HISTORY_MEASURE = 320.dp
private val BACK_GLYPH = 22.dp
