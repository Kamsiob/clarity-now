package com.kamsiob.claritynow.ui.settings

import com.kamsiob.claritynow.ui.theme.clarityMotion
import com.kamsiob.claritynow.ui.nav.pushedScreenScaleOut
import com.kamsiob.claritynow.ui.nav.pushedScreenScaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.animation.fadeOut
import androidx.compose.animation.fadeIn
import androidx.compose.animation.AnimatedContent
import android.content.ContentResolver
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kamsiob.claritynow.data.export.BackupFormat
import com.kamsiob.claritynow.data.export.ImportRefusal
import com.kamsiob.claritynow.data.export.LocalFileTarget
import com.kamsiob.claritynow.data.export.SyncTarget
import com.kamsiob.claritynow.ui.components.predictiveBackPreview
import com.kamsiob.claritynow.ui.components.rememberPredictiveBack
import com.kamsiob.claritynow.ui.about.AboutScreen
import com.kamsiob.claritynow.ui.theme.LocalCalmMode
import com.kamsiob.claritynow.ui.theme.LocalClarityColors
import java.io.FileNotFoundException

/**
 * Settings and everything reachable from it, as one surface over the Areas tab.
 * design-v3.md 10.15.
 *
 * **It is hosted from inside the Areas tab rather than from `ClarityShell`, and that is
 * a seam this phase could not close.** 10.15 makes Settings a pushed screen, which
 * should cover the floating tab bar the way the Focus surface does; the shell draws
 * that bar as a sibling above the tab content and `ClarityShell.kt` was outside this
 * phase's file list. [PushedScreen] carries the consequence and the remedy. Everything
 * else about the destination is as 10.15 describes it: entered from the settings glyph
 * in the Areas header, left by back.
 *
 * The sub destinations are bottom sheets, which is what puts them in their own window
 * and therefore genuinely above the tab bar. About is a pushed screen over this one,
 * because 14.4 gives it a 62dp mark and a reading measure and a sheet is neither.
 *
 * ## The two file flows
 *
 * Export is a sheet and then a picker, in that order, because the optional password is
 * a decision about the file and has to be made before there is somewhere to put it.
 * Import is a picker and then, only if the file turns out to be protected, a sheet: a
 * password field shown to everybody importing an ordinary readable backup would be a
 * question asked for nothing.
 *
 * The picked location is kept here rather than in the ViewModel, so that a wrong
 * password can be retried against the same file without the Android types crossing that
 * line. `SyncTarget` is the seam and this is the only place a real one is built.
 */
