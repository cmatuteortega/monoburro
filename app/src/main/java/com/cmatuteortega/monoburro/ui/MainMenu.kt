package com.cmatuteortega.monoburro.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cmatuteortega.monoburro.model.Mode
import com.cmatuteortega.monoburro.storage.Tab
import com.cmatuteortega.monoburro.storage.ThemeMode
import com.cmatuteortega.monoburro.ui.screens.BurritoDetailScreen
import com.cmatuteortega.monoburro.ui.screens.BurritosScreen
import com.cmatuteortega.monoburro.ui.screens.GoalsScreen
import com.cmatuteortega.monoburro.ui.screens.ProfileScreen
import com.cmatuteortega.monoburro.ui.screens.ProposalsScreen
import com.cmatuteortega.monoburro.ui.screens.ShoppingScreen

/**
 * The real app, once a burrito is picked: burritos, shopping list, goals and
 * profile on a bottom bar. A burrito or the suggestions open on top of the
 * Burritos tab, without the bar.
 */
@Composable
fun MainMenu(vm: AppViewModel, onModeBadge: () -> Unit, onPaywall: () -> Unit) {
    val state by vm.state.collectAsStateWithLifecycle()
    val nav by vm.nav.collectAsStateWithLifecycle()
    val notice by vm.notice.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    var confirmQuiz by remember { mutableStateOf(false) }

    LaunchedEffect(notice) {
        notice?.let {
            snackbar.showSnackbar(it)
            vm.clearNotice()
        }
    }

    val burrito = nav.burritoId?.let { id -> state.burritos.firstOrNull { it.id == id } }
    val pushed = burrito != null || nav.suggesting
    BackHandler(enabled = pushed || nav.tab != Tab.BURRITOS) { vm.navBack() }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = {
            MenuTopBar(
                title = when {
                    burrito != null -> burrito.name
                    nav.suggesting -> "New burrito"
                    else -> nav.tab.label
                },
                showBack = pushed,
                themeMode = state.themeMode,
                mode = state.mode ?: Mode.BURRO,
                onBack = { vm.navBack() },
                onTheme = vm::cycleTheme,
                onModeBadge = onModeBadge,
                onRedoQuiz = { confirmQuiz = true },
            )
        },
        bottomBar = {
            if (!pushed) {
                val toBuy = state.shopping.count { !it.checked }
                NavigationBar(containerColor = MaterialTheme.colorScheme.surfaceContainer) {
                    Tab.entries.forEach { tab ->
                        NavigationBarItem(
                            selected = nav.tab == tab,
                            onClick = { vm.openTab(tab) },
                            icon = {
                                BadgedBox(badge = { if (tab == Tab.SHOPPING && toBuy > 0) Badge { Text("$toBuy") } }) {
                                    Text(tab.emoji, fontSize = 22.sp)
                                }
                            },
                            label = { Text(tab.label) },
                        )
                    }
                }
            }
        },
    ) { padding ->
        Box(
            Modifier
                .padding(top = padding.calculateTopPadding(), bottom = if (pushed) 0.dp else padding.calculateBottomPadding())
                .fillMaxSize(),
        ) {
            AnimatedContent(
                targetState = when {
                    burrito != null -> "burrito:${burrito.id}"
                    nav.suggesting -> "suggest"
                    else -> nav.tab.name
                },
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "menu",
            ) { screen ->
                when {
                    screen.startsWith("burrito:") -> burrito?.let {
                        BurritoDetailScreen(
                            burrito = it,
                            prefs = state.prefs,
                            dailyGoals = state.dailyGoals,
                            burritosPerDay = state.burritosPerDay,
                            vm = vm,
                        )
                    }
                    screen == "suggest" -> ProposalsScreen(
                        prefs = state.prefs,
                        onChoose = vm::addBurrito,
                        onBackToSwipe = { confirmQuiz = true },
                        actionLabel = { "Add ${it.name.lowercase()} to my burritos →" },
                    )
                    screen == Tab.BURRITOS.name -> BurritosScreen(
                        burritos = state.burritos,
                        onOpen = vm::openBurrito,
                        onSuggest = vm::openSuggestions,
                        onBuildOwn = vm::addEmptyBurrito,
                    )
                    screen == Tab.SHOPPING.name -> ShoppingScreen(
                        items = state.shopping,
                        onToggle = vm::toggleShopping,
                        onRemove = vm::removeShopping,
                        onAdd = vm::addCustomItem,
                        onClearChecked = vm::clearChecked,
                        onClearAll = vm::clearShopping,
                        onGoToBurritos = { vm.openTab(Tab.BURRITOS) },
                    )
                    screen == Tab.GOALS.name -> GoalsScreen(
                        state = state,
                        onCustomGoals = vm::setCustomGoals,
                        onPerDay = vm::setBurritosPerDay,
                        onUseForSuggestions = vm::useGoalsForSuggestions,
                        onEditProfile = { vm.openTab(Tab.PROFILE) },
                    )
                    screen == Tab.PROFILE.name -> ProfileScreen(
                        state = state,
                        onProfile = vm::updateProfile,
                        onGoal = { if (!vm.setGoal(it)) onPaywall() },
                        onModeBadge = onModeBadge,
                        onRedoQuiz = { confirmQuiz = true },
                    )
                }
            }
        }
    }

    if (confirmQuiz) {
        AlertDialog(
            onDismissRequest = { confirmQuiz = false },
            title = { Text("Redo the taste quiz?") },
            text = { Text("Your swipes and ratios start over. Your burritos, shopping list, goals and profile stay.") },
            confirmButton = { TextButton(onClick = { confirmQuiz = false; vm.restart() }) { Text("Redo quiz") } },
            dismissButton = { TextButton(onClick = { confirmQuiz = false }) { Text("Cancel") } },
        )
    }
}

@Composable
private fun MenuTopBar(
    title: String,
    showBack: Boolean,
    themeMode: ThemeMode,
    mode: Mode,
    onBack: () -> Unit,
    onTheme: () -> Unit,
    onModeBadge: () -> Unit,
    onRedoQuiz: () -> Unit,
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
            if (showBack) {
                IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") }
            } else {
                Text("🌯", style = MaterialTheme.typography.headlineSmall, modifier = Modifier.padding(horizontal = 12.dp))
            }
            Column(Modifier.weight(1f)) {
                Text("Monoburro", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
                Text(
                    title,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                )
            }
            ThemeButton(themeMode, onTheme)
            Box {
                IconButton(onClick = { menu = true }) { Icon(Icons.Default.MoreVert, contentDescription = "More") }
                DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
                    DropdownMenuItem(
                        text = { Text("Redo taste quiz") },
                        leadingIcon = { Icon(Icons.Default.Refresh, contentDescription = null) },
                        onClick = { menu = false; onRedoQuiz() },
                    )
                }
            }
            ModeBadge(mode, onModeBadge)
        }
    }
}
