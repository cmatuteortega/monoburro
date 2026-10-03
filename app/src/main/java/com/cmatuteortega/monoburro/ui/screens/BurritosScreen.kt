package com.cmatuteortega.monoburro.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.cmatuteortega.monoburro.logic.categoryGrams
import com.cmatuteortega.monoburro.logic.fillings
import com.cmatuteortega.monoburro.logic.macrosPerBurrito
import com.cmatuteortega.monoburro.model.Burrito
import com.cmatuteortega.monoburro.ui.CompositionBar
import com.cmatuteortega.monoburro.ui.EmojiTile
import com.cmatuteortega.monoburro.ui.EmptyState
import com.cmatuteortega.monoburro.ui.MacroLine
import com.cmatuteortega.monoburro.ui.MenuCard
import com.cmatuteortega.monoburro.ui.ScreenPadding
import kotlin.math.roundToInt

/** The library: every saved burrito, and the way to make a new one. */
@Composable
fun BurritosScreen(
    burritos: List<Burrito>,
    dailyKcal: Int,
    burritosPerDay: Int,
    onOpen: (String) -> Unit,
    onSuggest: () -> Unit,
    onBuildOwn: () -> Unit,
) {
    var chooser by remember { mutableStateOf(false) }
    Box(Modifier.fillMaxSize()) {
        LazyColumn(
            Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = ScreenPadding, end = ScreenPadding, top = 8.dp, bottom = 104.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (burritos.isEmpty()) {
                item {
                    EmptyState(
                        "🫓",
                        "No burritos yet",
                        "Make one from your tastes, or fill an empty tortilla yourself.",
                    )
                }
            } else {
                item { Summary(burritos, burritosPerDay) }
            }
            items(burritos.asReversed(), key = { it.id }) { b ->
                BurritoCard(b, dailyKcal, onClick = { onOpen(b.id) })
            }
        }
        ExtendedFloatingActionButton(
            onClick = { chooser = true },
            icon = { Icon(Icons.Rounded.Add, contentDescription = null) },
            text = { Text("New burrito", style = MaterialTheme.typography.labelLarge) },
            shape = RoundedCornerShape(20.dp),
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.align(Alignment.BottomEnd).padding(ScreenPadding),
        )
    }

    if (chooser) {
        NewBurritoSheet(
            onSuggest = { chooser = false; onSuggest() },
            onBuildOwn = { chooser = false; onBuildOwn() },
            onDismiss = { chooser = false },
        )
    }
}

/** The whole library at a glance: how many burritos, and how long they last. */
@Composable
private fun Summary(burritos: List<Burrito>, perDay: Int) {
    val cs = MaterialTheme.colorScheme
    val total = burritos.sumOf { it.count }
    val days = total / perDay
    Surface(shape = RoundedCornerShape(28.dp), color = cs.primary, contentColor = cs.onPrimary, modifier = Modifier.fillMaxWidth()) {
        Box(
            Modifier.background(
                Brush.linearGradient(listOf(cs.primary, lerp(cs.primary, cs.tertiary, 0.45f))),
            ),
        ) {
            Row(Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("$total", style = MaterialTheme.typography.displayMedium)
                    Text(
                        "burritos in ${burritos.size} ${if (burritos.size == 1) "recipe" else "recipes"}",
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Text(
                        if (days >= 1) "About $days ${if (days == 1) "day" else "days"} of food at $perDay a day" else "Less than a day at $perDay a day",
                        style = MaterialTheme.typography.bodyMedium,
                        color = cs.onPrimary.copy(alpha = 0.85f),
                        modifier = Modifier.padding(top = 6.dp),
                    )
                }
                Text("🌯", style = MaterialTheme.typography.displayLarge)
            }
        }
    }
}

@Composable
private fun BurritoCard(b: Burrito, dailyKcal: Int, onClick: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    val macros = b.macrosPerBurrito()
    MenuCard(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                EmojiTile(b.emoji, size = 56.dp, color = cs.primaryContainer.copy(alpha = 0.6f))
                Column(Modifier.weight(1f).padding(start = 14.dp)) {
                    Text(b.name, style = MaterialTheme.typography.titleMedium, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    Text(
                        "${b.count} × ${b.tortilla.label} ${b.tortilla.inches}\" · ${(macros.kcal * 100 / dailyKcal).roundToInt()}% of your day each",
                        style = MaterialTheme.typography.bodySmall,
                        color = cs.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 2.dp),
                    )
                }
                Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, contentDescription = null, tint = cs.onSurfaceVariant)
            }
            if (b.fillings().isEmpty()) {
                Text(
                    "Empty tortilla: tap to add fillings",
                    style = MaterialTheme.typography.bodyMedium,
                    color = cs.primary,
                    modifier = Modifier.padding(top = 14.dp),
                )
            } else {
                CompositionBar(b.categoryGrams(), Modifier.padding(top = 16.dp, bottom = 12.dp), height = 6.dp)
                MacroLine(macros)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NewBurritoSheet(onSuggest: () -> Unit, onBuildOwn: () -> Unit, onDismiss: () -> Unit) {
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)) {
        Column(
            Modifier.navigationBarsPadding().padding(horizontal = ScreenPadding).padding(bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("New burrito", style = MaterialTheme.typography.headlineSmall)
            Text(
                "Let us propose one, or start from a bare tortilla.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 4.dp),
            )
            Option("✨", "From your tastes", "Three proposals built from your swipes and ratios.", onSuggest)
            Option("🧑‍🍳", "Build your own", "Start with an empty tortilla and add fillings yourself.", onBuildOwn)
        }
    }
}

@Composable
private fun Option(emoji: String, title: String, text: String, onClick: () -> Unit) {
    MenuCard(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            EmojiTile(emoji, size = 52.dp, color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f))
            Column(Modifier.weight(1f).padding(start = 14.dp)) {
                Text(title, style = MaterialTheme.typography.titleMedium)
                Text(text, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
