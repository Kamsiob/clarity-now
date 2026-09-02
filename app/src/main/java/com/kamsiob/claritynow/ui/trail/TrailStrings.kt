package com.kamsiob.claritynow.ui.trail

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import com.kamsiob.claritynow.R
import com.kamsiob.claritynow.domain.parseDateKey
import com.kamsiob.claritynow.domain.query.TrailRow
import com.kamsiob.claritynow.domain.query.TrailSentenceKey
import java.time.format.DateTimeFormatter
import java.util.Locale

/** The Report row renders its week key as a date. See [weekOf]. */
private val WEEK_START_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("MMMM d", Locale.US)

/**
 * The one place a Trail row becomes English. CLAUDE.md rule 8.
 *
 * Every sentence here is a record of what happened rather than an observation about
 * it, which is the distinction rule 8 draws and the reason these live in
 * `strings.xml` instead of a corpus. "Completed Kitchen shelf" names a verb and a
 * snapshot and carries no interpretation; "you finished more than you started this
 * week" compares, and anything that compares has to come through the engine layers
 * from a corpus file. MASTER_BUILD_PROMPT 11.2 closes the list of things that may
 * read a corpus and the Trail is not on it, so a second path would exist if a row
 * ever read one.
 *
 * The constraint that keeps the two apart is on the wording, and it is real: a
 * format string is a verb plus a snapshot, with no adverb, no frequency word, no
 * comparison and no count that spans rows. No "finally", no "again", no "still".
 * Adding one would silently move a sentence from the interface into the engine's
 * territory without moving the code.
 *
 * The `when` below is exhaustive over [TrailSentenceKey] on purpose. A twenty
 * sixth row shape breaks the build here rather than rendering as a blank line on a
 * screen nobody rereads, which is the same guarantee `TrailRow`'s own two `when`
 * expressions give one layer down.
 *
 * A null snapshot substitutes as nothing and the result is trimmed, so a row whose
 * title cannot be resolved reads "Queued" rather than inventing a name for it. That
 * only happens on an imported or merged log missing the ITEM_ADDED that named the
 * item, and CLARITY_LOGIC_ENGINE.md 1 is unambiguous about which of the two costs
 * more: "One fabricated area name or off-by-one number permanently destroys the
 * credibility of everything else the app says."
 */
