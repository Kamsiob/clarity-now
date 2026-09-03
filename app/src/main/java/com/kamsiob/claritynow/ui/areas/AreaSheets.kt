package com.kamsiob.claritynow.ui.areas

import androidx.compose.foundation.layout.width
import androidx.compose.foundation.interaction.MutableInteractionSource
import com.kamsiob.claritynow.ui.components.clarityFocusRing
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.selected
import androidx.compose.foundation.horizontalScroll
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.kamsiob.claritynow.R
import com.kamsiob.claritynow.data.repo.ClarityRepository
import com.kamsiob.claritynow.domain.replay.ItemState
import com.kamsiob.claritynow.ui.components.ClarityButton
import com.kamsiob.claritynow.ui.components.ClarityButtonRole
import com.kamsiob.claritynow.ui.components.ClarityIcon
import com.kamsiob.claritynow.ui.components.ClarityIcons
import com.kamsiob.claritynow.ui.components.ClaritySheet
import com.kamsiob.claritynow.ui.settings.confirmationMatches
import com.kamsiob.claritynow.ui.components.LocalSheetClose
import com.kamsiob.claritynow.ui.components.ClarityTextField
import com.kamsiob.claritynow.ui.components.Sidehead
import com.kamsiob.claritynow.ui.components.clarityClickable
import com.kamsiob.claritynow.ui.theme.ClarityHapticEvent
import com.kamsiob.claritynow.ui.theme.ClaritySpacing
import androidx.compose.ui.semantics.Role
import com.kamsiob.claritynow.ui.theme.LocalClarityShapes
import com.kamsiob.claritynow.ui.theme.LocalClarityColors
import com.kamsiob.claritynow.ui.theme.LocalClarityTypography
import com.kamsiob.claritynow.ui.theme.clarityMotion
import com.kamsiob.claritynow.ui.theme.parseAreaColor

private val SheetPadding = 20.dp

// Add an item ----------------------------------------------------------------

/**
 * Capture. MASTER_BUILD_PROMPT 8.2 and 14b.1, design-v3.md 10.17.
 *
 * **Capture must never require a decision, and it must never require a scroll either.**
 *
 * ## What was wrong with this sheet, measured
 *
 * The taps were already right: two, the plus and the keyboard's Done. What was wrong was
 * the height. Four stacked fields, a destination line and a button came to about 520dp,
 * and the software keyboard takes roughly half a phone. **So while a person was typing,
 * the line saying where the thought was about to go and the button that puts it there
 * were both below the fold.** At 200 percent text it was around 800dp and scrolled badly.
 * Somebody who types and taps Done never saw either. That is why five of six people in
 * testing lost track of their first capture: not because the app failed to say where it
 * went, but because it said so underneath the keyboard.
 *
 * The three optional fields are the reason it was that tall, and they are the three
 * things Addendum 01 4b is blunt that nothing may prompt for. **Order is the quietest
 * form of prompting there is**, and four fields in a column is a form. They are behind
 * one disclosure now. Nothing is removed and nothing is renamed; what changes is that a
 * person who wants to write one line and leave never passes a field they did not ask for.
 *
 * ## The destination is a choice on the sheet rather than a fact about how it was opened
 *
 * It used to be decided entirely by the route in: from an area's own sheet it went to
 * that area, from the plus with one area to that area, and from the plus with several to
 * the inbox, with no way to say otherwise without backing out and starting again in a
 * different place. **Now every destination is on the sheet**, one tap each, with the
 * route's own answer preselected.
 *
 * The inbox is still what an untouched sheet does, which is Addendum 01 4a and is the
 * whole point of an unfiled capture: no decision at the moment of writing. The change is
 * that not deciding is now visibly a choice among others rather than the only thing on
 * offer.
 *
 * **It does not remember the last one used.** `MASTER_BUILD_PROMPT.md` 13.5 refuses
 * adaptive ordering in the same words for the app shortcuts: "a shortcut list that
 * reordered itself around what the user did most would be a measurement of the user".
 * A destination row that rearranged itself would be the same measurement on the surface a
 * person touches most, and none of the apps this was checked against does it either.
 *
 * The sheet still states where the item will land before the person commits, in all
 * three cases, because an item that silently becomes active is a surprise, an item that
 * silently joins a queue is a different surprise, and an item that silently goes
 * somewhere the person has not seen yet is the worst of the three. It is above the
 * button now instead of under three fields.
 */
