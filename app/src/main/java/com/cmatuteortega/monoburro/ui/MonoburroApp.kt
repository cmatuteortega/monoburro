package com.cmatuteortega.monoburro.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.LunchDining
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cmatuteortega.monoburro.BuildConfig
import com.cmatuteortega.monoburro.billing.MonoBilling
import com.cmatuteortega.monoburro.model.Mode
import com.cmatuteortega.monoburro.storage.Step
import com.cmatuteortega.monoburro.ui.screens.LandingScreen
import com.cmatuteortega.monoburro.ui.screens.ProposalsScreen
import com.cmatuteortega.monoburro.ui.screens.RatioScreen
import com.cmatuteortega.monoburro.ui.screens.SwipeScreen
import com.cmatuteortega.monoburro.ui.theme.MonoburroTheme
import com.cmatuteortega.monoburro.ui.theme.isDark

private val STEP_TITLES = mapOf(
    Step.SWIPE to "Your tastes",
    Step.RATIOS to "Your ratios",
    Step.PROPOSALS to "Pick a burrito",
)

/** Which mode sheet is open: the paywall (from the landing or the Burro badge) or Mono's own. */
private enum class Sheet { PAYWALL_LANDING, PAYWALL, MONO }

@Composable
fun MonoburroApp(vm: AppViewModel, onDarkChanged: (Boolean) -> Unit, onSubscribe: () -> Unit) {
    val state by vm.state.collectAsStateWithLifecycle()
    val billing by vm.billingState.collectAsStateWithLifecycle()
    val dark = isDark(state.themeMode)
    LaunchedEffect(dark) { onDarkChanged(dark) }

    MonoburroTheme(dark) {
        var sheet by remember { mutableStateOf<Sheet?>(null) }
        val uriHandler = LocalUriHandler.current
        val closeSheet = {
            if (sheet != Sheet.MONO) vm.cancelMono()
            vm.clearBillingMessage()
            sheet = null
        }

        // Purchase confirmed (or simulated): the paywall has done its job.
        LaunchedEffect(state.mode) {
            if (state.mode == Mode.MONO && sheet != Sheet.MONO) sheet = null
        }

        AnimatedContent(
            targetState = state.mode == null,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "landing",
        ) { landing ->
            if (landing) {
                LandingScreen(
                    price = billing.price ?: MonoBilling.FALLBACK_PRICE,
                    onMono = { if (!vm.chooseMono()) sheet = Sheet.PAYWALL_LANDING },
                    onBurro = vm::chooseBurro,
                )
            } else {
                val onModeBadge = { sheet = if (state.mode == Mode.MONO) Sheet.MONO else Sheet.PAYWALL }
                AnimatedContent(
                    targetState = state.onboarded,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "menu",
                ) { onboarded ->
                    if (onboarded) {
                        MainMenu(vm, onModeBadge = onModeBadge, onPaywall = { sheet = Sheet.PAYWALL })
                    } else {
                        Onboarding(vm, onModeBadge = onModeBadge)
                    }
                }
            }
        }

        when (sheet) {
            Sheet.PAYWALL, Sheet.PAYWALL_LANDING -> PaywallSheet(
                billing = billing,
                debugBuild = BuildConfig.DEBUG,
                onSubscribe = onSubscribe,
                onSimulate = vm::simulateMono,
                onRestore = vm::refreshBilling,
                onDismiss = closeSheet,
                dismissLabel = if (sheet == Sheet.PAYWALL_LANDING) "Back" else "Stay Burro",
            )
            Sheet.MONO -> MonoSheet(
                simulated = BuildConfig.DEBUG && state.debugMono && !state.monoEntitled,
                onManage = { uriHandler.openUri(MonoBilling.MANAGE_URL) },
                onEndSimulated = { sheet = null; vm.endSimulatedMono() },
                onDismiss = closeSheet,
            )
            null -> Unit
        }
    }
}

