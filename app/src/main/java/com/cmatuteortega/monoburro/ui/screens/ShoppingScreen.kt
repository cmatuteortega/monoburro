package com.cmatuteortega.monoburro.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cmatuteortega.monoburro.data.ingredientOrNull
import com.cmatuteortega.monoburro.logic.amountLabel
import com.cmatuteortega.monoburro.model.Category
import com.cmatuteortega.monoburro.model.ShoppingItem
import com.cmatuteortega.monoburro.ui.EmojiTile
import com.cmatuteortega.monoburro.ui.EmptyState
import com.cmatuteortega.monoburro.ui.MenuCard
import com.cmatuteortega.monoburro.ui.PrimaryButton
import com.cmatuteortega.monoburro.ui.ScreenPadding

/** Aisle order for the list: tortillas first (you buy them, not cook them), typed-in items last. */
private fun ShoppingItem.aisle(): Pair<Int, String> {
    val category = ingredientId?.let(::ingredientOrNull)?.category
    return when (category) {
        null -> 99 to "Other"
        Category.TORTILLA -> -1 to "Tortillas"
        else -> Category.FILLINGS.indexOf(category) to category.label
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
        contentPadding = PaddingValues(start = ScreenPadding, end = ScreenPadding, top = 8.dp, bottom = 24.dp),
    ) {
        item {
            if (items.isNotEmpty()) Progress(bought.size, items.size)
            AddRow(onAdd, Modifier.padding(top = if (items.isEmpty()) 0.dp else 16.dp))
        }
        if (items.isEmpty()) {
            item {
                EmptyState(
                    "🛒",
                    "Nothing to buy yet",
                    "Open a burrito and add the whole batch, or a single ingredient from its menu.",
                ) { PrimaryButton("Go to my burritos", onGoToBurritos) }
            }
        }
        aisles.forEach { (aisle, lines) ->
            item(key = "aisle:${aisle.second}") {
                AisleHeader(aisle.second, "${lines.size}")
                ItemGroup(lines, onToggle, onRemove)
            }
        }
        if (bought.isNotEmpty()) {
            item(key = "bought") {
                AisleHeader("In the basket", "${bought.size}") {
                    TextButton(onClick = onClearChecked) { Text("Clear") }
                }
                ItemGroup(bought, onToggle, onRemove)
            }
        }
        if (items.isNotEmpty()) {
            item {
                TextButton(
                    onClick = { confirmClear = true },
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                ) { Text("Clear the whole list", color = MaterialTheme.colorScheme.error) }
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

/** "3 of 9 in the basket" with a bar: how far along the shop is. */
@Composable
private fun Progress(done: Int, total: Int) {
    val cs = MaterialTheme.colorScheme
    MenuCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.Bottom) {
                Text("$done", style = MaterialTheme.typography.headlineMedium)
                Text(
                    " / $total in the basket",
                    style = MaterialTheme.typography.titleMedium,
                    color = cs.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 3.dp).weight(1f),
                )
                if (done == total) Text("🎉", fontSize = 24.sp)
            }
            LinearProgressIndicator(
                progress = { done / total.toFloat() },
                color = cs.secondary,
                trackColor = cs.surfaceContainerHigh,
                drawStopIndicator = {},
                gapSize = 0.dp,
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp).height(8.dp).clip(RoundedCornerShape(4.dp)),
            )
            Text(
                "Weights are cooked / ready to use.",
                style = MaterialTheme.typography.bodySmall,
                color = cs.onSurfaceVariant,
                modifier = Modifier.padding(top = 10.dp),
            )
        }
    }
}

@Composable
private fun AisleHeader(title: String, count: String, trailing: (@Composable () -> Unit)? = null) {
    Row(
        Modifier.fillMaxWidth().padding(top = 24.dp, bottom = 8.dp, start = 4.dp).height(32.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            title.uppercase(),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.2.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            "  $count",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.outline,
            modifier = Modifier.weight(1f),
        )
        trailing?.invoke()
    }
}

