package com.cmatuteortega.monoburro.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cmatuteortega.monoburro.model.Category
import com.cmatuteortega.monoburro.model.Ratios
import com.cmatuteortega.monoburro.model.Targets
import com.cmatuteortega.monoburro.model.TortillaSize
import com.cmatuteortega.monoburro.model.UserPrefs
import com.cmatuteortega.monoburro.ui.AppViewModel
import com.cmatuteortega.monoburro.ui.BottomAction
import com.cmatuteortega.monoburro.ui.ChoiceRow
import com.cmatuteortega.monoburro.ui.CompositionBar
import com.cmatuteortega.monoburro.ui.MenuCard
import com.cmatuteortega.monoburro.ui.ScreenPadding
import com.cmatuteortega.monoburro.ui.Stepper
import com.cmatuteortega.monoburro.ui.theme.LocalDarkTheme
import com.cmatuteortega.monoburro.ui.theme.color
import kotlin.math.roundToInt

private enum class TargetMode(val label: String) { NONE("None"), KCAL("Calories"), PROTEIN("Protein") }

@Composable
fun RatioScreen(
    prefs: UserPrefs,
    onRatio: (Category, Int) -> Unit,
    onResetRatios: () -> Unit,
    onCount: (Int) -> Unit,
    onTortilla: (TortillaSize) -> Unit,
    onTargets: (Targets) -> Unit,
    onContinue: () -> Unit,
) {
    val dark = LocalDarkTheme.current
    Column(Modifier.fillMaxSize()) {
        Column(
            Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = ScreenPadding),
        ) {
            Text("How do you pack it?", style = MaterialTheme.typography.headlineSmall, modifier = Modifier.padding(top = 8.dp))
            Text(
                "Share of the filling weight. Moving one slider rebalances the rest, so it's always 100%.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp, bottom = 14.dp),
            )
            MenuCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
                    CompositionBar(Category.FILLINGS.associateWith { prefs.ratios[it] }, height = 12.dp)
                    Category.FILLINGS.forEach { c ->
                        val pct = prefs.ratios[c]
                        val grams = (prefs.tortilla.fillingBudgetGrams * pct / 100.0).roundToInt()
                        Row(Modifier.fillMaxWidth().padding(top = 14.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(c.emoji, fontSize = 18.sp)
                            Text(c.label, style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(start = 8.dp).weight(1f))
                            Text("≈ $grams g", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                "$pct%",
                                style = MaterialTheme.typography.titleMedium,
                                textAlign = TextAlign.End,
                                modifier = Modifier.width(52.dp),
                            )
                        }
                        Slider(
                            value = pct.toFloat(),
                            onValueChange = { onRatio(c, it.roundToInt()) },
                            valueRange = 0f..100f,
                            colors = SliderDefaults.colors(
                                thumbColor = c.color(dark),
                                activeTrackColor = c.color(dark),
                                inactiveTrackColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                            ),
                            modifier = Modifier.height(32.dp).semantics { contentDescription = "${c.label} percent" },
                        )
                    }
                    if (prefs.ratios != Ratios.DEFAULT) {
                        TextButton(onClick = onResetRatios) { Text("Reset to 35 / 30 / 20 / 10 / 5") }
                    }
                }
            }

            SectionTitle("How many burritos?")
            MenuCard(Modifier.fillMaxWidth()) {
                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("${prefs.burritoCount}", style = MaterialTheme.typography.displaySmall)
                        Text(
                            "burritos in one batch",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Stepper(
                        value = "",
                        onMinus = { onCount(prefs.burritoCount - 1) },
                        onPlus = { onCount(prefs.burritoCount + 1) },
                        minusEnabled = prefs.burritoCount > 1,
                        plusEnabled = prefs.burritoCount < AppViewModel.MAX_BURRITOS,
                        buttonSize = 48.dp,
                        valueWidth = 8.dp,
                        label = "burritos",
                    )
                }
            }

            SectionTitle("Tortilla size")
            ChoiceRow(
                options = TortillaSize.entries,
                selected = prefs.tortilla,
                onPick = onTortilla,
                title = { "${it.inches}\"" },
                subtitle = { "${it.label} · ${it.grams} g" },
            )

            SectionTitle("Target per burrito (optional)")
            TargetPicker(prefs.targets, onTargets)
            Spacer(Modifier.height(24.dp))
        }
        BottomAction("Show me 3 burritos", onContinue)
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(text, style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(top = 28.dp, bottom = 12.dp))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TargetPicker(targets: Targets, onTargets: (Targets) -> Unit) {
    var mode by remember {
        mutableStateOf(
            when {
                targets.kcal != null -> TargetMode.KCAL
                targets.protein != null -> TargetMode.PROTEIN
                else -> TargetMode.NONE
            },
        )
    }
    var text by remember { mutableStateOf((targets.kcal ?: targets.protein)?.toString().orEmpty()) }

    fun push(m: TargetMode, t: String) {
        val n = t.toIntOrNull()?.takeIf { it > 0 }
        onTargets(
            when (m) {
                TargetMode.NONE -> Targets()
                TargetMode.KCAL -> Targets(kcal = n)
                TargetMode.PROTEIN -> Targets(protein = n)
            },
        )
    }

    SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
        TargetMode.entries.forEachIndexed { i, m ->
            SegmentedButton(
                selected = mode == m,
                onClick = {
                    mode = m
                    if (m == TargetMode.KCAL && text.isEmpty()) text = "650"
                    if (m == TargetMode.PROTEIN && text.isEmpty()) text = "40"
                    push(m, text)
                },
                shape = SegmentedButtonDefaults.itemShape(i, TargetMode.entries.size),
            ) { Text(m.label) }
        }
    }
    if (mode != TargetMode.NONE) {
        OutlinedTextField(
            value = text,
            onValueChange = { v ->
                text = v.filter(Char::isDigit).take(4)
                push(mode, text)
            },
            label = { Text(if (mode == TargetMode.KCAL) "Calories per burrito" else "Protein per burrito") },
            suffix = { Text(if (mode == TargetMode.KCAL) "kcal" else "g") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
        )
        Text(
            if (mode == TargetMode.KCAL) {
                "Portions scale up or down (within reason) to land near this."
            } else {
                "We'll swap some carbs for protein until each burrito gets there."
            },
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 6.dp),
        )
    }
}
