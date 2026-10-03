package com.cmatuteortega.monoburro.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.outlined.LunchDining
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.ShoppingBasket
import androidx.compose.material.icons.outlined.TrackChanges
import androidx.compose.material.icons.rounded.LunchDining
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.ShoppingBasket
import androidx.compose.material.icons.rounded.TrackChanges
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cmatuteortega.monoburro.model.Mode
import com.cmatuteortega.monoburro.storage.Tab
import com.cmatuteortega.monoburro.ui.screens.BurritoDetailScreen
import com.cmatuteortega.monoburro.ui.screens.BurritosScreen
import com.cmatuteortega.monoburro.ui.screens.GoalsScreen
import com.cmatuteortega.monoburro.ui.screens.ProfileScreen
import com.cmatuteortega.monoburro.ui.screens.ProposalsScreen
import com.cmatuteortega.monoburro.ui.screens.ShoppingScreen

private fun Tab.icon(selected: Boolean): ImageVector = when (this) {
    Tab.BURRITOS -> if (selected) Icons.Rounded.LunchDining else Icons.Outlined.LunchDining
    Tab.SHOPPING -> if (selected) Icons.Rounded.ShoppingBasket else Icons.Outlined.ShoppingBasket
    Tab.GOALS -> if (selected) Icons.Rounded.TrackChanges else Icons.Outlined.TrackChanges
    Tab.PROFILE -> if (selected) Icons.Rounded.Person else Icons.Outlined.Person
}

/** The large title over each tab. */
private val Tab.title: String
    get() = when (this) {
        Tab.BURRITOS -> "Your burritos"
        Tab.SHOPPING -> "Shopping list"
        Tab.GOALS -> "Goals"
        Tab.PROFILE -> "Profile"
    }

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
            if (pushed) {
                PushedTopBar(title = if (nav.suggesting) "New burrito" else "", onBack = { vm.navBack() })
            } else {
                TabTopBar(title = nav.tab.title, mode = state.mode ?: Mode.BURRO, onModeBadge = onModeBadge)
            }
        },
        bottomBar = {
            if (!pushed) {
                val toBuy = state.shopping.count { !it.checked }
                val cs = MaterialTheme.colorScheme
                NavigationBar(containerColor = cs.surface, tonalElevation = 0.dp) {
                    Tab.entries.forEach { tab ->
                        val selected = nav.tab == tab
                        NavigationBarItem(
                            selected = selected,
                            onClick = { vm.openTab(tab) },
                            icon = {
                                BadgedBox(badge = { if (tab == Tab.SHOPPING && toBuy > 0) Badge { Text("$toBuy") } }) {
                                    Icon(tab.icon(selected), contentDescription = null)
                                }
                            },
                            label = { Text(tab.label) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = cs.onPrimaryContainer,
                                selectedTextColor = cs.onSurface,
                                indicatorColor = cs.primaryContainer,
                                unselectedIconColor = cs.onSurfaceVariant,
                                unselectedTextColor = cs.onSurfaceVariant,
                            ),
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
                transitionSpec = {
                    val push = targetState.startsWith("burrito:") || targetState == "suggest"
                    val pop = initialState.startsWith("burrito:") || initialState == "suggest"
                    when {
                        push && !pop -> (slideInHorizontally { it / 4 } + fadeIn()) togetherWith fadeOut()
                        pop && !push -> fadeIn() togetherWith (slideOutHorizontally { it / 4 } + fadeOut())
                        else -> fadeIn() togetherWith fadeOut()
                    }
                },
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
                        actionLabel = { "Add ${it.name.lowercase()} to my burritos" },
                    )
                    screen == Tab.BURRITOS.name -> BurritosScreen(
                        burritos = state.burritos,
                        dailyKcal = state.dailyGoals.kcal,
                        burritosPerDay = state.burritosPerDay,
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
                        onTheme = vm::setTheme,
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

/** A tab's large title, with the mode pill on the right. */
@Composable
internal fun TabTopBar(title: String, mode: Mode, onModeBadge: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .padding(start = ScreenPadding, end = 12.dp, top = 12.dp, bottom = 4.dp)
            .height(52.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            title,
            style = MaterialTheme.typography.headlineMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        ModeBadge(mode, onModeBadge)
    }
}

/** A screen opened on top of a tab: back, and an optional title. */
@Composable
private fun PushedTopBar(title: String, onBack: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .padding(horizontal = 4.dp)
            .height(56.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back") }
        Spacer(Modifier.width(4.dp))
        Text(title, style = MaterialTheme.typography.titleLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}