@Composable
fun AddItemSheet(
    areas: List<AreaCardModel>,
    initialAreaId: String?,
    landsActive: (String?) -> Boolean,
    onAdd: (String?, String, String?, String?, Int?) -> Unit,
    onNewArea: () -> Unit,
    onDismiss: () -> Unit,
) {
    val colors = LocalClarityColors.current
    val type = LocalClarityTypography.current
    // **`rememberSaveable`, because a half typed capture is the most expensive thing in
    // this app to lose.** A plain `remember` is discarded when the process is killed
    // behind a keyboard, which on a low memory phone is the ordinary case rather than the
    // exceptional one, and the whole point of a capture is that it costs nothing. The two
    // password fields and the three typed destructive confirmations deliberately stay on
    // plain `remember`: a secret and an "I meant it" are the two things that should not
    // survive the app going away.
    var title by rememberSaveable { mutableStateOf("") }
    var note by rememberSaveable { mutableStateOf("") }
    var firstStep by rememberSaveable { mutableStateOf("") }
    var estimate by rememberSaveable { mutableStateOf("") }
    var destination by rememberSaveable { mutableStateOf(initialAreaId) }
    var moreOpen by rememberSaveable { mutableStateOf(false) }
    val focus = remember { FocusRequester() }
    val keyboard = LocalSoftwareKeyboardController.current

    LaunchedEffect(Unit) { focus.requestFocus() }

    // An area deleted under an open sheet takes its selection with it rather than
    // committing the item into an id that no longer resolves.
    LaunchedEffect(areas) {
        if (destination != null && areas.none { it.id == destination }) destination = null
    }

    val commit = {
        keyboard?.hide()
        onAdd(
            destination,
            title,
            note.ifBlank { null },
            firstStep.ifBlank { null },
            estimate.toMinutes(),
        )
    }

    ClaritySheet(onDismiss = onDismiss, title = stringResource(R.string.sheet_add_item_title)) {
        Column(
            modifier = Modifier
                .heightIn(max = 620.dp)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = SheetPadding),
        ) {
            // **Done, not Next, and it commits.** The capture sheet is the one screen in
            // this app that promises to require no decisions, and the keyboard's action
            // key walked into the note field, which is multi line and whose own action key
            // is therefore a newline. There was no way to finish a capture from the
            // keyboard at all: every one line thought ended with a thumb moving past
            // three more fields to a button. The edit sheet keeps Next, because there the
            // decision has already been made and moving between fields is the job.
            //
            // **Capped on input, not on save.** `ClarityRepository.addItem` refuses a
            // title over `MAX_ITEM_TITLE` by returning null, so a long capture was
            // silently destroyed at the moment a person tapped Add: no message, no
            // recovery, and the sheet closed as though it had worked.
            ClarityTextField(
                value = title,
                onValueChange = { title = it.take(ClarityRepository.MAX_ITEM_TITLE) },
                label = stringResource(R.string.field_title),
                focusRequester = focus,
                imeAction = ImeAction.Done,
                onImeAction = { if (title.isNotBlank()) commit() },
            )

            Spacer(Modifier.height(ClaritySpacing.scaled(18.dp)))
            DestinationRow(
                areas = areas,
                selected = destination,
                onSelect = { destination = it },
                onNewArea = onNewArea,
            )

            Spacer(Modifier.height(ClaritySpacing.scaled(14.dp)))
            Text(
                text = stringResource(
                    when {
                        destination == null -> R.string.add_item_lands_inbox
                        landsActive(destination) -> R.string.add_item_lands_active
                        else -> R.string.add_item_lands_queue
                    },
                ),
                style = type.caption,
                // design-v3.md 3.1: `inkTertiary` carries no text anywhere in this
                // app, and this line tells a person where the thing they are typing
                // will end up. The caption size is what makes it quieter than the
                // fields above it; the color is not asked to do that job.
                color = colors.inkSecondary,
            )

            Spacer(Modifier.height(ClaritySpacing.scaled(16.dp)))
            ClarityButton(
                label = stringResource(R.string.action_add),
                enabled = title.isNotBlank(),
                onClick = commit,
            )

            // The three optional fields, behind one disclosure. Closed, this sheet is
            // about 250dp and sits entirely above the keyboard on a Pixel 8.
            Spacer(Modifier.height(ClaritySpacing.scaled(12.dp)))
            MoreFieldsDisclosure(open = moreOpen, onToggle = { moreOpen = !moreOpen })
            AnimatedVisibility(visible = moreOpen) {
                Column {
                    Spacer(Modifier.height(ClaritySpacing.scaled(16.dp)))
                    ClarityTextField(
                        value = note,
                        onValueChange = { note = it },
                        label = stringResource(R.string.field_note_optional),
                        singleLine = false,
                    )
                    Spacer(Modifier.height(ClaritySpacing.scaled(20.dp)))
                    FirstStepField(value = firstStep, onValueChange = { firstStep = it })
                    Spacer(Modifier.height(ClaritySpacing.scaled(20.dp)))
                    EstimateField(value = estimate, onValueChange = { estimate = it })
                }
            }
            Spacer(Modifier.height(ClaritySpacing.scaled(10.dp)))
        }
    }
}

/**
 * Where the item goes. One row, one tap, and the inbox is one of the choices rather than
 * the absence of one.
 *
 * The inbox leads because it is what an untouched sheet does, and a person scanning left
 * to right should meet the default first. Each area carries its own 7dp dot, which is
 * `design-v3.md` 3.4's first permitted form and the same mark the card and the tab bar
 * use, so an area is recognized here by the thing that identifies it everywhere else.
 *
 * `A new area` is last, because it is the rarest and because a chooser that opened with
 * a way to make a new option would be asking a question before offering the answers.
 */
@Composable
private fun DestinationRow(
    areas: List<AreaCardModel>,
    selected: String?,
    onSelect: (String?) -> Unit,
    onNewArea: () -> Unit,
) {
    val type = LocalClarityTypography.current
    Column {
        Text(
            text = stringResource(R.string.field_destination),
            style = type.sidehead,
            color = LocalClarityColors.current.inkSecondary,
        )
        Spacer(Modifier.height(ClaritySpacing.scaled(8.dp)))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(ClaritySpacing.tight),
        ) {
            DestinationChip(
                label = stringResource(R.string.destination_inbox),
                dot = null,
                selected = selected == null,
                onClick = { onSelect(null) },
            )
            areas.forEach { area ->
                DestinationChip(
                    label = area.name,
                    dot = parseAreaColor(area.colorHex),
                    selected = selected == area.id,
                    onClick = { onSelect(area.id) },
                )
            }
            DestinationChip(
                label = stringResource(R.string.inbox_file_new_area),
                dot = null,
                selected = false,
                onClick = onNewArea,
            )
        }
    }
}

