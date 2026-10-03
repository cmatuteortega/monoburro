package com.cmatuteortega.monoburro.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cmatuteortega.monoburro.data.INGREDIENTS
import com.cmatuteortega.monoburro.data.TORTILLA
import com.cmatuteortega.monoburro.data.ingredient
import com.cmatuteortega.monoburro.logic.BatchLine
import com.cmatuteortega.monoburro.logic.MAX_ITEM_GRAMS
import com.cmatuteortega.monoburro.logic.categoryGrams
import com.cmatuteortega.monoburro.logic.fillings
import com.cmatuteortega.monoburro.logic.formatGrams
import com.cmatuteortega.monoburro.logic.macrosPerBurrito
import com.cmatuteortega.monoburro.logic.ratios
import com.cmatuteortega.monoburro.logic.rebalance
import com.cmatuteortega.monoburro.logic.scaleBatch
import com.cmatuteortega.monoburro.model.Burrito
import com.cmatuteortega.monoburro.model.Category
import com.cmatuteortega.monoburro.model.MacroGoals
import com.cmatuteortega.monoburro.model.Ratios
import com.cmatuteortega.monoburro.model.TortillaSize
import com.cmatuteortega.monoburro.model.UserPrefs
import com.cmatuteortega.monoburro.ui.AppViewModel
import com.cmatuteortega.monoburro.ui.BottomAction
import com.cmatuteortega.monoburro.ui.MacroRow
import com.cmatuteortega.monoburro.ui.MenuCard
import com.cmatuteortega.monoburro.ui.SectionHeader
import com.cmatuteortega.monoburro.ui.Stepper
import com.cmatuteortega.monoburro.ui.theme.LocalDarkTheme
import com.cmatuteortega.monoburro.ui.theme.color
import kotlin.math.roundToInt

/** Everything the burrito screen can change. Implemented by [AppViewModel]. */
interface BurritoEditor {
    fun setBurritoEmoji(id: String, emoji: String)
    fun renameBurrito(id: String, name: String)
    fun duplicateBurrito(id: String)
    fun deleteBurrito(id: String)
    fun setBurritoCount(id: String, n: Int)
    fun setBurritoTortilla(id: String, size: TortillaSize)
    fun setBurritoRatios(id: String, ratios: Ratios)
    fun setItemGrams(id: String, ingredientId: String, grams: Int)
    fun removeItem(id: String, ingredientId: String)
    fun addItem(id: String, ingredientId: String)
    fun addToShopping(burritoId: String, ingredientId: String? = null)
}

private val BURRITO_EMOJIS = listOf("🌯", "💪", "🥦", "🔥", "🌶️", "🫓", "🥑", "🧀", "⭐", "🐒", "🫏", "🌮")

/** Grams added or removed per tap on an ingredient. */
private const val GRAM_STEP = 5

/**
 * One burrito: what's inside (per burrito and for the batch), how many, the
 * tortilla, the filling proportions, and adding it to the shopping list.
 */
