package com.cmatuteortega.monoburro.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
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
import com.cmatuteortega.monoburro.ui.MacroRow
import com.cmatuteortega.monoburro.ui.MenuCard
import com.cmatuteortega.monoburro.ui.NumberField
import com.cmatuteortega.monoburro.ui.SectionHeader
import com.cmatuteortega.monoburro.ui.Stepper
import com.cmatuteortega.monoburro.ui.dailyGoals
import com.cmatuteortega.monoburro.ui.goal
import kotlin.math.roundToInt

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
            .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        Text("Your goals", style = MaterialTheme.typography.headlineSmall)

        MenuCard(Modifier.fillMaxWidth().padding(top = 12.dp), onClick = onEditProfile) {
            Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(goal.emoji, fontSize = 34.sp)
                Column(Modifier.weight(1f).padding(start = 12.dp)) {
                    Text("Mode: ${goal.label}", style = MaterialTheme.typography.titleMedium)
                    Text(goal.blurb, style = MaterialTheme.typography.bodySmall, color = cs.onSurfaceVariant)
                }
                Text("Change ›", style = MaterialTheme.typography.labelLarge, color = cs.primary)
            }
        }

        SectionHeader("Daily macros")
        MenuCard(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("Set my own", style = MaterialTheme.typography.titleMedium)
                        Text(
                            if (custom == null) "Off: suggested from your profile" else "On: your numbers",
                            style = MaterialTheme.typography.bodySmall,
                            color = cs.onSurfaceVariant,
                        )
                    }
                    Switch(
                        checked = custom != null,
                        onCheckedChange = { on -> onCustomGoals(if (on) daily else null) },
                    )
                }
                Spacer(Modifier.height(14.dp))
                if (custom != null) {
                    CustomGoals(custom, onCustomGoals)
                } else {
                    MacroRow(Macros(daily.kcal.toDouble(), daily.protein.toDouble(), daily.carbs.toDouble(), daily.fat.toDouble()))
                    val maintenance = tdee(state.profile)
                    Text(
                        if (suggested != null && maintenance != null) {
                            "Maintenance ≈ ${maintenance.roundToInt()} kcal/day (Mifflin–St Jeor × ${state.profile.activity.label.lowercase()} activity)" +
                                when (goal) {
                                    Goal.BULK -> ", +10% to bulk."
                                    Goal.CUT -> ", −20% to cut."
                                    Goal.EAT -> "."
                                }
                        } else {
                            "These are generic numbers. Add your age, weight and height for ones that fit you."
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = cs.onSurfaceVariant,
                        modifier = Modifier.padding(top = 10.dp),
                    )
                    if (suggested == null) {
                        TextButton(onClick = onEditProfile) { Text("Complete my profile →") }
                    }
                }
            }
        }

        SectionHeader("Burritos a day", subtitle = "Your daily goal split evenly between them.")
        MenuCard(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("🌯 per day", style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                    Stepper(
                        value = "${state.burritosPerDay}",
                        onMinus = { onPerDay(state.burritosPerDay - 1) },
                        onPlus = { onPerDay(state.burritosPerDay + 1) },
                        minusEnabled = state.burritosPerDay > 1,
                        plusEnabled = state.burritosPerDay < AppViewModel.MAX_PER_DAY,
                    )
                }
                val n = state.burritosPerDay.toDouble()
                Text("Budget per burrito", style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(top = 14.dp, bottom = 8.dp))
                MacroRow(Macros(daily.kcal / n, daily.protein / n, daily.carbs / n, daily.fat / n), compact = true)
                val current = state.prefs.targets.kcal
                val perBurrito = daily.kcal / state.burritosPerDay
                FilledTonalButton(
                    onClick = onUseForSuggestions,
                    enabled = current != perBurrito,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                ) {
                    Text(if (current == perBurrito) "New suggestions aim for $perBurrito kcal ✓" else "Aim new suggestions at $perBurrito kcal")
                }
            }
        }

        if (state.burritos.isNotEmpty()) {
            SectionHeader("How your burritos fit", subtitle = "${state.burritosPerDay} a day, against your daily goal.")
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                state.burritos.forEach { b ->
                    val m = b.macrosPerBurrito() * state.burritosPerDay.toDouble()
                    MenuCard(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(16.dp)) {
                            Text("${b.emoji}  ${b.name}", style = MaterialTheme.typography.titleSmall, maxLines = 1)
                            FitBar("kcal", m.kcal, daily.kcal, cs.primary)
                            FitBar("protein", m.protein, daily.protein, cs.secondary)
                            FitBar("carbs", m.carbs, daily.carbs, cs.tertiary)
                            FitBar("fat", m.fat, daily.fat, cs.outline)
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
private fun FitBar(label: String, actual: Double, goal: Int, color: Color) {
    val pct = if (goal > 0) actual / goal else 0.0
    Row(Modifier.padding(top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(label, style = MaterialTheme.typography.labelMedium, modifier = Modifier.weight(0.22f))
        LinearProgressIndicator(
            progress = { pct.toFloat().coerceIn(0f, 1f) },
            color = color,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.weight(0.58f).height(8.dp).clip(RoundedCornerShape(4.dp)),
        )
        Text(
            "${(pct * 100).roundToInt()}%",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = if (pct > 1.1) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(0.2f).padding(start = 8.dp),
        )
    }
}
