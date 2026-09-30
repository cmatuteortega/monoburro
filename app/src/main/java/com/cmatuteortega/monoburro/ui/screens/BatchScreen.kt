package com.cmatuteortega.monoburro.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cmatuteortega.monoburro.logic.formatGrams
import com.cmatuteortega.monoburro.logic.scaleBatch
import com.cmatuteortega.monoburro.model.Category
import com.cmatuteortega.monoburro.model.Proposal
import com.cmatuteortega.monoburro.model.UserPrefs
import com.cmatuteortega.monoburro.ui.BottomAction
import com.cmatuteortega.monoburro.ui.MacroRow

@Composable
fun BatchScreen(
    proposal: Proposal?,
    prefs: UserPrefs,
    onNoProposal: () -> Unit,
    onNext: () -> Unit,
) {
    if (proposal == null) {
        LaunchedEffect(Unit) { onNoProposal() }
        return
    }
    val count = prefs.burritoCount
    val lines = remember(proposal, count) {
        // Tortillas first: they're what you buy, not what you cook.
        scaleBatch(proposal.items, count).sortedBy { if (it.ingredient.category == Category.TORTILLA) -1 else 0 }
    }
    val fillingTotal = lines.filter { it.ingredient.category != Category.TORTILLA }.sumOf { it.totalGrams }
    val cs = MaterialTheme.colorScheme

    Column(Modifier.fillMaxSize()) {
        LazyColumn(
            Modifier.weight(1f),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            item {
                Text("Your batch", style = MaterialTheme.typography.labelLarge, color = cs.primary)
                Text("${proposal.name} × $count", style = MaterialTheme.typography.headlineMedium)
                Text(
                    "${prefs.tortilla.label} ${prefs.tortilla.inches}\" tortillas · ${formatGrams(fillingTotal)} of filling in total",
                    style = MaterialTheme.typography.bodyMedium,
                    color = cs.onSurfaceVariant,
                    modifier = Modifier.padding(top = 2.dp, bottom = 14.dp),
                )
                Text("Per burrito", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = 8.dp))
                MacroRow(proposal.macrosPerBurrito)
                Text(
                    "Shopping & cooking list",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(top = 22.dp),
                )
                Text(
                    "Weights are cooked / ready to use.",
                    style = MaterialTheme.typography.bodySmall,
                    color = cs.onSurfaceVariant,
                )
            }
            items(lines, key = { it.ingredient.id }) { line ->
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = cs.surface,
                    border = BorderStroke(1.dp, cs.outlineVariant),
                ) {
                    Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(line.ingredient.emoji, fontSize = 30.sp, modifier = Modifier.width(48.dp), textAlign = TextAlign.Center)
                        Column(Modifier.weight(1f).padding(start = 6.dp)) {
                            Text(line.ingredient.name, style = MaterialTheme.typography.titleMedium)
                            Text(
                                "${line.gramsPerBurrito} g per burrito",
                                style = MaterialTheme.typography.bodySmall,
                                color = cs.onSurfaceVariant,
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(formatGrams(line.totalGrams), fontSize = 20.sp, fontWeight = FontWeight.Black)
                            line.friendly?.let {
                                Text(it, style = MaterialTheme.typography.bodySmall, color = cs.secondary, textAlign = TextAlign.End)
                            }
                        }
                    }
                }
            }
        }
        BottomAction("Next: after cooking →", onNext)
    }
}
