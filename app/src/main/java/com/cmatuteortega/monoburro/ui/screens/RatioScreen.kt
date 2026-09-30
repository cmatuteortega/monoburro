package com.cmatuteortega.monoburro.ui.screens

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cmatuteortega.monoburro.model.Category
import com.cmatuteortega.monoburro.model.Ratios
import com.cmatuteortega.monoburro.model.Targets
import com.cmatuteortega.monoburro.model.TortillaSize
import com.cmatuteortega.monoburro.model.UserPrefs
import com.cmatuteortega.monoburro.ui.BottomAction
import com.cmatuteortega.monoburro.ui.OnboardingViewModel
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
                .padding(horizontal = 16.dp),
        ) {
            Text("How do you pack it?", style = MaterialTheme.typography.headlineSmall, modifier = Modifier.padding(top = 8.dp))
            Text(
                "Share of the filling weight. Moving one slider rebalances the rest, so it's always 100%.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp, bottom = 14.dp),
            )
            RatioBar(prefs.ratios, dark)

            Category.FILLINGS.forEach { c ->
                val pct = prefs.ratios[c]
                val grams = (prefs.tortilla.fillingBudgetGrams * pct / 100.0).roundToInt()
                Row(Modifier.fillMaxWidth().padding(top = 14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(c.emoji, fontSize = 22.sp)
                    Text(c.label, style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(start = 10.dp).weight(1f))
                    Text("≈ $grams g", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        "$pct%",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        textAlign = TextAlign.End,
                        modifier = Modifier.width(56.dp),
                    )
                }
                Slider(
                    value = pct.toFloat(),
                    onValueChange = { onRatio(c, it.roundToInt()) },
                    valueRange = 0f..100f,
                    colors = SliderDefaults.colors(
                        thumbColor = c.color(dark),
                        activeTrackColor = c.color(dark),
                        inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant,
                        inactiveTickColor = c.color(dark),
                    ),
                    modifier = Modifier.semantics { contentDescription = "${c.label} percent" },
                )
            }
            if (prefs.ratios != Ratios.DEFAULT) {
                TextButton(onClick = onResetRatios) { Text("Reset to 35 / 30 / 20 / 10 / 5") }
            }

            SectionTitle("How many burritos?")
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                FilledTonalIconButton(onClick = { onCount(prefs.burritoCount - 1) }, modifier = Modifier.size(56.dp)) {
                    Text("−", fontSize = 28.sp, fontWeight = FontWeight.Bold)
                }
                Text(
                    "${prefs.burritoCount}",
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.width(120.dp),
                )
                FilledTonalIconButton(
                    onClick = { onCount(prefs.burritoCount + 1) },
                    enabled = prefs.burritoCount < OnboardingViewModel.MAX_BURRITOS,
                    modifier = Modifier.size(56.dp),
                ) { Text("+", fontSize = 28.sp, fontWeight = FontWeight.Bold) }
            }

            SectionTitle("Tortilla size")
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TortillaSize.entries.forEach { size ->
                    val selected = size == prefs.tortilla
                    Surface(
                        onClick = { onTortilla(size) },
                        selected = selected,
                        modifier = Modifier.weight(1f).height(84.dp),
                        shape = RoundedCornerShape(18.dp),
                        color = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainer,
                        border = if (selected) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null,
                    ) {
                        Column(
                            Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                        ) {
                            Text(size.label, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            Text("${size.inches}\"", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
                            Text("${size.grams} g", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }

            SectionTitle("Target per burrito (optional)")
            TargetPicker(prefs.targets, onTargets)
            Spacer(Modifier.height(24.dp))
        }
        BottomAction("Show me 3 burritos →", onContinue)
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(text, style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(top = 28.dp, bottom = 12.dp))
}

@Composable
private fun RatioBar(ratios: Ratios, dark: Boolean) {
    Row(
        Modifier
            .fillMaxWidth()
            .height(22.dp)
            .clip(RoundedCornerShape(11.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Category.FILLINGS.forEach { c ->
            val pct = ratios[c]
            if (pct > 0) Box(Modifier.weight(pct.toFloat()).fillMaxHeight().background(c.color(dark)))
        }
    }
    Row(Modifier.fillMaxWidth().padding(top = 6.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Category.FILLINGS.forEach { c ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(8.dp).clip(CircleShape).background(c.color(dark)))
                Text(" ${ratios[c]}", style = MaterialTheme.typography.labelSmall)
            }
        }
    }
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
