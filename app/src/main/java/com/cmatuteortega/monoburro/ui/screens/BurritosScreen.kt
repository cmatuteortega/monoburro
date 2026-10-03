package com.cmatuteortega.monoburro.ui.screens

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.Box
import com.cmatuteortega.monoburro.data.ingredient
import com.cmatuteortega.monoburro.logic.fillings
import com.cmatuteortega.monoburro.logic.macrosPerBurrito
import com.cmatuteortega.monoburro.model.Burrito
import com.cmatuteortega.monoburro.ui.MacroRow
import com.cmatuteortega.monoburro.ui.MenuCard

/** The library: every saved burrito, and the way to make a new one. */
@Composable
fun BurritosScreen(
    burritos: List<Burrito>,
    onOpen: (String) -> Unit,
    onSuggest: () -> Unit,
    onBuildOwn: () -> Unit,
) {
    var chooser by remember { mutableStateOf(false) }
    Box(Modifier.fillMaxSize()) {
        LazyColumn(
            Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Text("Your burritos", style = MaterialTheme.typography.headlineSmall)
                Text(
                    if (burritos.isEmpty()) "Nothing saved yet." else "Tap one to tune it, see what's inside or shop for it.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 2.dp, bottom = 4.dp),
                )
            }
            if (burritos.isEmpty()) {
                item {
                    Column(
                        Modifier.fillMaxWidth().padding(top = 48.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text("🫓", fontSize = 72.sp)
                        Text(
                            "Make your first burrito with the button below.",
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 8.dp),
                        )
                    }
                }
            }
            items(burritos.asReversed(), key = { it.id }) { b -> BurritoCard(b, onClick = { onOpen(b.id) }) }
        }
        ExtendedFloatingActionButton(
            onClick = { chooser = true },
            icon = { Text("➕") },
            text = { Text("New burrito") },
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp),
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

@Composable
private fun BurritoCard(b: Burrito, onClick: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    MenuCard(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(b.emoji, fontSize = 34.sp)
                Column(Modifier.weight(1f).padding(start = 12.dp)) {
                    Text(b.name, style = MaterialTheme.typography.titleMedium, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    Text(
                        "× ${b.count} · ${b.tortilla.label} ${b.tortilla.inches}\" tortilla",
                        style = MaterialTheme.typography.bodySmall,
                        color = cs.onSurfaceVariant,
                    )
                }
                Text("›", style = MaterialTheme.typography.headlineSmall, color = cs.onSurfaceVariant)
            }
            val fillings = b.fillings()
            Text(
                if (fillings.isEmpty()) "Empty: add some fillings" else fillings.joinToString(" ") { ingredient(it.ingredientId).emoji },
                fontSize = 20.sp,
                maxLines = 1,
                modifier = Modifier.padding(vertical = 10.dp),
            )
            MacroRow(b.macrosPerBurrito(), compact = true)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NewBurritoSheet(onSuggest: () -> Unit, onBuildOwn: () -> Unit, onDismiss: () -> Unit) {
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)) {
        Column(
            Modifier.navigationBarsPadding().padding(horizontal = 20.dp).padding(bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("New burrito", style = MaterialTheme.typography.headlineSmall)
            Option("✨", "From your tastes", "Three proposals built from your swipes and ratios.", onSuggest)
            Option("🧑‍🍳", "Build your own", "Start with an empty tortilla and add fillings yourself.", onBuildOwn)
        }
    }
}

@Composable
private fun Option(emoji: String, title: String, text: String, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(emoji, fontSize = 30.sp)
            Column(Modifier.padding(start = 14.dp)) {
                Text(title, style = MaterialTheme.typography.titleMedium)
                Text(text, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
