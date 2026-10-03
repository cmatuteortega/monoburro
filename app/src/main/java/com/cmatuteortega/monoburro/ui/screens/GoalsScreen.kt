package com.cmatuteortega.monoburro.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cmatuteortega.monoburro.logic.kcalFromMacros
import com.cmatuteortega.monoburro.logic.macrosPerBurrito
import com.cmatuteortega.monoburro.logic.suggestedGoals
import com.cmatuteortega.monoburro.logic.tdee
import com.cmatuteortega.monoburro.model.Goal
import com.cmatuteortega.monoburro.model.MacroGoals
import com.cmatuteortega.monoburro.model.Macros
import com.cmatuteortega.monoburro.storage.AppState
import com.cmatuteortega.monoburro.ui.AppViewModel
import com.cmatuteortega.monoburro.ui.EmojiTile
import com.cmatuteortega.monoburro.ui.MacroLine
import com.cmatuteortega.monoburro.ui.MacroStats
import com.cmatuteortega.monoburro.ui.MenuCard
import com.cmatuteortega.monoburro.ui.NumberField
import com.cmatuteortega.monoburro.ui.ScreenPadding
import com.cmatuteortega.monoburro.ui.SectionHeader
import com.cmatuteortega.monoburro.ui.Stepper
import com.cmatuteortega.monoburro.ui.dailyGoals
import com.cmatuteortega.monoburro.ui.goal
import com.cmatuteortega.monoburro.ui.theme.LocalDarkTheme
import com.cmatuteortega.monoburro.ui.theme.Macro
import com.cmatuteortega.monoburro.ui.theme.color
import com.cmatuteortega.monoburro.ui.value
import kotlin.math.roundToInt

private fun MacroGoals.asMacros() = Macros(kcal.toDouble(), protein.toDouble(), carbs.toDouble(), fat.toDouble())

/**
 * Daily macro goals: suggested from the profile (age, weight, height,
 * activity and Bulk / Cut / Eat) or set by hand, split across the burritos
 * eaten per day, and how each saved burrito fits.
 */
