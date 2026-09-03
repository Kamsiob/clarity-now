package com.kamsiob.claritynow.ui.areas

import androidx.compose.ui.text.style.TextOverflow
import com.kamsiob.claritynow.ui.components.rememberPredictiveBack
import com.kamsiob.claritynow.ui.components.predictiveBackPreview
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.kamsiob.claritynow.R
import com.kamsiob.claritynow.ui.components.ClarityIcon
import com.kamsiob.claritynow.ui.components.ClarityIcons
import com.kamsiob.claritynow.ui.components.ClarityCard
import com.kamsiob.claritynow.ui.components.clarityClickable
import com.kamsiob.claritynow.ui.components.clarityFocusRing
import com.kamsiob.claritynow.ui.settings.PushedScreen
import com.kamsiob.claritynow.ui.theme.ClarityHapticEvent
import com.kamsiob.claritynow.ui.theme.ClaritySpacing
import com.kamsiob.claritynow.ui.theme.LocalClarityColors
import com.kamsiob.claritynow.ui.theme.LocalClarityShapes
import com.kamsiob.claritynow.ui.theme.LocalClarityTypography
import com.kamsiob.claritynow.ui.theme.clarityMotion
import com.kamsiob.claritynow.ui.theme.parseAreaColor

/**
 * Manage areas. The header glyph between the archive and the settings gear.
 *
 * ## Why there is a room for this
 *
 * The order of the areas is the most consequential setting in the app and it was the
 * only one with no control. `MASTER_BUILD_PROMPT.md` 8.4 puts the areas in a list the
 * person arranges, and the way to arrange it was to press and hold a card until it
 * lifted. That is a drag, WCAG 2.2 SC 2.5.7 wants a single pointer alternative for
 * every drag, and the alternative on offer was `Move to top` in a menu reached by the
 * same long press: one destination, from a gesture nobody is told about. Somebody who
 * wanted their third area second could not get there at all.
 *
 * A screen rather than one more row in Settings, because ordering is a thing done to
 * a list while looking at the list, and Settings is a place for switches.
 *
 * ## What this screen does not do, and where the rest went
 *
 * Renaming, recoloring, archiving and deleting an area are **not** duplicated here.
 * Tapping a row opens [AreaMenuSheet], which is the same menu the long press on the
 * Areas list opens, with the same eight actions in the same order. A management screen
 * that grows its own copy of those buttons is how the two copies drift, and the second
 * copy is always the one that misses the next action added.
 *
 * So the screen owns exactly one job that exists nowhere else, ordering, and states it
 * in one line under the title. Everything else is one tap away through a surface the
 * person may already know from the other room.
 *
 * ## design-v3.md 15
 *
 * The statistically common reorder control is a drag handle down the right side of each
 * row, which is the thing this screen exists because the app already had. The second
 * most common is a numbered position field. Both are refused for two labeled arrows,
 * because an arrow is the only one of the three that a person can operate without
 * knowing what it is going to do first: it moves the row one place, it says which row
 * and which way to a screen reader, and at the ends of the list it is visibly spent
 * rather than absent, so the row's shape does not change under the thumb as it travels
 * (COGA o4p01).
 */
/**
 * Hosts [ManageAreasScreen] and owns the back gesture.
 *
 * Predictive, issue #63, and the same arrangement the archive has: the screen is drawn
 * over the Areas tab rather than in place of it, so the room the preview uncovers is the
 * room back arrives in.
 */
@Composable
fun ManageAreasRoute(
    areas: List<AreaCardModel>,
    onOpenArea: (AreaCardModel) -> Unit,
    onMove: (AreaCardModel, Int) -> Unit,
    onNewArea: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val predictiveBack = rememberPredictiveBack(onBack = onBack)
    ManageAreasScreen(
        areas = areas,
        onOpenArea = onOpenArea,
        onMove = onMove,
        onNewArea = onNewArea,
        onBack = onBack,
        modifier = modifier.predictiveBackPreview(predictiveBack),
    )
}