@Composable
fun trailSentence(row: TrailRow): String {
    val subject = row.subject.orEmpty()
    val minutes = row.minutes ?: 0
    val sentence = when (row.sentence) {
        TrailSentenceKey.AREA_CREATED -> stringResource(R.string.trail_area_created, subject)
        TrailSentenceKey.AREA_RENAMED ->
            stringResource(R.string.trail_area_renamed, subject, row.secondary.orEmpty())
        TrailSentenceKey.AREA_RECOLORED -> stringResource(R.string.trail_area_recolored, subject)
        TrailSentenceKey.AREA_REORDERED -> stringResource(R.string.trail_area_reordered, subject)
        TrailSentenceKey.AREA_ARCHIVED -> stringResource(R.string.trail_area_archived, subject)
        TrailSentenceKey.AREA_UNARCHIVED -> stringResource(R.string.trail_area_unarchived, subject)
        TrailSentenceKey.AREA_DELETED -> stringResource(R.string.trail_area_deleted, subject)
        TrailSentenceKey.ITEM_FILED ->
            stringResource(R.string.trail_item_filed, subject, row.secondary.orEmpty())
        TrailSentenceKey.ITEM_ESTIMATED ->
            stringResource(R.string.trail_item_estimated, subject)
        TrailSentenceKey.ITEM_ESTIMATE_CLEARED ->
            stringResource(R.string.trail_item_estimate_cleared, subject)
        TrailSentenceKey.FOCUS_EXTENDED ->
            pluralStringResource(R.plurals.trail_focus_extended, minutes, minutes, subject)

        TrailSentenceKey.ITEM_ADDED -> stringResource(R.string.trail_item_added, subject)
        TrailSentenceKey.ITEM_EDITED -> stringResource(R.string.trail_item_edited, subject)
        TrailSentenceKey.ITEM_QUEUED -> stringResource(R.string.trail_item_queued, subject)
        TrailSentenceKey.ITEM_PROMOTED -> stringResource(R.string.trail_item_promoted, subject)
        TrailSentenceKey.ITEM_SWAPPED -> stringResource(R.string.trail_item_swapped, subject)
        TrailSentenceKey.ITEM_COMPLETED -> stringResource(R.string.trail_item_completed, subject)
        TrailSentenceKey.ITEM_REOPENED -> stringResource(R.string.trail_item_reopened, subject)
        TrailSentenceKey.ITEM_REORDERED -> stringResource(R.string.trail_item_reordered, subject)
        TrailSentenceKey.ITEM_DELETED -> stringResource(R.string.trail_item_deleted, subject)

        TrailSentenceKey.FOCUS_STARTED -> stringResource(R.string.trail_focus_started, subject)
        // The minute count is the payload's own rounding, never a recomputation.
        TrailSentenceKey.FOCUS_COMPLETED ->
            pluralStringResource(R.plurals.trail_focus_completed, minutes, minutes, subject)
        // Neutral by design. MASTER_BUILD_PROMPT 10 treats abandonment neutrally
        // everywhere, so this is never "ended early" and never "abandoned".
        TrailSentenceKey.FOCUS_STOPPED ->
            // Zero is its own sentence. `minutesOf` rounds to nearest, so a twenty second
            // mis-tap rounds to zero, and "0 minutes of focus" files a discarded session as
            // an achievement of zero on a screen a person rereads.
            if (minutes == 0) {
                stringResource(R.string.trail_focus_stopped_zero, subject)
            } else {
                pluralStringResource(R.plurals.trail_focus_stopped, minutes, minutes, subject)
            }

        TrailSentenceKey.PULSE_GENERATED -> stringResource(R.string.trail_pulse_generated)
        TrailSentenceKey.PULSE_ANSWERED -> stringResource(R.string.trail_pulse_answered, subject)

        TrailSentenceKey.REPORT_GENERATED ->
            stringResource(R.string.trail_report_generated, weekOf(row.subject))

        // These two never appear as a pair and the absence of an acceptance is never
        // rendered as anything. CLAUDE.md rule 13.
        TrailSentenceKey.PLAN_OFFERED -> stringResource(R.string.trail_plan_offered)
        TrailSentenceKey.PLAN_ACCEPTED -> stringResource(R.string.trail_plan_accepted)

        TrailSentenceKey.SETTING_CHANGED -> stringResource(R.string.trail_setting_changed)
    }
    return sentence.trim()
}

/**
 * The row's whole description for a screen reader, and the time is not optional.
 *
 * The visible timestamp is suppressed on every row of a ten minute cluster after
 * the first, which MASTER_BUILD_PROMPT 9 requires and which costs a sighted reader
 * nothing because the time is one row up. A person moving row by row with TalkBack
 * has no row above: the day header is a separate node they may have passed several
 * rows ago. So [time] is spoken on every row whether or not it is drawn, and that is
 * the whole reason this function exists rather than the description being assembled
 * where the timestamp is decided.
 */
@Composable
fun trailRowDescription(sentence: String, time: String, areaName: String? = null): String {
    val base = stringResource(R.string.cd_trail_entry_at, sentence, time)
    // **The dot is the only thing that says which area an event belongs to, and a dot
    // says nothing out loud.** design-v3 13 refuses color as a sole signal; here it was
    // the sole signal twice over, because a screen reader got no area at all and a
    // person with any color vision difference got two indistinguishable dots. The name
    // goes last so the sentence and the time still arrive first.
    return if (areaName.isNullOrBlank()) base else "$base, $areaName"
}