@Composable
internal fun SettingsSurface(onDismiss: () -> Unit, modifier: Modifier = Modifier) {
    val colors = LocalClarityColors.current
    val calmMode = LocalCalmMode.current
    val context = LocalContext.current
    val viewModel: SettingsViewModel = viewModel(factory = SettingsViewModel.Factory)
    val state by viewModel.state.collectAsStateWithLifecycle()

    var sheet by remember { mutableStateOf(SettingsSheet.NONE) }
    var aboutOpen by remember { mutableStateOf(false) }

    // The password the export sheet collected, held only for the moment between it and
    // the picker. Never cleared by hand: the export coroutine is still reading it when
    // this composition moves on, and zeroing it here would encrypt a file with nulls.
    var exportPassword by remember { mutableStateOf<CharArray?>(null) }

    // The file being imported, kept so a wrong password can be tried again against it.
    var importUri by remember { mutableStateOf<Uri?>(null) }

    // A validated file waiting for a choice must not outlive the screen that asked for
    // it, and neither must the sentence about the last thing that happened.
    DisposableEffect(viewModel) {
        onDispose { viewModel.forgetTransientState() }
    }

    val resolver = context.contentResolver
    val zone = viewModel.zone

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument(BackupFormat.MIME_TYPE),
    ) { uri ->
        if (uri != null) viewModel.export(targetFor(resolver, uri), exportPassword)
        exportPassword = null
    }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
    ) { uri ->
        importUri = uri
        if (uri != null) viewModel.readForImport(targetFor(resolver, uri), null)
    }

    // Back leaves About first and Settings second. A sheet is a window of its own and
    // consumes back before this handler ever sees it, which is also what makes the
    // erase sheet's "back dismisses without erasing" true with nothing written here.
    //
    // Predictive, issue #63. This surface is drawn over the tab it was opened from, so
    // the preview uncovers the room back arrives in. **About is the one exception on this
    // screen and it keeps its preview anyway**: backing out of About goes to Settings,
    // which is one composition down rather than one screen behind, so what the gesture
    // shows there is the surface receding rather than a destination. That is the same
    // thing the platform does for a nested destination and it is honest about direction
    // if not about depth.
    val motion = clarityMotion()
    val predictiveBack = rememberPredictiveBack {
        if (aboutOpen) aboutOpen = false else onDismiss()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .predictiveBackPreview(predictiveBack)
            .background(colors.canvas),
    ) {
        // **About used to appear between two frames.** The same hard cut the archive and
        // manage areas had, and the same answer: a fade with a small scale from 0.97,
        // out faster than in, and no scale at all under reduced motion.
        //
        // `AnimatedContent` rather than two `AnimatedVisibility` blocks, because these
        // two are alternatives rather than a screen over a screen: Settings genuinely
        // leaves while About is up, so only one of them should be composed and the
        // transition should own the swap.
        AnimatedContent(
            targetState = aboutOpen,
            transitionSpec = {
                (fadeIn(motion.effects()) + pushedScreenScaleIn(motion))
                    .togetherWith(fadeOut(motion.effectsFast()) + pushedScreenScaleOut(motion))
            },
            label = "aboutSwap",
        ) { showingAbout ->
        if (showingAbout) {
            AboutScreen(onBack = { aboutOpen = false })
        } else {
            SettingsScreen(
                state = state,
                calmMode = calmMode,
                zone = zone,
                onBack = onDismiss,
                onThemeChange = viewModel::setTheme,
                onTextSizeChange = viewModel::setTextSize,
                onCalmModeChange = viewModel::setCalmMode,
                onFocusHighlightChange = viewModel::setFocusHighlight,
                onTransitionWarningChange = viewModel::setTransitionWarning,
                onAfterCompletingChange = viewModel::setAfterCompleting,
                onPulseRemindersChange = viewModel::setPulseReminders,
                onOpenReflection = { sheet = SettingsSheet.REFLECTION },
                onOpenSessionLength = { sheet = SettingsSheet.SESSION_LENGTH },
                onOpenReminderHour = { sheet = SettingsSheet.REMINDER_HOUR },
                onExport = { sheet = SettingsSheet.EXPORT },
                onImport = { importLauncher.launch(IMPORT_MIME_FILTER) },
                onOpenErase = { sheet = SettingsSheet.ERASE },
                onRebuildCache = { viewModel.rebuildCache() },
                onOpenPrivacy = { sheet = SettingsSheet.PRIVACY },
                onOpenLicenses = { sheet = SettingsSheet.LICENSES },
                // **Both close Settings, because both act on the screen behind it.**
                // The tutorial spotlights the FAB, an area card, two anchors and the tab
                // bar, every one of which is underneath this surface; the welcome replaces
                // the whole app. A row that sets a flag and leaves the person looking at
                // the row is the shape that made these two read as broken for four phases.
                onReplayTour = {
                    viewModel.replayTutorial()
                    onDismiss()
                },
                onReplayWelcome = {
                    viewModel.replayWelcome()
                    onDismiss()
                },
                onOpenAbout = { aboutOpen = true },
            )
        }
        }
    }

    when (sheet) {
        SettingsSheet.NONE -> Unit
        SettingsSheet.REFLECTION -> DailyReflectionSheet(onDismiss = { sheet = SettingsSheet.NONE })
        SettingsSheet.SESSION_LENGTH -> SessionLengthSheet(
            selected = state.focusMinutes,
            onSelect = viewModel::setFocusMinutes,
            onDismiss = { sheet = SettingsSheet.NONE },
        )

        SettingsSheet.REMINDER_HOUR -> ReminderHourSheet(
            selected = state.pulseReminderHour,
            onSelect = viewModel::setPulseReminderHour,
            onDismiss = { sheet = SettingsSheet.NONE },
        )

        SettingsSheet.EXPORT -> ExportSheet(
            onExport = { password ->
                exportPassword = password
                sheet = SettingsSheet.NONE
                exportLauncher.launch(viewModel.suggestedFileName())
            },
            onDismiss = { sheet = SettingsSheet.NONE },
        )

        SettingsSheet.PRIVACY -> PrivacySheet(onDismiss = { sheet = SettingsSheet.NONE })
        SettingsSheet.LICENSES -> LicensesSheet(onDismiss = { sheet = SettingsSheet.NONE })
        SettingsSheet.ERASE -> EraseSheet(
            onErase = {
                viewModel.eraseEverything()
                sheet = SettingsSheet.NONE
            },
            onDismiss = { sheet = SettingsSheet.NONE },
        )
    }

    // A protected file, asked about once and then asked about again if the password was
    // wrong. Driven by the refusal rather than by a tap, so the field cannot appear for
    // a file that did not ask for one.
    val refusal = (state.message as? DataMessage.ImportWasRefused)?.reason
    val protectedUri = importUri
    if (protectedUri != null && refusal != null && refusal.asksForAPassword()) {
        ImportPasswordSheet(
            refusal = refusal,
            onOpen = { password ->
                viewModel.dismissMessage()
                viewModel.readForImport(targetFor(resolver, protectedUri), password)
            },
            onDismiss = {
                viewModel.dismissMessage()
                importUri = null
            },
        )
    }

    // The choice a validated file offers. It appears because a file passed every check,
    // never because somebody opened something, so it is driven by the state rather than
    // by a tap.
    val opened = state.pendingImport
    if (opened != null) {
        ImportSheet(
            opened = opened,
            zone = zone,
            onConfirm = { mode ->
                importUri = null
                viewModel.confirmImport(mode)
            },
            onDismiss = {
                importUri = null
                viewModel.cancelImport()
            },
        )
    }
}