@Composable
fun ManageAreasScreen(
    areas: List<AreaCardModel>,
    onOpenArea: (AreaCardModel) -> Unit,
    onMove: (AreaCardModel, Int) -> Unit,
    onNewArea: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalClarityColors.current
    val type = LocalClarityTypography.current

    PushedScreen(
        title = stringResource(R.string.manage_areas_title),
        onBack = onBack,
        modifier = modifier,
    ) {
        Text(
            text = if (areas.isEmpty()) {
                stringResource(R.string.manage_areas_empty_body)
            } else {
                stringResource(R.string.manage_areas_lede)
            },
            style = type.body,
            color = colors.inkSecondary,
        )
        Spacer(Modifier.height(ClaritySpacing.scaled(22.dp)))

        areas.forEachIndexed { index, area ->
            if (index > 0) Spacer(Modifier.height(ClaritySpacing.scaled(11.dp)))
            ManageAreaRow(
                area = area,
                position = index + 1,
                total = areas.size,
                onOpen = { onOpenArea(area) },
                onMoveUp = if (index == 0) null else ({ onMove(area, index - 1) }),
                onMoveDown = if (index == areas.lastIndex) null else ({ onMove(area, index + 1) }),
            )
        }

        Spacer(Modifier.height(ClaritySpacing.scaled(if (areas.isEmpty()) 0.dp else 18.dp)))
        NewAreaAction(onClick = onNewArea)
    }
}

/**
 * One area, its place in the run, and the two controls that change that place.
 *
 * **The row is one target and the arrows are two more inside it.** Nesting clickables
 * is ordinarily a mistake, and it is right here because the three do genuinely
 * different things and the arrows are the small ones: a thumb that misses an arrow
 * lands on the row and opens the menu, which offers `Move to top` and is therefore
 * not a dead end. The reverse arrangement, a menu-only row with the arrows moved out
 * to a toolbar, would put the control further from the thing it moves.
 *
 * **No wash and no stripe.** The one separation device is the card, `design-v3.md` 6.1
 * and `CLAUDE.md` rules 10 and 11. Identity is the dot, which is the same 9dp dot the
 * Areas card and the archive card carry, at the same two alphas for the same two
 * states.
 *
 * **The status line is a count and not a reading of it.** `queue_waiting` is the plural
 * the Areas card and the All Areas widget already share, and `queue_empty` is the
 * sentence the detail sheet uses under an empty queue. Nothing here says anything about
 * how long an area has been quiet or what that means, which is an observation and comes
 * through the engine from a corpus or not at all. `CLAUDE.md` rule 8.
 */
@Composable
private fun ManageAreaRow(
    area: AreaCardModel,
    position: Int,
    total: Int,
    onOpen: () -> Unit,
    onMoveUp: (() -> Unit)?,
    onMoveDown: (() -> Unit)?,
) {
    val colors = LocalClarityColors.current
    val type = LocalClarityTypography.current
    val shape = LocalClarityShapes.current.card
    val accent = parseAreaColor(area.colorHex)
    val interaction = remember { MutableInteractionSource() }

    // Spoken after the name, so TalkBack reads `Groceries, 2 of 5` and the two move
    // buttons that follow have somewhere to move from.
    val spokenPosition = stringResource(R.string.cd_manage_position, position, total)
    val openLabel = stringResource(R.string.cd_manage_open_area, area.name)

    ClarityCard(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .clarityFocusRing(interaction, shape)
            .clarityClickable(
                interactionSource = interaction,
                haptic = ClarityHapticEvent.TAP,
                role = Role.Button,
                pressShape = shape,
                onClickLabel = openLabel,
                onClick = onOpen,
            ),
        colors = colors,
    ) {
        Row(
            modifier = Modifier.padding(
                start = ClaritySpacing.cardPaddingHorizontal,
                // Less on the trailing edge, because the arrows carry their own 48dp
                // targets and padding them again would set them a thumb's width in
                // from the card they belong to.
                end = ClaritySpacing.scaled(6.dp),
                top = ClaritySpacing.scaled(10.dp),
                bottom = ClaritySpacing.scaled(10.dp),
            ),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f).padding(vertical = ClaritySpacing.scaled(4.dp))) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(ClaritySpacing.areaDot)
                            .clip(CircleShape)
                            .background(accent.copy(alpha = if (area.isIdle) 0.45f else 1f)),
                    )
                    Text(
                        text = area.name,
                        style = type.itemTitle,
                        color = colors.inkPrimary,
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .semantics { contentDescription = "${area.name}, $spokenPosition" },
                    )
                }
                Spacer(Modifier.height(ClaritySpacing.scaled(3.dp)))
                Text(
                    // **What is in the area, in the order a person would ask.**
                    //
                    // The first version of this row read the queue length alone, so an
                    // area with something active and an empty queue said `Nothing
                    // waiting.` under its name. That is true about the queue and false
                    // about the area, and the manage screen is exactly where somebody
                    // decides whether an area is worth keeping.
                    //
                    // The active item's own title first, because it is the one thing
                    // the app claims to be about; the count when there is no active
                    // item but a queue behind it; and the empty sentence only when
                    // there is genuinely nothing. All three are the person's own text
                    // or a direct readout of a queried number. CLAUDE.md rule 8.
                    text = area.activeItemTitle
                        ?: if (area.queueLength == 0) {
                            stringResource(R.string.queue_empty)
                        } else {
                            pluralStringResource(
                                R.plurals.queue_waiting,
                                area.queueLength,
                                area.queueLength,
                            )
                        },
                    style = type.caption,
                    color = colors.inkSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(start = ClaritySpacing.areaDot + 8.dp),
                )
            }

            MoveButton(
                up = true,
                description = stringResource(R.string.cd_manage_move_up, area.name),
                onClick = onMoveUp,
            )
            MoveButton(
                up = false,
                description = stringResource(R.string.cd_manage_move_down, area.name),
                onClick = onMoveDown,
            )
        }
    }
}

