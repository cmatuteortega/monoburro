package com.cmatuteortega.monoburro.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cmatuteortega.monoburro.data.ingredientOrNull
import com.cmatuteortega.monoburro.model.Activity
import com.cmatuteortega.monoburro.model.Goal
import com.cmatuteortega.monoburro.model.Mode
import com.cmatuteortega.monoburro.model.Profile
import com.cmatuteortega.monoburro.model.Sex
import com.cmatuteortega.monoburro.model.can
import com.cmatuteortega.monoburro.storage.AppState
import com.cmatuteortega.monoburro.storage.ThemeMode
import com.cmatuteortega.monoburro.ui.ChoiceRow
import com.cmatuteortega.monoburro.ui.MenuCard
import com.cmatuteortega.monoburro.ui.NumberField
import com.cmatuteortega.monoburro.ui.Pill
import com.cmatuteortega.monoburro.ui.ScreenPadding
import com.cmatuteortega.monoburro.ui.SectionHeader
import com.cmatuteortega.monoburro.ui.displayEmoji
import com.cmatuteortega.monoburro.ui.goal
import kotlin.math.roundToInt

private val ThemeMode.label: String
    get() = when (this) {
        ThemeMode.SYSTEM -> "Auto"
        ThemeMode.LIGHT -> "Light"
        ThemeMode.DARK -> "Dark"
    }

private val ThemeMode.emoji: String
    get() = when (this) {
        ThemeMode.SYSTEM -> "🌓"
        ThemeMode.LIGHT -> "☀️"
        ThemeMode.DARK -> "🌙"
    }

/**
 * Who's eating: the mode, the goal (Bulk and Cut are Mono; Burro eats),
 * body stats for the macro goals, the favourite food, and app settings.
 */
