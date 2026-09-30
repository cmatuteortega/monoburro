package com.cmatuteortega.monoburro.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cmatuteortega.monoburro.data.TORTILLA
import com.cmatuteortega.monoburro.data.ingredient
import com.cmatuteortega.monoburro.logic.generateProposals
import com.cmatuteortega.monoburro.model.Proposal
import com.cmatuteortega.monoburro.model.ProposalKind
import com.cmatuteortega.monoburro.model.UserPrefs
import com.cmatuteortega.monoburro.ui.BottomAction
import com.cmatuteortega.monoburro.ui.MacroRow
import kotlin.math.roundToInt

private fun ProposalKind.emoji() = when (this) {
    ProposalKind.CLASSIC -> "🌯"
    ProposalKind.HIGH_PROTEIN -> "💪"
    ProposalKind.VEGGIE_FORWARD -> "🥦"
}

@Composable
fun ProposalsScreen(
    prefs: UserPrefs,
    chosenId: String?,
    onChoose: (Proposal) -> Unit,
    onBackToSwipe: () -> Unit,
) {
    val proposals = remember(prefs) { generateProposals(prefs) }
    // Only the tortilla means nothing liked survived: send the user back to the deck.
    if (proposals.all { p -> p.items.all { it.ingredientId == TORTILLA.id } }) {
        Column(
            Modifier.fillMaxSize().padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text("🫓", fontSize = 72.sp)
            Text(
                "Nothing to fill it with yet",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(top = 12.dp),
            )
            Text(
                "Like a few fillings first and we'll build burritos from them.",
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp, bottom = 24.dp),
            )
            Button(onClick = onBackToSwipe) { Text("Back to swiping") }
        }
        return
    }

    var selected by remember(proposals) {
        mutableStateOf(proposals.firstOrNull { it.id == chosenId }?.id ?: proposals.first().id)
    }
    Column(Modifier.fillMaxSize()) {
        LazyColumn(
            Modifier.weight(1f),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item {
                Text("Three burritos, your way", style = MaterialTheme.typography.headlineSmall)
                Text(
                    "Built from what you liked. Tap one to pick it.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
            items(proposals, key = { it.id }) { p ->
                ProposalCard(p, prefs, selected = p.id == selected, onClick = { selected = p.id })
            }
        }
        val pick = proposals.first { it.id == selected }
        BottomAction("Batch-cook ${pick.name.lowercase()} →", onClick = { onChoose(pick) })
    }
}

@Composable
private fun ProposalCard(p: Proposal, prefs: UserPrefs, selected: Boolean, onClick: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    Surface(
        onClick = onClick,
        selected = selected,
        shape = RoundedCornerShape(26.dp),
        color = if (selected) cs.surface else cs.surfaceContainer,
        border = BorderStroke(if (selected) 2.5.dp else 1.dp, if (selected) cs.primary else cs.outlineVariant),
        shadowElevation = if (selected) 4.dp else 0.dp,
    ) {
        Column(Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(p.kind.emoji(), fontSize = 34.sp)
                Column(Modifier.weight(1f).padding(start = 12.dp)) {
                    Text(p.name, style = MaterialTheme.typography.titleLarge)
                    Text(p.tagline, style = MaterialTheme.typography.bodyMedium, color = cs.onSurfaceVariant)
                }
                RadioButton(selected = selected, onClick = onClick)
            }
            Column(Modifier.padding(vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                p.items.forEach { item ->
                    val ing = ingredient(item.ingredientId)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(ing.emoji, fontSize = 18.sp, modifier = Modifier.width(30.dp))
                        Text(ing.name, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                        Text(
                            "${item.gramsPerBurrito} g",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = cs.onSurfaceVariant,
                        )
                    }
                }
            }
            MacroRow(p.macrosPerBurrito, compact = true)
            TargetLine(p, prefs)
        }
    }
}

@Composable
private fun TargetLine(p: Proposal, prefs: UserPrefs) {
    val (actual, target, unit) = prefs.targets.kcal?.let { Triple(p.macrosPerBurrito.kcal, it, "kcal") }
        ?: prefs.targets.protein?.let { Triple(p.macrosPerBurrito.protein, it, "g protein") }
        ?: return
    val onTarget = if (unit == "kcal") kotlin.math.abs(actual - target) <= target * 0.08 else actual >= target - 1
    Text(
        (if (onTarget) "🎯 On target: " else "↔ Close as it gets: ") + "${actual.roundToInt()} / $target $unit",
        style = MaterialTheme.typography.labelLarge,
        color = if (onTarget) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(top = 10.dp),
    )
}