/**
 * One place up, or one place down.
 *
 * **Spent rather than gone at the ends of the list.** A null [onClick] draws the glyph
 * at the faint ink and takes itself out of the accessibility tree entirely, which is
 * two decisions and not one. Drawn, because the top card losing its up arrow would
 * change every row's width as a card travels and COGA o4p01 asks that controls not move
 * unexpectedly. Removed from the tree, because a screen reader announcing `Move
 * Groceries up, button` on the first row is worse than silence: it is an offer the app
 * cannot keep.
 *
 * The glyph is `expand_more` turned, which is the app's only chevron. A second asset
 * drawn the same way pointing the other direction is one more file to keep in step with
 * the first for no gain a person can see.
 */
@Composable
private fun MoveButton(
    up: Boolean,
    description: String,
    onClick: (() -> Unit)?,
) {
    val colors = LocalClarityColors.current
    val motion = clarityMotion()
    val interaction = remember { MutableInteractionSource() }
    val target by animateFloatAsState(
        targetValue = if (onClick == null) 0.28f else 1f,
        animationSpec = motion.easeOut(),
        label = "moveArrow",
    )

    Box(
        modifier = Modifier
            .size(ClaritySpacing.minTouchTarget)
            .clip(RoundedCornerShape(24.dp))
            .then(
                if (onClick == null) {
                    Modifier.clearAndSetSemantics { }
                } else {
                    Modifier
                        .clarityFocusRing(interaction, RoundedCornerShape(24.dp))
                        .semantics { contentDescription = description }
                        .clarityClickable(
                            interactionSource = interaction,
                            haptic = ClarityHapticEvent.TAP,
                            role = Role.Button,
                            pressShape = RoundedCornerShape(24.dp),
                            onClick = onClick,
                        )
                },
            ),
        contentAlignment = Alignment.Center,
    ) {
        ClarityIcon(
            icon = ClarityIcons.expand,
            contentDescription = null,
            tint = colors.inkSecondary,
            modifier = Modifier
                .size(24.dp)
                .alpha(target)
                .rotate(if (up) 180f else 0f),
        )
    }
}

/**
 * The way to make an area from the room where the areas are kept.
 *
 * The same shape and the same words as the row at the foot of the Areas list, because
 * it is the same door. A person who found this screen looking for `where do I add one`
 * should not have to go back to find out.
 */
@Composable
private fun NewAreaAction(onClick: () -> Unit) {
    val colors = LocalClarityColors.current
    val type = LocalClarityTypography.current
    val interaction = remember { MutableInteractionSource() }
    val shape = LocalClarityShapes.current.card

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = ClaritySpacing.minTouchTarget)
            .clip(shape)
            .clarityFocusRing(interaction, shape)
            .clarityClickable(
                interactionSource = interaction,
                haptic = ClarityHapticEvent.TAP,
                role = Role.Button,
                pressShape = shape,
                onClick = onClick,
            )
            .padding(
                horizontal = ClaritySpacing.cardPaddingHorizontal,
                vertical = ClaritySpacing.scaled(14.dp),
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ClarityIcon(
            icon = ClarityIcons.add,
            contentDescription = null,
            tint = colors.inkSecondary,
            modifier = Modifier.size(20.dp),
        )
        Spacer(Modifier.width(ClaritySpacing.snug))
        Text(
            text = stringResource(R.string.areas_new_area),
            style = type.label,
            color = colors.inkSecondary,
        )
    }
}