/** One destination. Selected is a filled ground and a weight, never color alone. */
@Composable
private fun DestinationChip(
    label: String,
    dot: Color?,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val colors = LocalClarityColors.current
    val type = LocalClarityTypography.current
    val interaction = remember { MutableInteractionSource() }
    val shape = LocalClarityShapes.current.pill

    Row(
        modifier = Modifier
            .heightIn(min = ClaritySpacing.minTouchTarget)
            .clip(shape)
            .background(if (selected) colors.actionBlue.copy(alpha = 0.11f) else colors.raise)
            .clarityFocusRing(interaction, shape)
            .clarityClickable(
                interactionSource = interaction,
                haptic = ClarityHapticEvent.TAP,
                role = Role.Button,
                pressShape = shape,
                onClick = onClick,
            )
            .semantics { this.selected = selected }
            .padding(horizontal = ClaritySpacing.snug + 2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (dot != null) {
            Box(
                modifier = Modifier
                    .size(ClaritySpacing.areaDot)
                    .clip(CircleShape)
                    .background(dot),
            )
            Spacer(Modifier.width(ClaritySpacing.tight))
        }
        Text(
            text = label,
            style = type.label,
            color = if (selected) colors.actionBlue else colors.inkSecondary,
            maxLines = 1,
        )
    }
}

/**
 * `More` and nothing else, because the three fields behind it have nothing in common
 * except being optional and a heading that named them would be the prompt 4b forbids.
 */
@Composable
private fun MoreFieldsDisclosure(open: Boolean, onToggle: () -> Unit) {
    val colors = LocalClarityColors.current
    val type = LocalClarityTypography.current
    val motion = clarityMotion()
    val interaction = remember { MutableInteractionSource() }
    val turn by animateFloatAsState(
        targetValue = if (open) 180f else 0f,
        animationSpec = motion.springStandard(),
        label = "moreChevron",
    )

    Row(
        modifier = Modifier
            .heightIn(min = ClaritySpacing.minTouchTarget)
            .clip(LocalClarityShapes.current.row)
            .clarityFocusRing(interaction, LocalClarityShapes.current.row)
            .clarityClickable(
                interactionSource = interaction,
                haptic = ClarityHapticEvent.TAP,
                role = Role.Button,
                onClick = onToggle,
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(R.string.add_item_more),
            style = type.label,
            color = colors.inkSecondary,
        )
        Spacer(Modifier.width(ClaritySpacing.hair))
        ClarityIcon(
            icon = ClarityIcons.expand,
            contentDescription = null,
            tint = colors.inkSecondary,
            modifier = Modifier.size(18.dp).rotate(turn),
        )
    }
}

/**
 * design-v3.md 10.17 and Addendum 01 4b. One optional line: the first physical
 * action.
 *
 * The label is the one design-v3.md names verbatim and the placeholder is an example
 * rather than an instruction. That distinction is the whole design of this field.
 * `Break this down into steps` is an instruction, and an instruction to break a task
 * down is a second task handed to the person least able to take one on. An example
 * of what somebody else wrote is a demonstration, and it costs nothing to ignore.
 *
 * It is deterministic task breakdown: the user writes the small action, the app
 * stores it and shows it at the moment it is needed. Addendum 01 9b rules out AI
 * breakdown permanently, on both the no-AI and the no-network commitments, and this
 * is the version of the same idea that needs neither.
 *
 * One composable shared by the add and the edit sheets, so the label, the example
 * and the keyboard behavior cannot drift between the two places it appears.
 */
@Composable
private fun FirstStepField(value: String, onValueChange: (String) -> Unit) {
    ClarityTextField(
        value = value,
        onValueChange = onValueChange,
        label = stringResource(R.string.field_first_step),
        placeholder = stringResource(R.string.field_first_step_example),
        imeAction = ImeAction.Next,
    )
}

/**
 * design-v3.md 10.17 and Addendum 01 4c. Optional minutes, entered as a number.
 *
 * **A free number field rather than a set of durations**, which design-v3.md 10.17
 * settles and `DECISIONS.md` records. The chip set is the statistically common answer
 * and it loses twice: it is a decision with five options placed in the capture path,
 * and the buckets it imposes would become the shape of the calibration facts phase 8
 * reads, per MASTER_BUILD_PROMPT 14b.8.
 *
 * Digits only, four of them at most. The filter is on input rather than on save
 * because a field that accepts what it will silently discard is a field that lies.
 * Four digits is just under seven days, which is past the point where an estimate in
 * minutes is the right instrument at all.
 *
 * **Nothing anywhere counts down against this number.** Not this sheet, not the card,
 * not the area card, not a widget, not a notification. An estimate that becomes a
 * deadline is a worse instrument than no estimate, and it is a deadline the person
 * set for themselves in a hopeful moment and then has to watch expire.
 */
@Composable
private fun EstimateField(value: String, onValueChange: (String) -> Unit) {
    ClarityTextField(
        value = value,
        onValueChange = { input -> if (input.length <= 4 && input.all(Char::isDigit)) onValueChange(input) },
        label = stringResource(R.string.field_estimate_minutes),
        keyboardType = KeyboardType.Number,
    )
}

/**
 * The typed field as an estimate, or null.
 *
 * Blank and zero reach the same answer, which is the answer the repository reaches
 * too: zero minutes is not an estimate. Clearing the field on an edit therefore
 * writes `ITEM_ESTIMATED` with a null new value rather than writing nothing, per
 * Addendum 01 2b.
 */
private fun String.toMinutes(): Int? = trim().toIntOrNull()?.takeIf { it > 0 }

// Edit a queued or active item ------------------------------------------------

/**
 * design-v3.md 10.15. A queued item is tappable and this is the only way to edit
 * one. Without it the queue is read only, which nobody expects.
 *
 * It is also the only way to edit an unfiled item, reached from a row in the inbox
 * sheet. Addendum 01 4a allows an unfiled item to be edited and deleted, and this
 * sheet is where both happen. `Move to front` is absent for one, because an item in
 * no area has no queue to be at the front of, and the caller decides that rather than
 * this sheet guessing from a null.
 *
 * **Saving can write two events.** The title, the note and the first step move
 * together on `ITEM_EDITED`. The estimate moves on its own `ITEM_ESTIMATED`, which is
 * what lets a guess be revised without rewriting what the person first wrote down.
 * Neither is written when its half did not change.
 */
@Composable
fun EditItemSheet(
    item: ItemState,
    canMoveToFront: Boolean,
    onSave: (String, String?, String?, Int?) -> Unit,
    onDelete: () -> Unit,
    onMoveToFront: () -> Unit,
    onDismiss: () -> Unit,
) {
    var title by remember(item.id) { mutableStateOf(item.title) }
    var note by remember(item.id) { mutableStateOf(item.note.orEmpty()) }
    var firstStep by remember(item.id) { mutableStateOf(item.firstStep.orEmpty()) }
    var estimate by remember(item.id) { mutableStateOf(item.estimateMinutes?.toString().orEmpty()) }
    val keyboard = LocalSoftwareKeyboardController.current

    ClaritySheet(onDismiss = onDismiss, title = stringResource(R.string.sheet_edit_item_title)) {
        Column(
            modifier = Modifier
                .heightIn(max = 620.dp)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = SheetPadding),
        ) {
            ClarityTextField(
                value = title,
                onValueChange = { title = it.take(ClarityRepository.MAX_ITEM_TITLE) },
                label = stringResource(R.string.field_title),
                imeAction = ImeAction.Next,
            )
            Spacer(Modifier.height(ClaritySpacing.scaled(20.dp)))
            ClarityTextField(
                value = note,
                onValueChange = { note = it },
                label = stringResource(R.string.field_note_optional),
                singleLine = false,
            )
            Spacer(Modifier.height(ClaritySpacing.scaled(20.dp)))
            // Emptying either field is an ordinary edit and deletes the value. There
            // is no separate clear control, because a field a person can empty is a
            // field they already know how to clear.
            FirstStepField(value = firstStep, onValueChange = { firstStep = it })
            Spacer(Modifier.height(ClaritySpacing.scaled(20.dp)))
            EstimateField(value = estimate, onValueChange = { estimate = it })
            Spacer(Modifier.height(ClaritySpacing.scaled(24.dp)))

            if (canMoveToFront) {
                SheetActionRow(
                    icon = ClarityIcons.moveToFront,
                    label = stringResource(R.string.action_move_to_front),
                    onClick = onMoveToFront,
                )
            }
            SheetActionRow(
                icon = ClarityIcons.deleteSwipe,
                label = stringResource(R.string.action_delete),
                tint = LocalClarityColors.current.deleteMuted,
                onClick = onDelete,
            )

            Spacer(Modifier.height(ClaritySpacing.scaled(20.dp)))
            ClarityButton(
                label = stringResource(R.string.action_save),
                enabled = title.isNotBlank(),
                onClick = {
                    keyboard?.hide()
                    onSave(title, note.ifBlank { null }, firstStep.ifBlank { null }, estimate.toMinutes())
                },
            )
            Spacer(Modifier.height(ClaritySpacing.scaled(10.dp)))
        }
    }
}

