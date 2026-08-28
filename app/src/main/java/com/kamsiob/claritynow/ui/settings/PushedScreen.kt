package com.kamsiob.claritynow.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.kamsiob.claritynow.R
import com.kamsiob.claritynow.ui.components.ClarityIcon
import com.kamsiob.claritynow.ui.components.ClarityIcons
import com.kamsiob.claritynow.ui.components.TabBarHeight
import com.kamsiob.claritynow.ui.components.clarityClickable
import com.kamsiob.claritynow.ui.theme.ClarityHapticEvent
import com.kamsiob.claritynow.ui.theme.ClaritySpacing
import com.kamsiob.claritynow.ui.theme.LocalClarityColors
import com.kamsiob.claritynow.ui.theme.LocalClarityTypography

/**
 * A screen pushed over a tab, design-v3.md 10.15: entered from a glyph or a row, left
 * by back, and covering the tab it came from.
 *
 * **It reserves room at the foot for the floating tab bar, and that is a seam rather
 * than a design choice.** 10.15 makes Settings a pushed screen, which should cover the
 * tab bar entirely. `ClarityShell` draws that bar as a sibling above the tab content,
 * and hosting a pushed screen above it means hosting it from `ClarityShell`, which was
 * outside this phase's file list. So the surface is hosted from inside the Areas tab
 * and pads its content past the bar instead, which keeps everything reachable and
 * readable while the bar floats over the canvas. The fix is one branch in
 * `ClarityShell` beside the Focus surface, at which point this padding becomes the
 * ordinary navigation bar inset. `docs/BUILD_STATE.md` and the phase report both carry
 * it.
 *
 * The title is the serif `displayTitle` the Areas screen uses, so a pushed screen
 * announces itself the same way the home does rather than inventing a second voice for
 * the same job.
 */
@Composable
internal fun PushedScreen(
    title: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    val colors = LocalClarityColors.current
    val type = LocalClarityTypography.current
    val topInset = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val bottomInset = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    Box(modifier = modifier.fillMaxSize().background(colors.canvas)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(
                    start = ClaritySpacing.screenPadding,
                    end = ClaritySpacing.screenPadding,
                    top = topInset + 8.dp,
                    bottom = bottomInset + TabBarHeight + ClaritySpacing.tabBarInset + 32.dp,
                ),
        ) {
            Box(
                modifier = Modifier
                    // The target is 48dp and the glyph inside it is 22dp, so centering
                    // the target on the screen padding would set the glyph 13dp in from
                    // the title beneath it. The target keeps its size and moves; the
                    // glyph lands on the text edge.
                    .offset(x = -(ClaritySpacing.minTouchTarget - BACK_GLYPH) / 2)
                    .size(ClaritySpacing.minTouchTarget)
                    .clip(RoundedCornerShape(24.dp))
                    .clarityClickable(
                        haptic = ClarityHapticEvent.TAP,
                        role = Role.Button,
                        onClickLabel = stringResource(R.string.cd_settings_back),
                        onClick = onBack,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                ClarityIcon(
                    icon = ClarityIcons.back,
                    contentDescription = stringResource(R.string.cd_settings_back),
                    tint = colors.inkSecondary,
                    modifier = Modifier.size(BACK_GLYPH),
                )
            }
            Spacer(Modifier.height(6.dp))
            Text(
                text = title,
                style = type.displayTitle,
                color = colors.inkPrimary,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(22.dp))
            content()
        }
    }
}

/** design-v3.md section 7 glyph size for a header control, as the Areas header uses. */
private val BACK_GLYPH = 22.dp
