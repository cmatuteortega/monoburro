package com.cmatuteortega.monoburro.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cmatuteortega.monoburro.storage.Step
import com.cmatuteortega.monoburro.storage.ThemeMode
import com.cmatuteortega.monoburro.ui.screens.BatchScreen
import com.cmatuteortega.monoburro.ui.screens.ProposalsScreen
import com.cmatuteortega.monoburro.ui.screens.RatioScreen
import com.cmatuteortega.monoburro.ui.screens.SwipeScreen
import com.cmatuteortega.monoburro.ui.theme.MonoburroTheme
import com.cmatuteortega.monoburro.ui.theme.isDark
import kotlinx.coroutines.launch

private val STEP_TITLES = mapOf(
    Step.SWIPE to "Your tastes",
    Step.RATIOS to "Your ratios",
    Step.PROPOSALS to "Pick a burrito",
    Step.BATCH to "Batch plan",
)

@Composable
fun MonoburroApp(vm: OnboardingViewModel, onDarkChanged: (Boolean) -> Unit) {
    val state by vm.state.collectAsStateWithLifecycle()
    val dark = isDark(state.themeMode)
    LaunchedEffect(dark) { onDarkChanged(dark) }

    MonoburroTheme(dark) {
        val snackbar = remember { SnackbarHostState() }
        val scope = rememberCoroutineScope()
        var confirmRestart by remember { mutableStateOf(false) }

        BackHandler(enabled = state.step != Step.SWIPE) { vm.back() }

        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            snackbarHost = { SnackbarHost(snackbar) },
            topBar = {
                TopBar(
                    step = state.step,
                    themeMode = state.themeMode,
                    onBack = vm::back,
                    onTheme = vm::cycleTheme,
                    onRestart = { confirmRestart = true },
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
                        chosenId = state.chosen?.id,
                        onChoose = vm::choose,
                        onBackToSwipe = { vm.goTo(Step.SWIPE) },
                    )
                    Step.BATCH -> BatchScreen(
                        proposal = state.chosen,
                        prefs = state.prefs,
                        onNoProposal = { vm.goTo(Step.PROPOSALS) },
                        onNext = { scope.launch { snackbar.showSnackbar("After-cooking flow is coming soon 🌯") } },
                    )
                }
            }
        }

        if (confirmRestart) {
            AlertDialog(
                onDismissRequest = { confirmRestart = false },
                title = { Text("Restart onboarding?") },
                text = { Text("Your swipes, ratios and chosen burrito will be cleared.") },
                confirmButton = {
                    TextButton(onClick = { confirmRestart = false; vm.restart() }) { Text("Restart") }
                },
                dismissButton = { TextButton(onClick = { confirmRestart = false }) { Text("Cancel") } },
            )
        }
    }
}

@Composable
private fun TopBar(
    step: Step,
    themeMode: ThemeMode,
    onBack: () -> Unit,
    onTheme: () -> Unit,
    onRestart: () -> Unit,
) {
    var menu by remember { mutableStateOf(false) }
    Column(
        Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .padding(horizontal = 8.dp, vertical = 4.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (step != Step.SWIPE) {
                IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") }
            } else {
                Text("🌯", style = MaterialTheme.typography.headlineSmall, modifier = Modifier.padding(horizontal = 12.dp))
            }
            Column(Modifier.weight(1f)) {
                Text("Monoburro", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
                Text(
                    "Step ${step.ordinal + 1} of ${Step.entries.size} · ${STEP_TITLES.getValue(step)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            IconButton(onClick = onTheme) {
                Text(
                    when (themeMode) {
                        ThemeMode.SYSTEM -> "🌓"
                        ThemeMode.LIGHT -> "☀️"
                        ThemeMode.DARK -> "🌙"
                    },
                    style = MaterialTheme.typography.titleLarge,
                )
            }
            Box {
                IconButton(onClick = { menu = true }) { Icon(Icons.Default.MoreVert, contentDescription = "More") }
                DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
                    DropdownMenuItem(
                        text = { Text("Restart onboarding") },
                        leadingIcon = { Icon(Icons.Default.Refresh, contentDescription = null) },
                        onClick = { menu = false; onRestart() },
                    )
                }
            }
        }
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Step.entries.forEach {
                Box(
                    Modifier
                        .weight(1f)
                        .height(5.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(
                            if (it.ordinal <= step.ordinal) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.outlineVariant,
                        ),
                )
            }
        }
    }
}