// The area detail sheet -------------------------------------------------------

/**
 * Everything an area can do, reachable without a swipe. design-v3.md 10.3.1 makes
 * this mandatory rather than a convenience: swipe is invisible to a screen reader
 * and is only ever an accelerator.
 */
@Composable
fun AreaDetailSheet(
    area: AreaCardModel,
    active: ItemState?,
    queue: List<ItemState>,
    completed: List<ItemState>,
    onAddItem: () -> Unit,
    onEditArea: () -> Unit,
    onComplete: () -> Unit,
    onSwap: () -> Unit,
    onFocus: () -> Unit,
    onArchive: () -> Unit,
    onDelete: () -> Unit,
    onOpenItem: (ItemState) -> Unit,
    onReopenItem: (ItemState) -> Unit,
    onMakeActive: (ItemState) -> Unit,
    onDismiss: () -> Unit,
) {
    val colors = LocalClarityColors.current
    val type = LocalClarityTypography.current
    val motion = clarityMotion()
    val accent = parseAreaColor(area.colorHex)
    var showCompleted by remember { mutableStateOf(false) }

    ClaritySheet(onDismiss = onDismiss) {
        Column(
            modifier = Modifier
                .heightIn(max = 620.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = SheetPadding),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(9.dp).clip(CircleShape).background(accent))
                    Text(
                        text = area.name,
                        style = type.title,
                        color = colors.inkPrimary,
                        modifier = Modifier.padding(start = 10.dp),
                    )
                }
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .clarityClickable(onClickLabel = stringResource(R.string.cd_edit_area)) { onEditArea() },
                    contentAlignment = Alignment.Center,
                ) {
                    ClarityIcon(
                        icon = ClarityIcons.editArea,
                        contentDescription = stringResource(R.string.cd_edit_area),
                        tint = colors.inkSecondary,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }

            Spacer(Modifier.height(ClaritySpacing.scaled(18.dp)))
            Sidehead(
                text = stringResource(R.string.sidehead_active),
                modifier = Modifier.padding(horizontal = SheetPadding),
            )
            Spacer(Modifier.height(ClaritySpacing.scaled(10.dp)))
            if (active == null) {
                // The same string design-v3.md 10.3 resolved on the card, drawn the
                // same way here: weight 500 rather than the role's 650, and a color
                // that clears section 13's floor. The alternative branch below is the
                // real title at the same size, so before this the two states differed
                // by color alone and the idle one measured 2.402 to one.
                Text(
                    text = stringResource(R.string.area_idle_title),
                    style = type.itemTitle.copy(fontWeight = FontWeight(500)),
                    color = colors.inkSecondary,
                    modifier = Modifier.padding(horizontal = SheetPadding),
                )
            } else {
                Text(
                    text = active.title,
                    style = type.itemTitle,
                    color = colors.inkPrimary,
                    modifier = Modifier
                        .padding(horizontal = SheetPadding)
                        .clarityClickable { onOpenItem(active) },
                )
                // design-v3.md 10.17. The first step in full, above the note and
                // below the title: it is the way in, and it is read at the moment the
                // title has already failed to start someone. The card gets one
                // ellipsized line of it, and this is the surface that gets all of it.
                active.firstStep?.let { firstStep ->
                    Text(
                        text = firstStep,
                        style = type.body,
                        color = colors.inkPrimary,
                        modifier = Modifier
                            .padding(horizontal = SheetPadding)
                            .padding(top = ClaritySpacing.scaled(6.dp)),
                    )
                }
                active.note?.let { note ->
                    Text(
                        text = note,
                        style = type.body,
                        color = colors.inkSecondary,
                        modifier = Modifier.padding(
                            horizontal = SheetPadding,
                            vertical = ClaritySpacing.scaled(6.dp,
                        )),
                    )
                }
                // design-v3.md 10.17. The estimate appears here and on no other
                // surface in the app. Plain text, once: **never a countdown against
                // the item, never a bar filling toward it, never a target, and never
                // beside an actual.** No surface may draw the difference between an
                // estimate and an actual, by any means, and a shape can accuse as
                // plainly as a sentence can.
                active.estimateMinutes?.let { minutes ->
                    Text(
                        text = pluralStringResource(R.plurals.item_estimate_minutes, minutes, minutes),
                        style = type.caption,
                        // The third rank in this block is the caption size, not a
                        // third ink. design-v3.md 3.1 gives the ladder two ranks that
                        // carry text and keeps `inkTertiary` for shapes, so the title,
                        // the note and the estimate separate by 5.3's scale.
                        color = colors.inkSecondary,
                        modifier = Modifier
                            .padding(horizontal = SheetPadding)
                            .padding(top = ClaritySpacing.scaled(8.dp)),
                    )
                }
                Spacer(Modifier.height(ClaritySpacing.scaled(14.dp)))
                Row(
                    modifier = Modifier.padding(horizontal = SheetPadding),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    ClarityButton(
                        label = stringResource(R.string.action_complete),
                        role = ClarityButtonRole.POSITIVE,
                        onClick = onComplete,
                        fillWidth = false,
                        modifier = Modifier.weight(1f),
                    )
                    if (area.offersSwap) {
                        ClarityButton(
                            label = stringResource(R.string.action_swap),
                            role = ClarityButtonRole.SECONDARY,
                            onClick = onSwap,
                            fillWidth = false,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
                // **Start here, on the thing being read.** Issue #62: this is the moment
                // a person has decided what to work on, and until now the only way to
                // begin was to close this, cross the home screen, open the chooser and
                // find the same area again. Eight interactions to arrange five minutes.
                //
                // Its own row rather than a third button beside the two above. Three
                // buttons across a sheet clip their labels at 200 percent text, and
                // these are two different kinds of act: Complete and Swap change where
                // this item stands, and this one starts something and leaves the queue
                // exactly as it was.
                Spacer(Modifier.height(ClaritySpacing.scaled(10.dp)))
                ClarityButton(
                    label = stringResource(R.string.action_focus_on_this),
                    role = ClarityButtonRole.SECONDARY,
                    onClick = onFocus,
                    modifier = Modifier.padding(horizontal = SheetPadding),
                )
            }

            Spacer(Modifier.height(ClaritySpacing.scaled(26.dp)))
            Sidehead(
                text = stringResource(R.string.sidehead_queue),
                modifier = Modifier.padding(horizontal = SheetPadding),
            )
            Spacer(Modifier.height(ClaritySpacing.scaled(6.dp)))
            if (queue.isEmpty()) {
                Text(
                    text = stringResource(R.string.queue_empty),
                    style = type.body,
                    // design-v3.md 10.13: an empty state is an invitation. An
                    // invitation at 2.402 to one is not one.
                    color = colors.inkSecondary,
                    modifier = Modifier.padding(
                        horizontal = SheetPadding,
                        vertical = ClaritySpacing.scaled(8.dp,
                    )),
                )
            } else {
                queue.forEach { item ->
                    QueueRow(
                        item = item,
                        onClick = { onOpenItem(item) },
                        onMakeActive = { onMakeActive(item) },
                    )
                }
            }

            Spacer(Modifier.height(ClaritySpacing.scaled(10.dp)))
            SheetActionRow(
                icon = ClarityIcons.add,
                label = stringResource(R.string.area_add_item),
                tint = colors.actionBlue,
                onClick = onAddItem,
            )

            if (completed.isNotEmpty()) {
                Spacer(Modifier.height(ClaritySpacing.scaled(18.dp)))
                val chevronRotation by animateFloatAsState(
                    targetValue = if (showCompleted) 180f else 0f,
                    animationSpec = motion.springStandard(),
                    label = "completedChevron",
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clarityClickable { showCompleted = !showCompleted }
                        .padding(
                            horizontal = SheetPadding,
                            vertical = ClaritySpacing.scaled(10.dp,
                        )),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(R.string.sidehead_completed),
                        style = type.sidehead,
                        color = colors.inkSecondary,
                    )
                    Spacer(Modifier.size(8.dp))
                    Text(
                        text = completed.size.toString(),
                        style = type.caption,
                        // A queried number read straight off the state, so it is read.
                        // The sidehead beside it is a different type role, which is
                        // what separates them now that both clear section 13.
                        color = colors.inkSecondary,
                    )
                    Spacer(Modifier.weight(1f))
                    ClarityIcon(
                        icon = ClarityIcons.expand,
                        contentDescription = null,
                        // The rotation is the disclosure state, so this glyph carries
                        // meaning and takes design-v3.md 13's 3.0 floor for a graphic.
                        // `inkTertiary` misses it in the light world at 2.402.
                        tint = colors.inkSecondary,
                        modifier = Modifier.size(20.dp).rotate(chevronRotation),
                    )
                }
                AnimatedVisibility(
                    visible = showCompleted,
                    enter = fadeIn(motion.easeOut()) + expandVertically(motion.springGentle()),
                    exit = fadeOut(motion.easeOut()) + shrinkVertically(motion.springGentle()),
                ) {
                    Column {
                        completed.forEach { item ->
                            CompletedRow(item = item, onReopen = { onReopenItem(item) })
                        }
                    }
                }
            }

            Spacer(Modifier.height(ClaritySpacing.scaled(22.dp)))
            Sidehead(
                text = stringResource(R.string.sidehead_area_actions),
                modifier = Modifier.padding(horizontal = SheetPadding),
            )
            Spacer(Modifier.height(ClaritySpacing.scaled(4.dp)))
            // **Renaming was behind an unlabeled glyph in a corner and is a labeled row
            // here too.** The glyph stays, because it is where somebody who has already
            // found it will look, but a 44dp pencil at the top right of a scrolling sheet
            // is not how a person discovers that an area can be renamed or recolored. It
            // leads the group because it is the only one of the three that is not
            // destructive.
            SheetActionRow(
                icon = ClarityIcons.editArea,
                label = stringResource(R.string.area_edit),
                onClick = onEditArea,
            )
            SheetActionRow(
                icon = ClarityIcons.archive,
                label = stringResource(R.string.action_archive),
                onClick = onArchive,
            )
            SheetActionRow(
                icon = ClarityIcons.deleteSwipe,
                label = stringResource(R.string.action_delete),
                tint = colors.deleteMuted,
                onClick = onDelete,
            )
            Spacer(Modifier.height(ClaritySpacing.scaled(8.dp)))
        }
    }
}