@Composable
fun BurritoDetailScreen(
    burrito: Burrito,
    prefs: UserPrefs,
    dailyGoals: MacroGoals,
    burritosPerDay: Int,
    vm: BurritoEditor,
) {
    val cs = MaterialTheme.colorScheme
    val id = burrito.id
    val lines = remember(burrito.items, burrito.count) { scaleBatch(burrito.items, burrito.count) }
    val macros = burrito.macrosPerBurrito()
    var picker by remember { mutableStateOf(false) }
    var renaming by remember { mutableStateOf(false) }
    var deleting by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize()) {
        LazyColumn(
            Modifier.weight(1f),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            item {
                Header(
                    burrito = burrito,
                    onEmoji = { vm.setBurritoEmoji(id, it) },
                    onRename = { renaming = true },
                    onDuplicate = { vm.duplicateBurrito(id) },
                    onDelete = { deleting = true },
                )
                Text("Per burrito", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 16.dp, bottom = 8.dp))
                MacroRow(macros)
                val share = (macros.kcal * 100 / dailyGoals.kcal).roundToInt()
                Text(
                    "Batch of ${burrito.count}: ${(macros.kcal * burrito.count).roundToInt()} kcal · " +
                        "${(macros.protein * burrito.count).roundToInt()} g protein\n" +
                        "One burrito is $share% of your daily ${dailyGoals.kcal} kcal ($burritosPerDay a day = ${share * burritosPerDay}%).",
                    style = MaterialTheme.typography.bodySmall,
                    color = cs.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp),
                )

                SectionHeader("Batch")
                MenuCard(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("How many", style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                            Stepper(
                                value = "${burrito.count}",
                                onMinus = { vm.setBurritoCount(id, burrito.count - 1) },
                                onPlus = { vm.setBurritoCount(id, burrito.count + 1) },
                                minusEnabled = burrito.count > 1,
                                plusEnabled = burrito.count < AppViewModel.MAX_BURRITOS,
                            )
                        }
                        Text("Tortilla", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 14.dp, bottom = 8.dp))
                        TortillaChooser(burrito.tortilla) { vm.setBurritoTortilla(id, it) }
                        Text(
                            "Changing size scales the filling to fit.",
                            style = MaterialTheme.typography.bodySmall,
                            color = cs.onSurfaceVariant,
                            modifier = Modifier.padding(top = 6.dp),
                        )
                    }
                }

                ProportionSection(burrito) { vm.setBurritoRatios(id, it) }

                SectionHeader("What's inside", subtitle = "Per burrito · whole batch. Weights are cooked / ready to use.")
            }
            items(lines, key = { it.ingredient.id }) { line ->
                IngredientRow(
                    line = line,
                    onMinus = { vm.setItemGrams(id, line.ingredient.id, line.gramsPerBurrito - GRAM_STEP) },
                    onPlus = { vm.setItemGrams(id, line.ingredient.id, line.gramsPerBurrito + GRAM_STEP) },
                    onShop = { vm.addToShopping(id, line.ingredient.id) },
                    onRemove = { vm.removeItem(id, line.ingredient.id) },
                )
            }
            item {
                OutlinedButton(
                    onClick = { picker = true },
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                ) { Text("➕  Add an ingredient") }
            }
        }
        BottomAction("Add all to shopping list 🛒", onClick = { vm.addToShopping(id) })
    }

    if (picker) {
        IngredientPicker(
            burrito = burrito,
            prefs = prefs,
            onPick = { vm.addItem(id, it); picker = false },
            onDismiss = { picker = false },
        )
    }
    if (renaming) {
        RenameDialog(burrito.name, onDone = { vm.renameBurrito(id, it); renaming = false }, onDismiss = { renaming = false })
    }
    if (deleting) {
        AlertDialog(
            onDismissRequest = { deleting = false },
            title = { Text("Delete ${burrito.name}?") },
            text = { Text("It's removed from your burritos. Your shopping list stays as it is.") },
            confirmButton = { TextButton(onClick = { deleting = false; vm.deleteBurrito(id) }) { Text("Delete") } },
            dismissButton = { TextButton(onClick = { deleting = false }) { Text("Cancel") } },
        )
    }
}

