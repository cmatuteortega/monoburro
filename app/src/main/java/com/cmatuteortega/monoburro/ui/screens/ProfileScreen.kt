package com.cmatuteortega.monoburro.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
import com.cmatuteortega.monoburro.ui.MenuCard
import com.cmatuteortega.monoburro.ui.NumberField
import com.cmatuteortega.monoburro.ui.SectionHeader
import com.cmatuteortega.monoburro.ui.displayEmoji
import com.cmatuteortega.monoburro.ui.goal
import kotlin.math.roundToInt

/**
 * Who's eating: the mode, the goal (Bulk and Cut are Mono; Burro eats),
 * body stats for the macro goals, and the favourite food.
 */
@Composable
fun ProfileScreen(
    state: AppState,
    onProfile: ((Profile) -> Profile) -> Unit,
    onGoal: (Goal) -> Unit,
    onModeBadge: () -> Unit,
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
            .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        Text("Your profile", style = MaterialTheme.typography.headlineSmall)

        MenuCard(Modifier.fillMaxWidth().padding(top = 12.dp), onClick = onModeBadge) {
            Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(mode.displayEmoji, fontSize = 38.sp)
                Column(Modifier.weight(1f).padding(start = 12.dp)) {
                    Text("${mode.label} mode", style = MaterialTheme.typography.titleMedium)
                    Text(mode.tagline, style = MaterialTheme.typography.bodySmall, color = cs.onSurfaceVariant)
                }
                Text(if (mode == Mode.MONO) "Manage ›" else "Go Mono ›", style = MaterialTheme.typography.labelLarge, color = cs.primary)
            }
        }

        SectionHeader(
            "Goal",
            subtitle = if (mode == Mode.MONO) "Sets your daily macros." else "Burros eat. Bulk and Cut come with Mono 🐒.",
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Goal.entries.forEach { g ->
                val selected = g == goal
                val locked = !mode.can(g)
                Surface(
                    onClick = { onGoal(g) },
                    selected = selected,
                    shape = RoundedCornerShape(20.dp),
                    color = if (selected) cs.primaryContainer else cs.surfaceContainer,
                    border = if (selected) BorderStroke(2.dp, cs.primary) else null,
                    modifier = Modifier.weight(1f).height(112.dp),
                ) {
                    Column(
                        Modifier.padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        Text(if (locked) "🔒" else g.emoji, fontSize = 28.sp)
                        Text(g.label, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(
                            if (locked) "Mono" else g.blurb.substringBefore('.'),
                            style = MaterialTheme.typography.labelSmall,
                            textAlign = TextAlign.Center,
                            color = cs.onSurfaceVariant,
                            maxLines = 2,
                        )
                    }
                }
            }
        }

        SectionHeader("About you", subtitle = "Used to suggest your daily macros. Stays on this phone.")
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
        SexPicker(profile.sex) { s -> onProfile { it.copy(sex = s) } }

        Text("Activity", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 16.dp, bottom = 4.dp))
        Activity.entries.forEach { a ->
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                RadioButton(selected = profile.activity == a, onClick = { onProfile { it.copy(activity = a) } })
                Column {
                    Text(a.label, style = MaterialTheme.typography.bodyLarge)
                    Text(a.blurb, style = MaterialTheme.typography.bodySmall, color = cs.onSurfaceVariant)
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

        OutlinedButton(
            onClick = onRedoQuiz,
            shape = RoundedCornerShape(18.dp),
            modifier = Modifier.fillMaxWidth().padding(top = 24.dp).height(52.dp),
        ) { Text("🔄  Redo the taste quiz") }
        Spacer(Modifier.height(24.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SexPicker(sex: Sex, onSex: (Sex) -> Unit) {
    Text("Sex", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 16.dp, bottom = 8.dp))
    SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
        Sex.entries.forEachIndexed { i, s ->
            SegmentedButton(
                selected = sex == s,
                onClick = { onSex(s) },
                shape = SegmentedButtonDefaults.itemShape(i, Sex.entries.size),
            ) { Text(s.label, maxLines = 1) }
        }
    }
}

/** Favourites from the swipe deck, as chips. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FavoriteFillings(state: AppState) {
    val favorites = state.prefs.favorites.mapNotNull(::ingredientOrNull)
    if (favorites.isEmpty()) return
    Text("⭐ Favourite fillings", style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(top = 14.dp, bottom = 8.dp))
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        favorites.forEach { ing ->
            Surface(shape = RoundedCornerShape(50), color = MaterialTheme.colorScheme.secondaryContainer) {
                Text("${ing.emoji} ${ing.name}", style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp))
            }
        }
    }
}
