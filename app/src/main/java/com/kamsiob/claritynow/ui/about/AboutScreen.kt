package com.kamsiob.claritynow.ui.about

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.kamsiob.claritynow.BuildConfig
import com.kamsiob.claritynow.R
import com.kamsiob.claritynow.ui.components.ClarityIcon
import com.kamsiob.claritynow.ui.components.ClarityIcons
import com.kamsiob.claritynow.ui.components.Sidehead
import com.kamsiob.claritynow.ui.settings.PushedScreen
import com.kamsiob.claritynow.ui.theme.LocalClarityColors
import com.kamsiob.claritynow.ui.theme.LocalClarityShapes
import com.kamsiob.claritynow.ui.theme.LocalClarityTypography
import com.kamsiob.claritynow.ui.theme.MarkBackground
import com.kamsiob.claritynow.ui.theme.MarkForeground

/**
 * About, MASTER_BUILD_PROMPT 14.4.
 *
 * The mark at 62dp on `#141A2E`, the name in `displayTitle`, the version and the maker,
 * one paragraph in `bodySerif`, a quiet `Elsewhere` list, the support block, then the
 * license lines.
 *
 * **The links are findable but subordinate and the support block is the only warm
 * colored element on the screen**, which is the whole hierarchy of this surface. That
 * is why the `Elsewhere` rows carry no accent and no chevron, and why the license lines
 * at the foot are caption `inkTertiary`: everything under the paragraph is there to be
 * found by somebody looking for it, and nothing under the paragraph competes with the
 * one element that is asking for something.
 *
 * **One sentence specified for this screen is deliberately absent.** 14.4 requires
 * `Clarity Now is a productivity tool. It does not provide medical advice, diagnosis or
 * treatment.` verbatim under the paragraph, and marks it pending phase 13, where 16.11
 * requires the identical sentence in the store listing at the same time. Shipping it
 * early would put it in the app before the listing it has to match.
 */
@Composable
internal fun AboutScreen(onBack: () -> Unit, modifier: Modifier = Modifier) {
    val colors = LocalClarityColors.current
    val type = LocalClarityTypography.current
    val shapes = LocalClarityShapes.current
    val context = LocalContext.current

    PushedScreen(
        title = stringResource(R.string.about_title),
        onBack = onBack,
        modifier = modifier,
    ) {
        Box(
            modifier = Modifier
                .size(MARK_SIZE)
                .clip(shapes.markBadge)
                .background(MarkBackground),
            contentAlignment = Alignment.Center,
        ) {
            ClarityIcon(
                icon = ClarityIcons.mark,
                contentDescription = null,
                tint = MarkForeground,
                modifier = Modifier.size(MARK_SIZE),
            )
        }
        Spacer(Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.about_name),
            style = type.displayTitle,
            color = colors.inkPrimary,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = stringResource(R.string.about_version, BuildConfig.VERSION_NAME),
            style = type.caption,
            color = colors.inkTertiary,
        )
        Spacer(Modifier.height(20.dp))
        Text(
            text = stringResource(R.string.about_paragraph),
            style = type.bodySerif,
            color = colors.inkSecondary,
        )

        Spacer(Modifier.height(30.dp))
        Sidehead(text = stringResource(R.string.about_elsewhere), modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(4.dp))
        ElsewhereRow(
            icon = ClarityIcons.openExternal,
            label = stringResource(R.string.about_link_youtube),
            destination = stringResource(R.string.about_link_youtube_value),
            onClick = { openExternalLink(context, ClarityLinks.YOUTUBE) },
        )
        ElsewhereRow(
            icon = ClarityIcons.openExternal,
            label = stringResource(R.string.about_link_source),
            destination = stringResource(R.string.about_link_source_value),
            onClick = { openExternalLink(context, ClarityLinks.SOURCE) },
        )
        ElsewhereRow(
            icon = ClarityIcons.openExternal,
            label = stringResource(R.string.about_link_website),
            destination = stringResource(R.string.about_link_website_value),
            onClick = { openExternalLink(context, ClarityLinks.WEBSITE) },
        )
        ElsewhereRow(
            icon = ClarityIcons.openExternal,
            label = stringResource(R.string.about_link_lab),
            destination = stringResource(R.string.about_link_lab_value),
            onClick = { openExternalLink(context, ClarityLinks.LAB) },
        )
        ElsewhereRow(
            icon = ClarityIcons.feedback,
            label = stringResource(R.string.about_link_feedback),
            destination = stringResource(R.string.about_link_feedback_value),
            onClick = { openExternalLink(context, ClarityLinks.FEEDBACK) },
        )

        Spacer(Modifier.height(30.dp))
        SupportBlock()

        Spacer(Modifier.height(26.dp))
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf(
                R.string.about_license_app,
                R.string.about_license_fonts,
                R.string.about_license_symbols,
            ).forEach { line ->
                Text(
                    text = stringResource(line),
                    style = type.caption,
                    color = colors.inkTertiary,
                )
            }
        }
    }
}

/**
 * design-v3.md 4.2: the About screen draws the mark at 62dp on a 16dp rounded square.
 *
 * The glyph is drawn at the full badge size rather than inset, because the artwork
 * carries its own margin: 4.1 lays the mark out on a 100 unit square whose content
 * spans 14 to 86, so a 62dp glyph puts the front card at 44.6dp with 8.7dp of air
 * around it, which is the proportion the launcher icon uses. Insetting it here would
 * inset it twice.
 *
 * `ColorFilter.tint` blends `SrcIn`, so the 26 and 50 percent fills on the two cards
 * behind survive the tint and the mark keeps the depth 4.1 says is load bearing.
 */
private val MARK_SIZE = 62.dp