@Composable
private fun Header(
    burrito: Burrito,
    onEmoji: (String) -> Unit,
    onRename: () -> Unit,
    onDuplicate: () -> Unit,
    onDelete: () -> Unit,
) {
    var emojis by remember { mutableStateOf(false) }
    var menu by remember { mutableStateOf(false) }
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box {
            Surface(
                onClick = { emojis = true },
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceContainer,
                modifier = Modifier.size(64.dp),
            ) {
                Box(contentAlignment = Alignment.Center) { Text(burrito.emoji, fontSize = 34.sp) }
            }
            DropdownMenu(expanded = emojis, onDismissRequest = { emojis = false }) {
                BURRITO_EMOJIS.chunked(4).forEach { row ->
                    Row {
                        row.forEach { e ->
                            TextButton(onClick = { emojis = false; onEmoji(e) }) { Text(e, fontSize = 24.sp) }
                        }
                    }
                }
            }
        }
        Text(
            burrito.name,
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.weight(1f).padding(start = 14.dp),
        )
        IconButton(onClick = onRename) { Icon(Icons.Default.Edit, contentDescription = "Rename") }
        Box {
            IconButton(onClick = { menu = true }) { Icon(Icons.Default.MoreVert, contentDescription = "More") }
            DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
                DropdownMenuItem(text = { Text("Duplicate") }, leadingIcon = { Text("📄") }, onClick = { menu = false; onDuplicate() })
                DropdownMenuItem(text = { Text("Delete") }, leadingIcon = { Text("🗑️") }, onClick = { menu = false; onDelete() })
            }
        }
    }
}

@Composable
private fun TortillaChooser(selected: TortillaSize, onPick: (TortillaSize) -> Unit) {
    val cs = MaterialTheme.colorScheme
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        TortillaSize.entries.forEach { size ->
            val on = size == selected
            Surface(
                onClick = { onPick(size) },
                selected = on,
                modifier = Modifier.weight(1f).height(64.dp),
                shape = RoundedCornerShape(16.dp),
                color = if (on) cs.primaryContainer else cs.surfaceContainer,
                border = if (on) BorderStroke(2.dp, cs.primary) else null,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                    Text(size.label, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    Text("${size.inches}\"", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black)
                }
            }
        }
    }
}

/**
 * The filling split, as in the onboarding: moving one slider rebalances the
 * others. Applied on release, keeping the total filling weight.
 */
@Composable
private fun ProportionSection(burrito: Burrito, onRatios: (Ratios) -> Unit) {
    val present = Category.FILLINGS.filter { c -> burrito.fillings().any { ingredient(it.ingredientId).category == c } }
    if (present.size < 2) return
    val dark = LocalDarkTheme.current
    var draft by remember(burrito.items) { mutableStateOf(burrito.ratios()) }
    val fillingGrams = burrito.categoryGrams().values.sum()

    SectionHeader("Proportions", subtitle = "Share of ${fillingGrams} g of filling. The total stays the same.")
    MenuCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .height(18.dp)
                    .clip(RoundedCornerShape(9.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
            ) {
                present.forEach { c ->
                    if (draft[c] > 0) Box(Modifier.weight(draft[c].toFloat()).fillMaxHeight().background(c.color(dark)))
                }
            }
            present.forEach { c ->
                Row(Modifier.fillMaxWidth().padding(top = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(c.emoji, fontSize = 20.sp)
                    Text(c.label, style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(start = 8.dp).weight(1f))
                    Text(
                        "≈ ${(fillingGrams * draft[c] / 100.0).roundToInt()} g",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        "${draft[c]}%",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.ExtraBold,
                        textAlign = TextAlign.End,
                        modifier = Modifier.width(52.dp),
                    )
                }
                Slider(
                    value = draft[c].toFloat(),
                    onValueChange = { draft = rebalance(draft, c, it.roundToInt()) },
                    onValueChangeFinished = { onRatios(draft) },
                    valueRange = 0f..100f,
                    colors = SliderDefaults.colors(
                        thumbColor = c.color(dark),
                        activeTrackColor = c.color(dark),
                        inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant,
                    ),
                    modifier = Modifier.semantics { contentDescription = "${c.label} percent" },
                )
            }
        }
    }
}