/**
 * **A queue row now has two targets, and the second one is the whole point.**
 *
 * Changing which item is active was possible before and it was not easy: a full left
 * swipe on the area card opened a chooser, which is a gesture nothing on the screen
 * announces, and tapping a queue row opened the item for editing, which is what a person
 * does far less often than deciding to work on something else instead. The queue is a
 * list of things a person has already said they intend to do, so **the common act on it
 * is "that one, now"**, and it was three deliberate steps behind an undiscoverable
 * gesture.
 *
 * The row keeps its old behavior and gains a 48dp trailing button that promotes the item
 * in one tap. Whatever is active is demoted to the head of the queue, which is exactly
 * what Swap already did, so this adds no new state and no new event type. The chooser
 * sheets pass no handler, because in those the whole row already means "choose this" and
 * a second control saying the same thing would be a second way to do one thing.
 */
@Composable
private fun QueueRow(item: ItemState, onClick: () -> Unit, onMakeActive: (() -> Unit)? = null) {
    val colors = LocalClarityColors.current
    val type = LocalClarityTypography.current
    val shapes = LocalClarityShapes.current
    val makeActiveLabel = stringResource(R.string.action_make_active)
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            modifier = Modifier
                .weight(1f)
                .clarityClickable(onClickLabel = item.title, onClick = onClick)
                .padding(
                    start = SheetPadding,
                    end = ClaritySpacing.tight,
                    top = ClaritySpacing.snug,
                    bottom = ClaritySpacing.snug,
                )
                // 13's floor. The row used to measure 46.5dp, which is a live defect
                // rather than a matter of taste.
                .heightIn(min = ClaritySpacing.minTouchTarget - ClaritySpacing.snug * 2),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                        text = item.title,
                        style = type.body,
                        color = colors.inkPrimary,
                        // A 200 character title in a list row is a row taller than the phone.
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                // The note is a rank below the title and stays one: `caption` against
                // `body`, design-v3.md 5.3. The ink is the same, because 3.1 gives this
                // app two inks that carry text and the third is for shapes.
                item.note?.let {
                    Text(text = it, style = type.caption, color = colors.inkSecondary)
                }
            }
        }
        if (onMakeActive == null) {
            ClarityIcon(
                icon = ClarityIcons.chevron,
                contentDescription = null,
                tint = colors.inkSecondary,
                modifier = Modifier.padding(end = SheetPadding - 3.dp).size(18.dp),
            )
        } else {
            Box(
                modifier = Modifier
                    .padding(end = ClaritySpacing.snug)
                    .size(ClaritySpacing.minTouchTarget)
                    .clip(shapes.pill)
                    .clarityClickable(
                        haptic = ClarityHapticEvent.PROMOTE,
                        role = Role.Button,
                        onClickLabel = makeActiveLabel,
                        onClick = onMakeActive,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                ClarityIcon(
                    icon = ClarityIcons.promoted,
                    contentDescription = makeActiveLabel,
                    tint = colors.actionBlue,
                    modifier = Modifier.size(22.dp),
                )
            }
        }
    }
}