@Composable
fun GoalsScreen(
    state: AppState,
    onCustomGoals: (MacroGoals?) -> Unit,
    onPerDay: (Int) -> Unit,
    onUseForSuggestions: () -> Unit,
    onEditProfile: () -> Unit,
) {
    val cs = MaterialTheme.colorScheme
    val goal = state.goal
    val suggested = suggestedGoals(state.profile, goal)
    val daily = state.dailyGoals
    val custom = state.customGoals

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = ScreenPadding, vertical = 8.dp),
    ) {
        // The daily target, front and centre.
        MenuCard(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(18.dp)) {
                Text(
                    "EVERY DAY",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp,
                    color = cs.onSurfaceVariant,
                )
                Row(verticalAlignment = Alignment.Bottom) {
                    Text("${daily.kcal}", style = MaterialTheme.typography.displayMedium, color = cs.primary)
                    Text(" kcal", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(bottom = 8.dp))
                }
                MacroStats(
                    Macros(0.0, daily.protein.toDouble(), daily.carbs.toDouble(), daily.fat.toDouble()),
                    Modifier.padding(top = 4.dp),
                    without = Macro.KCAL,
                )
                Text(
                    when {
                        custom != null -> "Your own numbers."
                        suggested != null -> {
                            val maintenance = tdee(state.profile)?.roundToInt()
                            "Maintenance ≈ $maintenance kcal (Mifflin–St Jeor × ${state.profile.activity.label.lowercase()} activity)" +
                                when (goal) {
                                    Goal.BULK -> ", +10% to bulk."
                                    Goal.CUT -> ", −20% to cut."
                                    Goal.EAT -> "."
                                }
                        }
                        else -> "Generic numbers. Add your age, weight and height for ones that fit you."
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = cs.onSurfaceVariant,
                    modifier = Modifier.padding(top = 14.dp),
                )
                if (custom == null && suggested == null) {
                    TextButton(onClick = onEditProfile, modifier = Modifier.padding(top = 4.dp)) { Text("Complete my profile") }
                }
            }
        }

        MenuCard(Modifier.fillMaxWidth().padding(top = 12.dp), onClick = onEditProfile) {
            Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                EmojiTile(goal.emoji, size = 44.dp, color = cs.primaryContainer.copy(alpha = 0.6f))
                Column(Modifier.weight(1f).padding(start = 12.dp)) {
                    Text("Goal: ${goal.label}", style = MaterialTheme.typography.titleMedium)
                    Text(goal.blurb, style = MaterialTheme.typography.bodySmall, color = cs.onSurfaceVariant)
                }
                Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, contentDescription = "Change goal", tint = cs.onSurfaceVariant)
            }
        }

        MenuCard(Modifier.fillMaxWidth().padding(top = 12.dp)) {
            Column(Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("Set my own numbers", style = MaterialTheme.typography.titleMedium)
                        Text(
                            if (custom == null) "Off: suggested from your profile" else "On: the numbers below",
                            style = MaterialTheme.typography.bodySmall,
                            color = cs.onSurfaceVariant,
                        )
                    }
                    Switch(checked = custom != null, onCheckedChange = { on -> onCustomGoals(if (on) daily else null) })
                }
                if (custom != null) {
                    Spacer(Modifier.height(14.dp))
                    CustomGoals(custom, onCustomGoals)
                }
            }
        }

        SectionHeader("Burritos a day", subtitle = "Your day split evenly between them.")
        MenuCard(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Burritos per day", style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                    Stepper(
                        value = "${state.burritosPerDay}",
                        onMinus = { onPerDay(state.burritosPerDay - 1) },
                        onPlus = { onPerDay(state.burritosPerDay + 1) },
                        minusEnabled = state.burritosPerDay > 1,
                        plusEnabled = state.burritosPerDay < AppViewModel.MAX_PER_DAY,
                        label = "burritos per day",
                    )
                }
                val n = state.burritosPerDay.toDouble()
                Text(
                    "Each burrito gets",
                    style = MaterialTheme.typography.bodySmall,
                    color = cs.onSurfaceVariant,
                    modifier = Modifier.padding(top = 16.dp, bottom = 6.dp),
                )
                MacroLine(Macros(daily.kcal / n, daily.protein / n, daily.carbs / n, daily.fat / n))
                val current = state.prefs.targets.kcal
                val perBurrito = daily.kcal / state.burritosPerDay
                FilledTonalButton(
                    onClick = onUseForSuggestions,
                    enabled = current != perBurrito,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth().padding(top = 14.dp).height(48.dp),
                ) {
                    Text(if (current == perBurrito) "New proposals aim for $perBurrito kcal ✓" else "Aim new proposals at $perBurrito kcal")
                }
            }
        }

        if (state.burritos.isNotEmpty()) {
            SectionHeader("How your burritos fit", subtitle = "${state.burritosPerDay} a day of each, against your daily goal.")
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                state.burritos.forEach { b ->
                    val m = b.macrosPerBurrito() * state.burritosPerDay.toDouble()
                    MenuCard(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                EmojiTile(b.emoji, size = 32.dp)
                                Text(
                                    b.name,
                                    style = MaterialTheme.typography.titleSmall,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.padding(start = 10.dp),
                                )
                            }
                            Macro.entries.forEach { macro -> FitBar(macro, m.value(macro), daily.asMacros().value(macro)) }
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun CustomGoals(goals: MacroGoals, onGoals: (MacroGoals?) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        NumberField(goals.kcal, { onGoals(goals.copy(kcal = it?.roundToInt() ?: 0)) }, "Calories", Modifier.fillMaxWidth(), suffix = "kcal")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            NumberField(goals.protein, { onGoals(goals.copy(protein = it?.roundToInt() ?: 0)) }, "Protein", Modifier.weight(1f), suffix = "g", maxLength = 4)
            NumberField(goals.carbs, { onGoals(goals.copy(carbs = it?.roundToInt() ?: 0)) }, "Carbs", Modifier.weight(1f), suffix = "g", maxLength = 4)
            NumberField(goals.fat, { onGoals(goals.copy(fat = it?.roundToInt() ?: 0)) }, "Fat", Modifier.weight(1f), suffix = "g", maxLength = 4)
        }
        val fromMacros = goals.kcalFromMacros()
        val off = goals.kcal > 0 && kotlin.math.abs(fromMacros - goals.kcal) > goals.kcal * 0.1
        Text(
            "Your macros add up to $fromMacros kcal" + if (off) ": more than 10% off your calories." else ".",
            style = MaterialTheme.typography.bodySmall,
            color = if (off) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun FitBar(macro: Macro, actual: Double, goal: Double) {
    val dark = LocalDarkTheme.current
    val pct = if (goal > 0) actual / goal else 0.0
    Row(Modifier.padding(top = 10.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(
            macro.short,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(56.dp),
        )
        LinearProgressIndicator(
            progress = { pct.toFloat().coerceIn(0f, 1f) },
            color = macro.color(dark),
            trackColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            drawStopIndicator = {},
            gapSize = 0.dp,
            modifier = Modifier.weight(1f).height(8.dp).clip(RoundedCornerShape(4.dp)),
        )
        Text(
            "${(pct * 100).roundToInt()}%",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.End,
            color = if (pct > 1.1) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.width(48.dp),
        )
    }
}