@Composable
private fun IngredientRow(
    line: BatchLine,
    onMinus: () -> Unit,
    onPlus: () -> Unit,
    onShop: () -> Unit,
    onRemove: () -> Unit,
) {
    val cs = MaterialTheme.colorScheme
    val tortilla = line.ingredient.id == TORTILLA.id
    MenuCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(start = 12.dp, end = 4.dp, top = 10.dp, bottom = 10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(line.ingredient.emoji, fontSize = 28.sp, modifier = Modifier.width(40.dp), textAlign = TextAlign.Center)
                Column(Modifier.weight(1f).padding(start = 6.dp)) {
                    Text(line.ingredient.name, style = MaterialTheme.typography.titleMedium)
                    Text(
                        listOfNotNull(formatGrams(line.totalGrams), line.friendly).joinToString(" · "),
                        style = MaterialTheme.typography.bodySmall,
                        color = cs.secondary,
                    )
                }
                IconButton(onClick = onShop) { Text("🛒", fontSize = 18.sp) }
                if (!tortilla) {
                    IconButton(onClick = onRemove) { Icon(Icons.Default.Close, contentDescription = "Remove ${line.ingredient.name}") }
                }
            }
            Row(Modifier.padding(start = 46.dp, top = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                if (tortilla) {
                    Text("${line.gramsPerBurrito} g each", style = MaterialTheme.typography.bodyMedium, color = cs.onSurfaceVariant)
                } else {
                    FilledTonalIconButton(onClick = onMinus, enabled = line.gramsPerBurrito > 0, modifier = Modifier.size(32.dp)) {
                        Text("−", fontWeight = FontWeight.Bold)
                    }
                    Text(
                        "${line.gramsPerBurrito} g",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.ExtraBold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.width(64.dp),
                    )
                    FilledTonalIconButton(onClick = onPlus, enabled = line.gramsPerBurrito < MAX_ITEM_GRAMS, modifier = Modifier.size(32.dp)) {
                        Text("+", fontWeight = FontWeight.Bold)
                    }
                    Text(" per burrito", style = MaterialTheme.typography.bodySmall, color = cs.onSurfaceVariant)
                }
            }
        }
    }
}

/** Every filling not already in the burrito, by category; the user's favourites and likes first. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun IngredientPicker(burrito: Burrito, prefs: UserPrefs, onPick: (String) -> Unit, onDismiss: () -> Unit) {
    val inside = burrito.items.map { it.ingredientId }.toSet()
    val groups = Category.FILLINGS.associateWith { c ->
        INGREDIENTS.filter { it.category == c && it.id !in inside }
            .sortedByDescending { if (prefs.isFavorite(it.id)) 2 else if (prefs.likes(it.id)) 1 else 0 }
    }.filterValues { it.isNotEmpty() }
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)) {
        LazyColumn(
            Modifier.navigationBarsPadding(),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            item { Text("Add an ingredient", style = MaterialTheme.typography.headlineSmall) }
            groups.forEach { (c, list) ->
                item(key = c.name) {
                    Text(
                        "${c.emoji} ${c.label}",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(top = 14.dp, bottom = 2.dp),
                    )
                }
                items(list, key = { it.id }) { ing ->
                    Surface(
                        onClick = { onPick(ing.id) },
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceContainer,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Row(Modifier.padding(horizontal = 14.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(ing.emoji, fontSize = 22.sp, modifier = Modifier.width(36.dp))
                            Text(ing.name, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                            when {
                                prefs.isFavorite(ing.id) -> Text("⭐")
                                prefs.likes(ing.id) -> Text("👍")
                                ing.id in prefs.disliked -> Text("👎")
                            }
                            Text(
                                "  ${ing.defaultGramsPerBurrito} g",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RenameDialog(current: String, onDone: (String) -> Unit, onDismiss: () -> Unit) {
    var text by remember { mutableStateOf(current) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Rename burrito") },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it.take(AppViewModel.MAX_NAME) },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
            )
        },
        confirmButton = { TextButton(onClick = { onDone(text.trim()) }, enabled = text.isNotBlank()) { Text("Save") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}