@Composable
private fun CompletedRow(item: ItemState, onReopen: () -> Unit) {
    val colors = LocalClarityColors.current
    val type = LocalClarityTypography.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = SheetPadding, vertical = ClaritySpacing.scaled(10.dp)),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ClarityIcon(
            icon = ClarityIcons.completed,
            contentDescription = null,
            // design-v3.md 3.1 as amended by the phase 13 contrast audit: positiveGreen
            // is the fill and positiveInk is the foreground. The glyph measured 2.20 to
            // one on this sheet's card in the fill color, against a floor of 3.0 for a
            // graphic, and 7.00 in the ink.
            tint = colors.positiveInk,
            modifier = Modifier.size(17.dp),
        )
        Text(
            text = item.title,
            style = type.body.copy(textDecoration = TextDecoration.LineThrough),
            // The strike and the check to its left are what say completed, and
            // design-v3.md 13 requires exactly that: color is never the only signal.
            // So the color has nothing left to say and is free to be readable.
            color = colors.inkSecondary,
            modifier = Modifier.weight(1f).padding(start = 10.dp),
        )
        Text(
            text = stringResource(R.string.action_reopen),
            style = type.caption,
            color = colors.actionBlue,
            modifier = Modifier.clarityClickable(onClick = onReopen),
        )
    }
}

