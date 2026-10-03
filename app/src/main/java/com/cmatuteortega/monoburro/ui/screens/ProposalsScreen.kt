package com.cmatuteortega.monoburro.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cmatuteortega.monoburro.data.TORTILLA
import com.cmatuteortega.monoburro.data.ingredient
import com.cmatuteortega.monoburro.logic.emoji
import com.cmatuteortega.monoburro.logic.generateProposals
import com.cmatuteortega.monoburro.model.Proposal
import com.cmatuteortega.monoburro.model.UserPrefs
import com.cmatuteortega.monoburro.ui.BottomAction
import com.cmatuteortega.monoburro.ui.EmojiTile
import com.cmatuteortega.monoburro.ui.EmptyState
import com.cmatuteortega.monoburro.ui.MacroStats
import com.cmatuteortega.monoburro.ui.PrimaryButton
import com.cmatuteortega.monoburro.ui.ScreenPadding
import com.cmatuteortega.monoburro.ui.theme.LocalDarkTheme
import com.cmatuteortega.monoburro.ui.theme.color
import kotlin.math.roundToInt

@Composable
fun ProposalsScreen(
    prefs: UserPrefs,
    onChoose: (Proposal) -> Unit,
    onBackToSwipe: () -> Unit,
    actionLabel: (Proposal) -> String = { "Batch-cook ${it.name.lowercase()}" },
) {
    val proposals = remember(prefs) { generateProposals(prefs) }
    // Only the tortilla means nothing liked survived: send the user back to the deck.
    if (proposals.all { p -> p.items.all { it.ingredientId == TORTILLA.id } }) {
        Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center) {
            EmptyState("🫓", "Nothing to fill it with yet", "Like a few fillings first and we'll build burritos from them.") {
                PrimaryButton("Back to swiping", onBackToSwipe)
            }
        }
        return
    }

    var selected by remember(proposals) {
        mutableStateOf(proposals.first().id)
    }
    Column(Modifier.fillMaxSize()) {
        LazyColumn(
            Modifier.weight(1f),
            contentPadding = PaddingValues(start = ScreenPadding, end = ScreenPadding, top = 8.dp, bottom = 16.dp),
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
        BottomAction(actionLabel(pick), onClick = { onChoose(pick) })
    }
}

@Composable
private fun ProposalCard(p: Proposal, prefs: UserPrefs, selected: Boolean, onClick: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    val dark = LocalDarkTheme.current
    val border by animateColorAsState(if (selected) cs.primary else cs.outlineVariant, label = "border")
    Surface(
        onClick = onClick,
        selected = selected,
        shape = RoundedCornerShape(26.dp),
        color = cs.surface,
        border = BorderStroke(if (selected) 2.dp else 1.dp, border),
        shadowElevation = if (selected) 6.dp else 0.dp,
    ) {
        Column(Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                EmojiTile(p.kind.emoji(), size = 52.dp, color = cs.primaryContainer.copy(alpha = 0.6f))
                Column(Modifier.weight(1f).padding(start = 14.dp)) {
                    Text(p.name, style = MaterialTheme.typography.titleLarge)
                    Text(p.tagline, style = MaterialTheme.typography.bodySmall, color = cs.onSurfaceVariant, maxLines = 2)
                }
                RadioButton(selected = selected, onClick = onClick)
            }
            Column(Modifier.padding(top = 14.dp, bottom = 14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                p.items.forEach { item ->
                    val ing = ingredient(item.ingredientId)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        EmojiTile(ing.emoji, size = 28.dp, color = ing.category.color(dark).copy(alpha = 0.16f))
                        Text(
                            ing.name,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f).padding(start = 10.dp),
                        )
                        Text(
                            "${item.gramsPerBurrito} g",
                            style = MaterialTheme.typography.labelLarge,
                            fontSize = 14.sp,
                            color = cs.onSurfaceVariant,
                        )
                    }
                }
            }
            HorizontalDivider(color = cs.outlineVariant)
            MacroStats(p.macrosPerBurrito, Modifier.padding(top = 14.dp))
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
