package com.kamsiob.claritynow.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.kamsiob.claritynow.R

/**
 * design-v3.md section 7. Material Symbols Rounded at weight 500, committed as
 * vector drawables rather than pulled from an icon font at runtime.
 *
 * Icons are named by what they mean here, so a screen never reaches for a resource
 * id directly and the mapping can change in one place. There is deliberately no
 * sparkle or magic wand entry: `auto_awesome` reads as an AI affordance, which is
 * the opposite of what this app claims about itself.
 */
object ClarityIcons {
    val areas = R.drawable.ic_home
    val areasFilled = R.drawable.ic_home_filled
    val momentum = R.drawable.ic_arrow_outward
    val momentumFilled = R.drawable.ic_arrow_outward_filled
    val report = R.drawable.ic_article
    val reportFilled = R.drawable.ic_article_filled
    val trail = R.drawable.ic_history
    val trailFilled = R.drawable.ic_history_filled

    val add = R.drawable.ic_add
    val focus = R.drawable.ic_play_arrow
    val pulse = R.drawable.ic_graphic_eq
    val settings = R.drawable.ic_settings
    val archive = R.drawable.ic_inventory_2
    val unarchive = R.drawable.ic_unarchive
    val completed = R.drawable.ic_check_circle
    val promoted = R.drawable.ic_arrow_circle_up
    val focusEvent = R.drawable.ic_timer
    val regenerate = R.drawable.ic_refresh
    val copy = R.drawable.ic_content_copy
    val export = R.drawable.ic_file_download
    val importData = R.drawable.ic_file_upload
    val erase = R.drawable.ic_delete_outline
    val privacy = R.drawable.ic_shield
    val licenses = R.drawable.ic_menu_book
    val feedback = R.drawable.ic_mail
    val support = R.drawable.ic_favorite
    val reorder = R.drawable.ic_drag_handle
    val swap = R.drawable.ic_swap_vert
    val deleteSwipe = R.drawable.ic_delete_outline

    val check = R.drawable.ic_check
    val close = R.drawable.ic_close
    val chevron = R.drawable.ic_chevron_right
    val back = R.drawable.ic_arrow_back
    val expand = R.drawable.ic_expand_more
    val edit = R.drawable.ic_edit
    val editArea = R.drawable.ic_tune
    val overflow = R.drawable.ic_more_vert
    val moveToFront = R.drawable.ic_vertical_align_top
    val reminders = R.drawable.ic_notifications
    val appearance = R.drawable.ic_palette
    val time = R.drawable.ic_schedule
    val openExternal = R.drawable.ic_open_in_new

    val mark = R.drawable.ic_mark
}

/** Icons take ink colors or the surface accent, and are never multicolored. */
@Composable
fun ClarityIcon(
    @DrawableRes icon: Int,
    contentDescription: String?,
    tint: Color,
    modifier: Modifier = Modifier,
) {
    Image(
        painter = painterResource(icon),
        contentDescription = contentDescription,
        contentScale = ContentScale.Fit,
        colorFilter = ColorFilter.tint(tint),
        modifier = modifier,
    )
}