@Composable
private fun SheetActionRow(
    icon: Int,
    label: String,
    onClick: () -> Unit,
    tint: Color = LocalClarityColors.current.inkSecondary,
) {
    val colors = LocalClarityColors.current
    val type = LocalClarityTypography.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clarityClickable(onClickLabel = label, onClick = onClick)
            .padding(horizontal = SheetPadding, vertical = ClaritySpacing.scaled(14.dp)),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ClarityIcon(icon = icon, contentDescription = null, tint = tint, modifier = Modifier.size(20.dp))
        Text(
            text = label,
            style = type.body,
            color = if (tint == colors.inkSecondary) colors.inkPrimary else tint,
            modifier = Modifier.padding(start = 14.dp),
        )
    }
}

// Area editor -----------------------------------------------------------------

@Composable
fun AreaEditorSheet(
    initialName: String,
    initialColorHex: String,
    isNew: Boolean,
    previewItemTitle: String?,
    onSave: (String, String) -> Unit,
    onDismiss: () -> Unit,
) {
    var name by remember { mutableStateOf(initialName) }
    var hex by remember { mutableStateOf(initialColorHex) }
    val keyboard = LocalSoftwareKeyboardController.current
    val nameFocus = remember { FocusRequester() }

    // A new area opens on its name, an existing one does not, because reopening an
    // editor with the keyboard up hides the thing being edited.
    LaunchedEffect(isNew) { if (isNew) nameFocus.requestFocus() }

    ClaritySheet(
        onDismiss = onDismiss,
        title = stringResource(if (isNew) R.string.sheet_new_area_title else R.string.sheet_edit_area_title),
    ) {
        Column(
            modifier = Modifier
                .heightIn(max = 640.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            Column(modifier = Modifier.padding(horizontal = SheetPadding)) {
                ClarityTextField(
                    value = name,
                    onValueChange = { if (it.length <= 40) name = it },
                    label = stringResource(R.string.field_area_name),
                    focusRequester = nameFocus,
                )
            }
            Spacer(Modifier.height(ClaritySpacing.scaled(26.dp)))
            AreaColorPicker(
                areaName = name,
                selectedHex = hex,
                onSelect = { hex = it },
                previewItemTitle = previewItemTitle,
            )
            Spacer(Modifier.height(ClaritySpacing.scaled(20.dp)))
            Column(modifier = Modifier.padding(horizontal = SheetPadding)) {
                ClarityButton(
                    label = stringResource(R.string.action_save),
                    enabled = name.isNotBlank(),
                    onClick = {
                        keyboard?.hide()
                        onSave(name, hex)
                    },
                )
            }
            Spacer(Modifier.height(ClaritySpacing.scaled(12.dp)))
        }
    }
}

/**
 * The long press menu on an area card. `design-v3.md` 10.3.1, and it was never built.
 *
 * **The specification calls it mandatory and the code redirected it to the detail
 * sheet.** 10.3.1: "All three actions must also be reachable from a long press context
 * menu on the card **and** from the area detail sheet. Swipe is invisible to TalkBack and
 * is an accelerator, never the only path." `AreaSheet.LongPressMenu` existed as a type
 * and its one handler read `sheet = AreaSheet.Detail(current.areaId)`.
 *
 * ## It carries the area operations too, and that is the point rather than a bonus
 *
 * The owner's report was that he designed this app and could not work out how to manage
 * an area. Editing lived behind an unlabeled 44dp pencil in the corner of a sheet;
 * archiving and deleting lived under a sidehead reading `Area`, below the active item,
 * the whole queue, an add row and the completed list, which on a full area is off the
 * bottom of a 620dp scrolling sheet. Every one of them was reachable and none of them was
 * findable.
 *
 * This is one labeled list of every verb an area has, and a long press is a second path
 * to it rather than the only one.
 *
 * ## `Move to top` is here because dragging is not an accessible way to reorder
 *
 * WCAG 2.2 SC 2.5.7 requires a single pointer alternative to any drag operation.
 * Reordering was a long press and drag, with a TalkBack custom action as the only
 * alternative, which serves a screen reader user and nobody else: a person with a tremor,
 * a person using a stylus, and the person in testing who never discovered the gesture all
 * had no path at all. One tap does it now.
 */
@Composable
fun AreaMenuSheet(
    area: AreaCardModel,
    onAddItem: () -> Unit,
    onComplete: () -> Unit,
    onSwap: () -> Unit,
    onMoveToTop: (() -> Unit)?,
    onEdit: () -> Unit,
    onArchive: () -> Unit,
    onDelete: () -> Unit,
    onDismiss: () -> Unit,
) {
    val colors = LocalClarityColors.current
    val type = LocalClarityTypography.current
    val accent = parseAreaColor(area.colorHex)
    val close = LocalSheetClose.current

    ClaritySheet(onDismiss = onDismiss) {
        Column(modifier = Modifier.padding(bottom = ClaritySpacing.scaled(10.dp))) {
            Row(
                modifier = Modifier.padding(
                    horizontal = SheetPadding,
                    vertical = ClaritySpacing.scaled(6.dp),
                ),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(ClaritySpacing.areaDot)
                        .clip(CircleShape)
                        .background(accent),
                )
                Spacer(Modifier.width(ClaritySpacing.snug))
                Text(text = area.name, style = type.itemTitle, color = colors.inkPrimary)
            }
            Spacer(Modifier.height(ClaritySpacing.scaled(10.dp)))

            SheetActionRow(
                icon = ClarityIcons.add,
                label = stringResource(R.string.area_add_item),
                onClick = { close(); onAddItem() },
            )
            // The two item verbs, on the same conditions the swipe faces use, so a
            // control that does nothing is never offered. `AreasViewModel` holds both.
            if (area.offersComplete) {
                SheetActionRow(
                    icon = ClarityIcons.completed,
                    label = stringResource(R.string.action_complete),
                    onClick = { close(); onComplete() },
                )
            }
            if (area.offersSwap) {
                SheetActionRow(
                    icon = ClarityIcons.swap,
                    label = stringResource(R.string.action_swap),
                    onClick = { close(); onSwap() },
                )
            }
            if (onMoveToTop != null) {
                SheetActionRow(
                    icon = ClarityIcons.moveToFront,
                    label = stringResource(R.string.area_move_to_top),
                    onClick = { close(); onMoveToTop() },
                )
            }
            SheetActionRow(
                icon = ClarityIcons.edit,
                label = stringResource(R.string.area_edit),
                onClick = { close(); onEdit() },
            )
            SheetActionRow(
                icon = ClarityIcons.archive,
                label = stringResource(R.string.action_archive),
                onClick = { close(); onArchive() },
            )
            SheetActionRow(
                icon = ClarityIcons.deleteSwipe,
                label = stringResource(R.string.area_delete),
                onClick = { close(); onDelete() },
                tint = colors.deleteMuted,
            )
        }
    }
}