@Composable
private fun ItemGroup(lines: List<ShoppingItem>, onToggle: (String) -> Unit, onRemove: (String) -> Unit) {
    MenuCard(Modifier.fillMaxWidth()) {
        Column {
            lines.forEachIndexed { i, item ->
                if (i > 0) HorizontalDivider(Modifier.padding(start = 64.dp), color = MaterialTheme.colorScheme.outlineVariant)
                ShoppingRow(item, onToggle, onRemove)
            }
        }
    }
}

@Composable
private fun AddRow(onAdd: (String) -> Unit, modifier: Modifier = Modifier) {
    val cs = MaterialTheme.colorScheme
    var text by remember { mutableStateOf("") }
    val submit = {
        onAdd(text)
        text = ""
    }
    OutlinedTextField(
        value = text,
        onValueChange = { text = it.take(60) },
        placeholder = { Text("Add something (limes, foil…)") },
        singleLine = true,
        shape = RoundedCornerShape(50),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(onDone = { if (text.isNotBlank()) submit() }),
        trailingIcon = {
            FilledIconButton(onClick = submit, enabled = text.isNotBlank(), modifier = Modifier.padding(end = 6.dp)) {
                Icon(Icons.Rounded.Add, contentDescription = "Add")
            }
        },
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedContainerColor = cs.surface,
            focusedContainerColor = cs.surface,
            unfocusedBorderColor = cs.outlineVariant,
        ),
        modifier = modifier.fillMaxWidth(),
    )
}

@Composable
private fun ShoppingRow(item: ShoppingItem, onToggle: (String) -> Unit, onRemove: (String) -> Unit) {
    val cs = MaterialTheme.colorScheme
    Surface(
        onClick = { onToggle(item.id) },
        color = Color.Transparent,
        modifier = Modifier.fillMaxWidth().semantics { contentDescription = "${item.name}, ${if (item.checked) "bought" else "to buy"}" },
    ) {
        Row(Modifier.padding(start = 16.dp, end = 4.dp, top = 10.dp, bottom = 10.dp), verticalAlignment = Alignment.CenterVertically) {
            RoundCheck(item.checked)
            EmojiTile(item.emoji, size = 36.dp, modifier = Modifier.padding(start = 12.dp))
            Column(Modifier.weight(1f).padding(start = 12.dp)) {
                Text(
                    item.name,
                    style = MaterialTheme.typography.titleSmall,
                    textDecoration = if (item.checked) TextDecoration.LineThrough else null,
                    color = if (item.checked) cs.onSurfaceVariant else cs.onSurface,
                )
                val detail = listOfNotNull(
                    item.amountLabel(),
                    item.sources.takeIf { it.isNotEmpty() }?.let { "for " + it.joinToString(", ") },
                ).joinToString(" · ")
                if (detail.isNotEmpty()) {
                    Text(
                        detail,
                        style = MaterialTheme.typography.bodySmall,
                        color = cs.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            IconButton(onClick = { onRemove(item.id) }) {
                Icon(Icons.Rounded.Close, contentDescription = "Remove ${item.name}", tint = cs.outline, modifier = Modifier.size(20.dp))
            }
        }
    }
}

/** A round tick, filled green once bought. */
@Composable
private fun RoundCheck(checked: Boolean) {
    val cs = MaterialTheme.colorScheme
    val fill by animateColorAsState(if (checked) cs.secondary else Color.Transparent, label = "check")
    Surface(
        shape = CircleShape,
        color = fill,
        contentColor = cs.onSecondary,
        border = if (checked) null else BorderStroke(2.dp, cs.outline),
        modifier = Modifier.size(26.dp),
    ) {
        Box(contentAlignment = Alignment.Center) {
            if (checked) Icon(Icons.Rounded.Check, contentDescription = null, modifier = Modifier.size(18.dp))
        }
    }
}