/** The two refusals that are a question rather than an ending. */
private fun ImportRefusal.asksForAPassword(): Boolean =
    this == ImportRefusal.PASSWORD_REQUIRED || this == ImportRefusal.WRONG_PASSWORD

/** Which sub sheet is showing. About is a pushed screen and is not one of these. */
private enum class SettingsSheet {
    NONE,
    REFLECTION,
    SESSION_LENGTH,
    REMINDER_HOUR,
    EXPORT,
    PRIVACY,
    LICENSES,
    ERASE,
}

/**
 * What the picker offers when importing.
 *
 * Deliberately everything rather than `BackupFormat.MIME_TYPE`. A file written by this
 * app, copied through a cloud drive, a chat app or a file manager, very often comes
 * back typed as `application/octet-stream`, and a filter that hid the person's own
 * backup from them would be a filter that looked correct and lost their data. Nothing
 * is trusted from the type either way: the file has to prove what it is.
 */
private val IMPORT_MIME_FILTER = arrayOf("*/*")

/**
 * A location the person picked, as the seam `data.export` takes.
 *
 * Both streams are opened lazily, which is what `SyncTarget` asks for, so building one
 * of these costs nothing and an export that is abandoned leaves nothing open. A
 * provider that hands back no stream is a `FileNotFoundException`, which is an
 * `IOException`, which is what both halves of the backup path are written to expect.
 *
 * "wt" truncates. The document the picker made is empty, but a person can pick a file
 * that already exists, and a shorter export written over a longer one would otherwise
 * leave the tail of the old file behind, which would read as damaged rather than as
 * absent.
 */
private fun targetFor(resolver: ContentResolver, uri: Uri): SyncTarget = LocalFileTarget(
    openSink = {
        resolver.openOutputStream(uri, "wt")
            ?: throw FileNotFoundException("nothing to write to at $uri")
    },
    openSource = {
        resolver.openInputStream(uri)
            ?: throw FileNotFoundException("nothing to read at $uri")
    },
)
