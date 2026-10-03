package com.cmatuteortega.monoburro.ui.screens

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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cmatuteortega.monoburro.data.ingredientOrNull
import com.cmatuteortega.monoburro.logic.amountLabel
import com.cmatuteortega.monoburro.model.Category
import com.cmatuteortega.monoburro.model.ShoppingItem
import com.cmatuteortega.monoburro.ui.MenuCard

/** Aisle order for the list: tortillas first (you buy them, not cook them), typed-in items last. */
private fun ShoppingItem.aisle(): Pair<Int, String> {
    val category = ingredientId?.let(::ingredientOrNull)?.category
    return when (category) {
        null -> 99 to "🛒 Other"
        Category.TORTILLA -> -1 to "🌯 Tortillas"
        else -> Category.FILLINGS.indexOf(category) to "${category.emoji} ${category.label}"
    }
}

@Composable
fun ShoppingScreen(
    items: List<ShoppingItem>,
    onToggle: (String) -> Unit,
    onRemove: (String) -> Unit,
    onAdd: (String) -> Unit,
    onClearChecked: () -> Unit,
    onClearAll: () -> Unit,
    onGoToBurritos: () -> Unit,
) {
    var confirmClear by remember { mutableStateOf(false) }
    val toBuy = items.filterNot { it.checked }
    val bought = items.filter { it.checked }
    val aisles = toBuy.groupBy { it.aisle() }.toSortedMap(compareBy { it.first })

    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item {
            Text("Shopping list", style = MaterialTheme.typography.headlineSmall)
            Text(
                when {
                    items.isEmpty() -> "Empty for now."
                    toBuy.isEmpty() -> "All in the basket 🎉"
                    else -> "${toBuy.size} to buy · weights are cooked / ready to use"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 2.dp, bottom = 8.dp),
            )
            AddRow(onAdd)
        }
        if (items.isEmpty()) {
            item {
                Column(Modifier.fillMaxWidth().padding(top = 36.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🛒", fontSize = 64.sp)
                    Text(
                        "Open a burrito and tap 🛒 on an ingredient, or add the whole batch.",
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 8.dp, bottom = 16.dp),
                    )
                    Button(onClick = onGoToBurritos) { Text("Go to my burritos") }
                }
            }
        }
        aisles.forEach { (aisle, lines) ->
            item(key = "aisle:${aisle.second}") {
                Text(aisle.second, style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 12.dp))
            }
            items(lines, key = { it.id }) { ShoppingRow(it, onToggle, onRemove) }
        }
        if (bought.isNotEmpty()) {
            item(key = "bought") {
                Row(Modifier.fillMaxWidth().padding(top = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("✅ In the basket", style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                    TextButton(onClick = onClearChecked) { Text("Clear ticked") }
                }
            }
            items(bought, key = { it.id }) { ShoppingRow(it, onToggle, onRemove) }
        }
        if (items.isNotEmpty()) {
            item {
                TextButton(onClick = { confirmClear = true }, modifier = Modifier.padding(top = 12.dp)) { Text("Clear the whole list") }
            }
        }
    }

    if (confirmClear) {
        AlertDialog(
            onDismissRequest = { confirmClear = false },
            title = { Text("Clear the shopping list?") },
            text = { Text("Every item goes, ticked or not.") },
            confirmButton = { TextButton(onClick = { confirmClear = false; onClearAll() }) { Text("Clear") } },
            dismissButton = { TextButton(onClick = { confirmClear = false }) { Text("Cancel") } },
        )
    }
}

@Composable
private fun AddRow(onAdd: (String) -> Unit) {
    var text by remember { mutableStateOf("") }
    val submit = {
        onAdd(text)
        text = ""
    }
    Row(verticalAlignment = Alignment.CenterVertically) {
        OutlinedTextField(
            value = text,
            onValueChange = { text = it.take(60) },
            placeholder = { Text("Add something (limes, foil…)") },
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { submit() }),
            modifier = Modifier.weight(1f),
        )
        FilledTonalButton(onClick = submit, enabled = text.isNotBlank(), modifier = Modifier.padding(start = 8.dp)) { Text("Add") }
    }
}

@Composable
private fun ShoppingRow(item: ShoppingItem, onToggle: (String) -> Unit, onRemove: (String) -> Unit) {
    val cs = MaterialTheme.colorScheme
    MenuCard(onClick = { onToggle(item.id) }, modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.padding(start = 4.dp, end = 4.dp, top = 6.dp, bottom = 6.dp), verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = item.checked, onCheckedChange = { onToggle(item.id) })
            Text(item.emoji, fontSize = 22.sp, modifier = Modifier.width(34.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    item.name,
                    style = MaterialTheme.typography.titleSmall,
                    textDecoration = if (item.checked) TextDecoration.LineThrough else null,
                    color = if (item.checked) cs.onSurfaceVariant else cs.onSurface,
                )
                item.amountLabel()?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = cs.secondary) }
                if (item.sources.isNotEmpty()) {
                    Text(
                        "For " + item.sources.joinToString(", "),
                        style = MaterialTheme.typography.labelSmall,
                        color = cs.onSurfaceVariant,
                        maxLines = 1,
                    )
                }
            }
            IconButton(onClick = { onRemove(item.id) }) { Icon(Icons.Default.Close, contentDescription = "Remove ${item.name}") }
        }
    }
}