@Composable
fun ProfileScreen(
    state: AppState,
    onProfile: ((Profile) -> Profile) -> Unit,
    onGoal: (Goal) -> Unit,
    onModeBadge: () -> Unit,
    onTheme: (ThemeMode) -> Unit,
    onRedoQuiz: () -> Unit,
) {
    val cs = MaterialTheme.colorScheme
    val profile = state.profile
    val mode = state.mode ?: Mode.BURRO
    val goal = state.goal

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = ScreenPadding, vertical = 8.dp),
    ) {
        ModeCard(mode, onModeBadge)

        SectionHeader(
            "Goal",
            subtitle = if (mode == Mode.MONO) "Sets your daily macros." else "Burros eat. Bulk and Cut come with Mono.",
        )
        ChoiceRow(
            options = Goal.entries,
            selected = goal,
            onPick = onGoal,
            title = { it.label },
            leading = { if (mode.can(it)) it.emoji else "🔒" },
            subtitle = { if (mode.can(it)) it.blurb.substringBefore(',').substringBefore('.') else "Mono" },
            height = 92.dp,
        )

        SectionHeader("About you", subtitle = "Used to suggest your daily macros. Stays on this phone.")
        MenuCard(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    NumberField(
                        profile.age, { v -> onProfile { it.copy(age = v?.roundToInt()?.takeIf { a -> a in 10..110 }) } },
                        "Age", Modifier.weight(1f), suffix = "y", maxLength = 3,
                    )
                    NumberField(
                        profile.weightKg, { v -> onProfile { it.copy(weightKg = v?.takeIf { w -> w in 25.0..400.0 }) } },
                        "Weight", Modifier.weight(1f), suffix = "kg", decimals = true,
                    )
                    NumberField(
                        profile.heightCm, { v -> onProfile { it.copy(heightCm = v?.roundToInt()?.takeIf { h -> h in 100..250 }) } },
                        "Height", Modifier.weight(1f), suffix = "cm", maxLength = 3,
                    )
                }
                Text("Sex", style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(top = 18.dp, bottom = 8.dp))
                ChoiceRow(
                    options = Sex.entries,
                    selected = profile.sex,
                    onPick = { s -> onProfile { it.copy(sex = s) } },
                    title = { it.label },
                )
            }
        }

        SectionHeader("Activity")
        MenuCard(Modifier.fillMaxWidth()) {
            Column {
                Activity.entries.forEachIndexed { i, a ->
                    if (i > 0) HorizontalDivider(Modifier.padding(start = 56.dp), color = cs.outlineVariant)
                    Surface(
                        onClick = { onProfile { it.copy(activity = a) } },
                        color = Color.Transparent,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Row(Modifier.padding(horizontal = 4.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(selected = profile.activity == a, onClick = { onProfile { it.copy(activity = a) } })
                            Column(Modifier.padding(start = 4.dp)) {
                                Text(a.label, style = MaterialTheme.typography.titleSmall)
                                Text(a.blurb, style = MaterialTheme.typography.bodySmall, color = cs.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        }

        SectionHeader("Favourite food")
        OutlinedTextField(
            value = profile.favoriteFood,
            onValueChange = { v -> onProfile { it.copy(favoriteFood = v.take(60)) } },
            placeholder = { Text("Burritos, obviously") },
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth(),
        )
        FavoriteFillings(state)

        SectionHeader("App")
        MenuCard(Modifier.fillMaxWidth()) {
            Column {
                Column(Modifier.padding(16.dp)) {
                    Text("Appearance", style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(bottom = 10.dp))
                    ChoiceRow(
                        options = ThemeMode.entries,
                        selected = state.themeMode,
                        onPick = onTheme,
                        title = { it.label },
                        leading = { it.emoji },
                    )
                }
                HorizontalDivider(color = cs.outlineVariant)
                Surface(onClick = onRedoQuiz, color = Color.Transparent, modifier = Modifier.fillMaxWidth()) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.Refresh, contentDescription = null, tint = cs.primary)
                        Column(Modifier.weight(1f).padding(start = 14.dp)) {
                            Text("Redo the taste quiz", style = MaterialTheme.typography.titleSmall)
                            Text(
                                "Swipe again. Burritos and shopping list stay.",
                                style = MaterialTheme.typography.bodySmall,
                                color = cs.onSurfaceVariant,
                            )
                        }
                        Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, contentDescription = null, tint = cs.onSurfaceVariant)
                    }
                }
            }
        }
        Spacer(Modifier.height(24.dp))
    }
}

/** Mono: a gold card with the perks unlocked. Burro: the invitation to upgrade. */
@Composable
private fun ModeCard(mode: Mode, onClick: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    val mono = mode == Mode.MONO
    val start = if (mono) cs.tertiary else cs.primary
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(28.dp),
        color = start,
        contentColor = Color.White,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Box(Modifier.background(Brush.linearGradient(listOf(start, lerp(start, Color(0xFF2B1D14), 0.35f))))) {
            Row(Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(mode.displayEmoji, fontSize = 48.sp)
                Column(Modifier.weight(1f).padding(start = 16.dp)) {
                    Pill(
                        if (mono) "ACTIVE" else "FREE PLAN",
                        color = Color.White.copy(alpha = 0.2f),
                        contentColor = Color.White,
                    )
                    Text(
                        if (mono) "You're Mono" else "You're a Burro",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(top = 6.dp),
                    )
                    Text(
                        if (mono) "AI chef, macro coach, Bulk & Cut. Manage your subscription." else "Go Mono for the AI chef, Bulk & Cut and more.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.88f),
                    )
                }
                Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, contentDescription = if (mono) "Manage Mono" else "Go Mono")
            }
        }
    }
}

/** Favourites from the swipe deck, as chips. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FavoriteFillings(state: AppState) {
    val favorites = state.prefs.favorites.mapNotNull(::ingredientOrNull)
    if (favorites.isEmpty()) return
    Text("⭐ Favourite fillings", style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(top = 16.dp, bottom = 8.dp))
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        favorites.forEach { ing ->
            Surface(shape = RoundedCornerShape(50), color = MaterialTheme.colorScheme.secondaryContainer) {
                Text(
                    "${ing.emoji} ${ing.name}",
                    style = MaterialTheme.typography.labelLarge,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                )
            }
        }
    }
}