// Swap and queue choosers -----------------------------------------------------

/** Names the item being demoted, so nothing disappears silently. No warning tone. */
@Composable
fun SwapChooserSheet(
    demotedTitle: String?,
    queue: List<ItemState>,
    onChoose: (ItemState) -> Unit,
    onDismiss: () -> Unit,
) {
    val colors = LocalClarityColors.current
    val type = LocalClarityTypography.current

    ClaritySheet(onDismiss = onDismiss, title = stringResource(R.string.sheet_swap_title)) {
        Column {
            if (demotedTitle != null) {
                Column(modifier = Modifier.padding(horizontal = SheetPadding)) {
                    Text(text = demotedTitle, style = type.bodyStrong, color = colors.inkPrimary)
                    Text(
                        text = stringResource(R.string.swap_demoting),
                        style = type.caption,
                        // This sentence is the only place the sheet says what happens
                        // to the item being replaced, which makes it the one line here
                        // a person has to be able to read.
                        color = colors.inkSecondary,
                    )
                }
                Spacer(Modifier.height(ClaritySpacing.scaled(18.dp)))
            }
            queue.forEach { item ->
                QueueRow(item = item, onClick = { onChoose(item) })
            }
            Spacer(Modifier.height(ClaritySpacing.scaled(10.dp)))
            Column(modifier = Modifier.padding(horizontal = SheetPadding)) {
                ClarityButton(
                    label = stringResource(R.string.action_never_mind),
                    role = ClarityButtonRole.TERTIARY,
                    onClick = LocalSheetClose.current,
                )
            }
        }
    }
}

/** Dismissing leaves the area idle, which is a supported state and not a failure. */
@Composable
fun QueueChooserSheet(
    queue: List<ItemState>,
    onChoose: (ItemState) -> Unit,
    onDismiss: () -> Unit,
) {
    ClaritySheet(onDismiss = onDismiss, title = stringResource(R.string.sheet_queue_choice_title)) {
        Column {
            queue.forEach { item ->
                QueueRow(item = item, onClick = { onChoose(item) })
            }
            Spacer(Modifier.height(ClaritySpacing.scaled(10.dp)))
            Column(modifier = Modifier.padding(horizontal = SheetPadding)) {
                ClarityButton(
                    label = stringResource(R.string.queue_choice_dismiss),
                    role = ClarityButtonRole.TERTIARY,
                    onClick = LocalSheetClose.current,
                )
            }
        }
    }
}

// Delete an area --------------------------------------------------------------

/**
 * A typed confirmation rather than an undo, because an area carries everything
 * inside it and a five second window is not a fair amount of time to notice.
 */
@Composable
fun DeleteAreaSheet(
    areaName: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    val colors = LocalClarityColors.current
    val type = LocalClarityTypography.current
    var typed by remember { mutableStateOf("") }
    val required = stringResource(R.string.delete_area_confirm_word)
    // **Case insensitive, matching the two settings sheets that ask the same thing.**
    //
    // `confirmationMatches` in SettingsSheets.kt states the argument and is used by Erase
    // and by Replace on import; this third confirmation was written separately and pinned
    // `ignoreCase = false`, so typing `delete` on a phone that autocapitalizes nothing
    // left the button grey with no explanation of why. The guard is meant to prove
    // deliberateness, and typing the word is the proof. Holding shift is not.
    val armed = confirmationMatches(typed, required)
    val haptics = com.kamsiob.claritynow.ui.theme.LocalClarityHaptics.current

    LaunchedEffect(armed) { if (armed) haptics.perform(ClarityHapticEvent.WARN) }

    ClaritySheet(onDismiss = onDismiss) {
        Column(modifier = Modifier.padding(horizontal = SheetPadding)) {
            Text(
                text = stringResource(R.string.sheet_delete_area_title),
                style = type.readSerif,
                color = colors.inkPrimary,
            )
            Spacer(Modifier.height(ClaritySpacing.scaled(12.dp)))
            Text(text = areaName, style = type.bodyStrong, color = colors.inkSecondary)
            Spacer(Modifier.height(ClaritySpacing.scaled(10.dp)))
            Text(
                text = stringResource(R.string.delete_area_body),
                style = type.body,
                color = colors.inkSecondary,
            )
            Spacer(Modifier.height(ClaritySpacing.scaled(22.dp)))
            ClarityTextField(
                value = typed,
                onValueChange = { typed = it },
                label = stringResource(R.string.delete_area_confirm_hint),
            )
            Spacer(Modifier.height(ClaritySpacing.scaled(22.dp)))
            ClarityButton(
                label = stringResource(R.string.action_delete),
                role = ClarityButtonRole.DESTRUCTIVE,
                enabled = armed,
                onClick = onConfirm,
            )
            Spacer(Modifier.height(ClaritySpacing.scaled(8.dp)))
            ClarityButton(
                label = stringResource(R.string.delete_area_keep),
                role = ClarityButtonRole.TERTIARY,
                onClick = LocalSheetClose.current,
            )
        }
    }
}