@Composable
private fun Onboarding(vm: AppViewModel, onModeBadge: () -> Unit) {
    val state by vm.state.collectAsStateWithLifecycle()
    val notice by vm.notice.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    var confirmRestart by remember { mutableStateOf(false) }

    LaunchedEffect(notice) {
        notice?.let {
            snackbar.showSnackbar(it)
            vm.clearNotice()
        }
    }

    BackHandler(enabled = state.step != Step.SWIPE) { vm.back() }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = {
            TopBar(
                step = state.step,
                mode = state.mode ?: Mode.BURRO,
                onModeBadge = onModeBadge,
                onBack = vm::back,
                onRestart = { confirmRestart = true },
                onBackToMenu = if (state.burritos.isNotEmpty()) vm::backToMenu else null,
            )
        },
    ) { padding ->
        AnimatedContent(
            targetState = state.step,
            modifier = Modifier.padding(top = padding.calculateTopPadding()).fillMaxSize(),
            transitionSpec = {
                val forward = targetState.ordinal > initialState.ordinal
                (slideInHorizontally { if (forward) it / 3 else -it / 3 } + fadeIn()) togetherWith
                    (slideOutHorizontally { if (forward) -it / 3 else it / 3 } + fadeOut())
            },
            label = "step",
        ) { step ->
            when (step) {
                Step.SWIPE -> SwipeScreen(
                    state = state,
                    onSwipe = vm::swipe,
                    onUndo = vm::undo,
                    onKeepSwiping = vm::keepSwiping,
                    onContinue = { vm.goTo(Step.RATIOS) },
                )
                Step.RATIOS -> RatioScreen(
                    prefs = state.prefs,
                    onRatio = vm::setRatio,
                    onResetRatios = vm::resetRatios,
                    onCount = vm::setBurritoCount,
                    onTortilla = vm::setTortilla,
                    onTargets = vm::setTargets,
                    onContinue = { vm.goTo(Step.PROPOSALS) },
                )
                Step.PROPOSALS -> ProposalsScreen(
                    prefs = state.prefs,
                    onChoose = vm::choose,
                    onBackToSwipe = { vm.goTo(Step.SWIPE) },
                )
            }
        }
    }

    if (confirmRestart) {
        AlertDialog(
            onDismissRequest = { confirmRestart = false },
            title = { Text("Restart onboarding?") },
            text = { Text("Your swipes and ratios will be cleared.") },
            confirmButton = {
                TextButton(onClick = { confirmRestart = false; vm.restart() }) { Text("Restart") }
            },
            dismissButton = { TextButton(onClick = { confirmRestart = false }) { Text("Cancel") } },
        )
    }
}

@Composable
private fun TopBar(
    step: Step,
    onBack: () -> Unit,
    mode: Mode,
    onModeBadge: () -> Unit,
    onRestart: () -> Unit,
    onBackToMenu: (() -> Unit)?,
) {
    var menu by remember { mutableStateOf(false) }
    val cs = MaterialTheme.colorScheme
    Column(
        Modifier
            .fillMaxWidth()
            .background(cs.background)
            .statusBarsPadding()
            .padding(horizontal = 8.dp, vertical = 4.dp),
    ) {
        Row(Modifier.height(56.dp), verticalAlignment = Alignment.CenterVertically) {
            if (step != Step.SWIPE) {
                IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back") }
            } else {
                Spacer(Modifier.width(12.dp))
            }
            Column(Modifier.weight(1f).padding(start = 4.dp)) {
                Text(
                    "STEP ${step.ordinal + 1} OF ${Step.entries.size}",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp,
                    color = cs.primary,
                )
                Text(STEP_TITLES.getValue(step), style = MaterialTheme.typography.titleLarge)
            }
            Box {
                IconButton(onClick = { menu = true }) { Icon(Icons.Rounded.MoreVert, contentDescription = "More") }
                DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
                    onBackToMenu?.let { back ->
                        DropdownMenuItem(
                            text = { Text("Back to my burritos") },
                            leadingIcon = { Icon(Icons.Rounded.LunchDining, contentDescription = null) },
                            onClick = { menu = false; back() },
                        )
                    }
                    DropdownMenuItem(
                        text = { Text("Restart onboarding") },
                        leadingIcon = { Icon(Icons.Rounded.Refresh, contentDescription = null) },
                        onClick = { menu = false; onRestart() },
                    )
                }
            }
            ModeBadge(mode, onModeBadge)
        }
        Row(
            Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Step.entries.forEach {
                val progress by animateFloatAsState(if (it.ordinal <= step.ordinal) 1f else 0f, label = "segment")
                Box(
                    Modifier
                        .weight(1f)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(cs.outlineVariant),
                ) {
                    Box(Modifier.fillMaxWidth(progress).fillMaxHeight().background(cs.primary))
                }
            }
        }
    }
}

/**
 * The chosen mode, top right on every main screen: a gold "Mono" pill, or
 * for Burro a pill inviting the upgrade. Tapping opens the Mono sheet or the
 * paywall.
 */
@Composable
internal fun ModeBadge(mode: Mode, onClick: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    val mono = mode == Mode.MONO
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(50),
        color = if (mono) cs.tertiaryContainer else cs.surface,
        contentColor = if (mono) cs.onTertiaryContainer else cs.onSurface,
        border = BorderStroke(1.dp, if (mono) cs.tertiary.copy(alpha = 0.5f) else cs.outlineVariant),
        modifier = Modifier.padding(horizontal = 4.dp).height(40.dp),
    ) {
        Row(Modifier.padding(start = 6.dp, end = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(mode.displayEmoji, fontSize = 20.sp)
            Text(
                if (mono) "Mono" else "Go Mono",
                style = MaterialTheme.typography.labelLarge,
                fontSize = 14.sp,
                color = if (mono) cs.onTertiaryContainer else cs.primary,
                modifier = Modifier.padding(start = 6.dp),
            )
        }
    }
}