/**
 * The day header's description: its label and the number of entries under it.
 *
 * The count is a direct readout of a queried number, which is the other half of what
 * CLAUDE.md rule 8 puts in `strings.xml`, and the plural form is the resource file's
 * job rather than a branch here.
 */
@Composable
fun trailDayDescription(label: String, count: Int): String =
    label + ", " + pluralStringResource(R.plurals.trail_day_event_count, count, count)

/**
 * A week key rendered as a date.
 *
 * `ReportGenerated.weekStartKey` is passed through the pure mapper raw, because
 * turning `2026-08-23` into `August 23` is a locale and format decision that belongs
 * to the screen and not to the log. An unparseable key prints as itself rather than
 * as a guess.
 */
private fun weekOf(weekStartKey: String?): String {
    if (weekStartKey.isNullOrBlank()) return ""
    return runCatching { WEEK_START_FORMAT.format(parseDateKey(weekStartKey)) }
        .getOrDefault(weekStartKey)
}

/**
 * A Trail row split into what happened and what it happened to.
 *
 * [subject] is null when the row names nothing of the person's own, which is true of
 * the five rows that record something the app did.
 */
data class TrailRowText(val action: String, val subject: String?)

/**
 * Trailing function words. After the snapshot is lifted out of a template, whatever
 * preposition introduced it is left dangling on the end of the action.
 */
private val DANGLING = listOf(" on", " in", " to", " of", " with", " at", " for")

/**
 * **The row is one sentence and it is set as two, and that is the whole fix for the
 * Trail.**
 *
 * Every row shipped as a single string at `body` 15/400. Nine of them on a screen is
 * nine lines of identical type, and A.3 measured the screen's dominance ratio at
 * **1.13 to 1**, the worst in the app: the day header was 17sp against a 15sp body, so
 * there was almost literally nothing to catch the eye. The owner's word for it was that
 * the page is hard to tell apart, which is the same measurement from the other side.
 *
 * Nothing about the data changes. `TrailRow` has carried `subject` in its own field
 * since phase 1, separate from the sentence key, precisely because the snapshot is not
 * part of the template; this only stops throwing that structure away at the last step.
 * The split is mechanical and has no table of special cases:
 *
 * > **Cut the rendered sentence at the first occurrence of the snapshot. What is before
 * > it, plus what is after it, is the action. The snapshot is the subject.**
 *
 * `Made X active` becomes `Made active` over `X`. `Started a focus session on X` becomes
 * `Started a focus session` over `X`, once the dangling preposition is trimmed. A row
 * that names two things keeps them together, because `Renamed` over `Studio to Work` is
 * the sentence and `Renamed to Work` over `Studio` is not.
 *
 * A row whose snapshot could not be resolved, or whose template names nothing, falls
 * back to one line. It never guesses, which is the same refusal [trailSentence]
 * documents one function up.
 */
@Composable
fun trailRowText(row: TrailRow): TrailRowText {
    val sentence = trailSentence(row)
    val subject = row.subject?.takeIf { it.isNotBlank() } ?: return TrailRowText(sentence, null)
    // The Report row renders its snapshot as a date, so the raw key is not in the string.
    val at = sentence.indexOf(subject).takeIf { it > 0 } ?: return TrailRowText(sentence, null)

    val before = sentence.take(at).trim()
    return if (row.secondary.isNullOrBlank()) {
        val after = sentence.substring(at + subject.length).trim()
        val action = listOf(before, after).filter { it.isNotEmpty() }.joinToString(" ")
        TrailRowText(action.trimDangling(), subject)
    } else {
        TrailRowText(before.trimDangling(), sentence.substring(at).trim())
    }
}

private fun String.trimDangling(): String {
    val trimmed = DANGLING.firstOrNull { endsWith(it) }?.let { dropLast(it.length) } ?: this
    return trimmed.trim()
}
