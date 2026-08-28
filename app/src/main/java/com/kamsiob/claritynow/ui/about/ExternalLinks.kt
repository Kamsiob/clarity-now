package com.kamsiob.claritynow.ui.about

import android.content.Context
import android.content.Intent
import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.kamsiob.claritynow.ui.components.ClarityIcon
import com.kamsiob.claritynow.ui.components.clarityClickable
import com.kamsiob.claritynow.ui.theme.ClarityHapticEvent
import com.kamsiob.claritynow.ui.theme.ClaritySpacing
import com.kamsiob.claritynow.ui.theme.LocalClarityColors
import com.kamsiob.claritynow.ui.theme.LocalClarityTypography

/**
 * Every destination this app can send somebody to, MASTER_BUILD_PROMPT 14.4 and 14.5.
 *
 * **They are constants here rather than string resources, on purpose.** A destination
 * is not copy: nobody translates a URL, and a resource is a thing that gets rendered
 * by mistake. That matters for exactly one of them. 14.5's copy rules forbid any
 * coffee or caffeine reference in the label or the body of the support block, and the
 * destination it names contains one, so the safest place for that string is somewhere
 * a `stringResource` call cannot reach.
 *
 * Opening any of them hands an intent to the system, which is not a network
 * capability: this app still declares no internet permission and still cannot open a
 * connection itself. A browser can.
 */
internal object ClarityLinks {
    const val YOUTUBE = "https://youtube.com/@kamsiob"
    const val SOURCE = "https://github.com/kamsiob/clarity-now"
    const val WEBSITE = "https://kamsiob.com"
    const val LAB = "https://t.me/+g5LKm9rUnNcxMjk5"
    const val FEEDBACK = "mailto:hello@kamsiob.com"
    const val SUPPORT = "https://buymeacoffee.com/kamsiob"
}

/**
 * Hands [url] to whatever the phone uses for it, and does nothing at all when there is
 * nothing to hand it to.
 *
 * A device with no browser and no mail client is unusual and is not this app's problem
 * to narrate. Saying "no app can open this" to somebody who tapped a link tells them
 * something they cannot act on, which is the same reasoning `NotificationPermission`
 * gives for never mentioning a refused permission.
 */
internal fun openExternalLink(context: Context, url: String) {
    // `runCatching` because there are three ways this throws, an absent handler, a
    // refused start and a malformed destination, and all three end the same way: the
    // person taps and stays where they were.
    runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, url.toUri())) }
}

/**
 * One row of the `Elsewhere` list, MASTER_BUILD_PROMPT 14.4.
 *
 * **Findable but subordinate**: an outlined icon at 50 percent opacity, the label, and
 * the destination trailing in caption inkTertiary. There is no chevron, because a
 * chevron in this design means a screen inside the app and every one of these leaves
 * it, and there is no accent color, because the support block below is the only warm
 * colored element on the screen.
 */
@Composable
internal fun ElsewhereRow(
    @DrawableRes icon: Int,
    label: String,
    destination: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalClarityColors.current
    val type = LocalClarityTypography.current
    Row(
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 48.dp)
            .clarityClickable(
                haptic = ClarityHapticEvent.TAP,
                role = Role.Button,
                onClickLabel = label,
                onClick = onClick,
            )
            .padding(vertical = ClaritySpacing.scaled(8.dp)),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ClarityIcon(
            icon = icon,
            contentDescription = null,
            tint = colors.inkSecondary,
            modifier = Modifier.size(18.dp).alpha(0.5f),
        )
        Spacer(Modifier.width(14.dp))
        Text(
            text = label,
            style = type.body,
            color = colors.inkPrimary,
            modifier = Modifier.weight(1f),
        )
        Spacer(Modifier.width(12.dp))
        // MASTER_BUILD_PROMPT 14.5 says "the destination trailing in caption
        // inkTertiary" and design-v3.md 3.1 says `inkTertiary` carries no text
        // anywhere in this app. design-v3.md wins on anything visual, CLAUDE.md's
        // authority order, and 13 states one floor: this measured 2.337 to one on the
        // canvas. The destination is the only thing on the row that says where a tap
        // is about to send somebody out of the app, so it is not an ornament.
        Text(text = destination, style = type.caption, color = colors.inkSecondary)
    }
}
