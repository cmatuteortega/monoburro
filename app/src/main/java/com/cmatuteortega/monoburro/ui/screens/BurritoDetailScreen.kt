package com.cmatuteortega.monoburro.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddShoppingCart
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
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
import com.cmatuteortega.monoburro.ui.ChoiceRow
import com.cmatuteortega.monoburro.ui.CompositionBar
import com.cmatuteortega.monoburro.ui.EmojiTile
import com.cmatuteortega.monoburro.ui.MacroStats
import com.cmatuteortega.monoburro.ui.MenuCard
import com.cmatuteortega.monoburro.ui.Pill
import com.cmatuteortega.monoburro.ui.ScreenPadding
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
 * One burrito: its numbers up top, then what's inside (per burrito and for
 * the batch), how many and which tortilla, the filling proportions, and
 * adding it to the shopping list.
 */
@Composable
fun BurritoDetailScreen(
    burrito: Burrito,
    prefs: UserPrefs,
    dailyGoals: MacroGoals,
    burritosPerDay: Int,
    vm: BurritoEditor,
) {
    val id = burrito.id
    val lines = remember(burrito.items, burrito.count) { scaleBatch(burrito.items, burrito.count) }
    var picker by remember { mutableStateOf(false) }
    var renaming by remember { mutableStateOf(false) }
    var deleting by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize()) {
        LazyColumn(
            Modifier.weight(1f),
            contentPadding = PaddingValues(start = ScreenPadding, end = ScreenPadding, bottom = 16.dp),
        ) {
            item {
                Hero(
                    burrito = burrito,
                    dailyGoals = dailyGoals,
                    burritosPerDay = burritosPerDay,
                    onEmoji = { vm.setBurritoEmoji(id, it) },
                    onRename = { renaming = true },
                    onDuplicate = { vm.duplicateBurrito(id) },
                    onDelete = { deleting = true },
                )
                SectionHeader(
                    "What's inside",
                    subtitle = "Per burrito, and for all ${burrito.count}. Cooked / ready-to-use weights.",
                )
            }
            item {
                MenuCard(Modifier.fillMaxWidth()) {
                    Column {
                        lines.forEach { line ->
                            IngredientRow(
                                line = line,
                                onMinus = { vm.setItemGrams(id, line.ingredient.id, line.gramsPerBurrito - GRAM_STEP) },
                                onPlus = { vm.setItemGrams(id, line.ingredient.id, line.gramsPerBurrito + GRAM_STEP) },
                                onShop = { vm.addToShopping(id, line.ingredient.id) },
                                onRemove = { vm.removeItem(id, line.ingredient.id) },
                            )
                            HorizontalDivider(Modifier.padding(start = 68.dp), color = MaterialTheme.colorScheme.outlineVariant)
                        }
                        TextButton(
                            onClick = { picker = true },
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp),
                        ) {
                            Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(20.dp))
                            Text("Add an ingredient", style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(start = 8.dp))
                        }
                    }
                }
            }
            item {
                SectionHeader("Batch")
                MenuCard(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text("How many", style = MaterialTheme.typography.titleMedium)
                                Text(
                                    "Rolled in one session",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                            Stepper(
                                value = "${burrito.count}",
                                onMinus = { vm.setBurritoCount(id, burrito.count - 1) },
                                onPlus = { vm.setBurritoCount(id, burrito.count + 1) },
                                minusEnabled = burrito.count > 1,
                                plusEnabled = burrito.count < AppViewModel.MAX_BURRITOS,
                                label = "burritos",
                            )
                        }
                        Text("Tortilla", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 18.dp, bottom = 10.dp))
                        TortillaChooser(burrito.tortilla) { vm.setBurritoTortilla(id, it) }
                        Text(
                            "Changing size scales the filling to fit.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 8.dp),
                        )
                    }
                }
                ProportionSection(burrito) { vm.setBurritoRatios(id, it) }
            }
        }
        BottomAction("Add all ${burrito.count} to shopping list", onClick = { vm.addToShopping(id) })
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

/** Emoji, name and the numbers that matter: macros per burrito, share of the day, the filling split. */
@Composable
private fun Hero(
    burrito: Burrito,
    dailyGoals: MacroGoals,
    burritosPerDay: Int,
    onEmoji: (String) -> Unit,
    onRename: () -> Unit,
    onDuplicate: () -> Unit,
    onDelete: () -> Unit,
) {
    val cs = MaterialTheme.colorScheme
    val macros = burrito.macrosPerBurrito()
    var emojis by remember { mutableStateOf(false) }
    var menu by remember { mutableStateOf(false) }
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box {
            Surface(
                onClick = { emojis = true },
                shape = RoundedCornerShape(24.dp),
                color = cs.primaryContainer.copy(alpha = 0.6f),
                modifier = Modifier.size(76.dp).semantics { contentDescription = "Change emoji" },
            ) {
                Box(contentAlignment = Alignment.Center) { Text(burrito.emoji, fontSize = 40.sp) }
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
        Column(Modifier.weight(1f).padding(start = 16.dp)) {
            Text(burrito.name, style = MaterialTheme.typography.headlineSmall, maxLines = 3, overflow = TextOverflow.Ellipsis)
            Text(
                "${burrito.count} burritos · ${burrito.tortilla.label} ${burrito.tortilla.inches}\" tortilla",
                style = MaterialTheme.typography.bodyMedium,
                color = cs.onSurfaceVariant,
                modifier = Modifier.padding(top = 2.dp),
            )
        }
        Box {
            IconButton(onClick = { menu = true }) { Icon(Icons.Rounded.MoreVert, contentDescription = "More") }
            DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
                DropdownMenuItem(
                    text = { Text("Rename") },
                    leadingIcon = { Icon(Icons.Outlined.Edit, contentDescription = null) },
                    onClick = { menu = false; onRename() },
                )
                DropdownMenuItem(
                    text = { Text("Duplicate") },
                    leadingIcon = { Icon(Icons.Outlined.ContentCopy, contentDescription = null) },
                    onClick = { menu = false; onDuplicate() },
                )
                DropdownMenuItem(
                    text = { Text("Delete", color = cs.error) },
                    leadingIcon = { Icon(Icons.Outlined.Delete, contentDescription = null, tint = cs.error) },
                    onClick = { menu = false; onDelete() },
                )
            }
        }
    }

    MenuCard(Modifier.fillMaxWidth().padding(top = 20.dp)) {
        Column(Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "PER BURRITO",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp,
                    color = cs.onSurfaceVariant,
                    modifier = Modifier.weight(1f),
                )
                val share = (macros.kcal * 100 / dailyGoals.kcal).roundToInt()
                Pill("$share% of your day", color = cs.primaryContainer.copy(alpha = 0.6f), contentColor = cs.onPrimaryContainer)
            }
            MacroStats(macros, Modifier.padding(top = 12.dp), large = true)
            if (burrito.fillings().isNotEmpty()) {
                CompositionBar(burrito.categoryGrams(), Modifier.padding(top = 18.dp), height = 8.dp)
            }
            Text(
                "Whole batch: ${(macros.kcal * burrito.count).roundToInt()} kcal · ${(macros.protein * burrito.count).roundToInt()} g protein. " +
                    "At $burritosPerDay a day it lasts ${burrito.count / burritosPerDay} days.",
                style = MaterialTheme.typography.bodySmall,
                color = cs.onSurfaceVariant,
                modifier = Modifier.padding(top = 12.dp),
            )
        }
    }
}

@Composable
internal fun TortillaChooser(selected: TortillaSize, onPick: (TortillaSize) -> Unit) {
    ChoiceRow(
        options = TortillaSize.entries,
        selected = selected,
        onPick = onPick,
        title = { "${it.inches}\"" },
        subtitle = { it.label },
    )
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

    SectionHeader("Proportions", subtitle = "Share of $fillingGrams g of filling. The total stays the same.")
    MenuCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
            CompositionBar(present.associateWith { draft[it] }, height = 10.dp)
            present.forEach { c ->
                Row(Modifier.fillMaxWidth().padding(top = 14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(c.emoji, fontSize = 18.sp)
                    Text(c.label, style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(start = 8.dp).weight(1f))
                    Text(
                        "≈ ${(fillingGrams * draft[c] / 100.0).roundToInt()} g",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        "${draft[c]}%",
                        style = MaterialTheme.typography.titleMedium,
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
                        inactiveTrackColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    ),
                    modifier = Modifier.height(32.dp).semantics { contentDescription = "${c.label} percent" },
                )
            }
        }
    }
}

/** One line per ingredient: what it is, how much for the batch, grams per burrito, and a menu. */
@Composable
private fun IngredientRow(
    line: BatchLine,
    onMinus: () -> Unit,
    onPlus: () -> Unit,
    onShop: () -> Unit,
    onRemove: () -> Unit,
) {
    val cs = MaterialTheme.colorScheme
    val dark = LocalDarkTheme.current
    val tortilla = line.ingredient.id == TORTILLA.id
    var menu by remember { mutableStateOf(false) }
    Row(
        Modifier.padding(start = 14.dp, end = 4.dp, top = 12.dp, bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        EmojiTile(line.ingredient.emoji, size = 42.dp, color = line.ingredient.category.color(dark).copy(alpha = 0.16f))
        Column(Modifier.weight(1f).padding(start = 12.dp, end = 8.dp)) {
            Text(line.ingredient.name, style = MaterialTheme.typography.titleSmall, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Text(
                listOfNotNull(formatGrams(line.totalGrams), line.friendly).joinToString(" · "),
                style = MaterialTheme.typography.bodySmall,
                color = cs.onSurfaceVariant,
                maxLines = 2,
            )
        }
        if (tortilla) {
            Text(
                "${line.gramsPerBurrito} g",
                style = MaterialTheme.typography.labelLarge,
                color = cs.onSurfaceVariant,
                modifier = Modifier.padding(end = 12.dp),
            )
        } else {
            Stepper(
                value = "${line.gramsPerBurrito} g",
                onMinus = onMinus,
                onPlus = onPlus,
                minusEnabled = line.gramsPerBurrito > 0,
                plusEnabled = line.gramsPerBurrito < MAX_ITEM_GRAMS,
                buttonSize = 32.dp,
                valueWidth = 50.dp,
                label = line.ingredient.name,
            )
        }
        Box {
            IconButton(onClick = { menu = true }) {
                Icon(Icons.Rounded.MoreVert, contentDescription = "${line.ingredient.name} options", tint = cs.onSurfaceVariant)
            }
            DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
                DropdownMenuItem(
                    text = { Text("Add to shopping list") },
                    leadingIcon = { Icon(Icons.Outlined.AddShoppingCart, contentDescription = null) },
                    onClick = { menu = false; onShop() },
                )
                if (!tortilla) {
                    DropdownMenuItem(
                        text = { Text("Remove from burrito", color = cs.error) },
                        leadingIcon = { Icon(Icons.Outlined.Delete, contentDescription = null, tint = cs.error) },
                        onClick = { menu = false; onRemove() },
                    )
                }
            }
        }
    }
}

/** Every filling not already in the burrito, by category; the user's favourites and likes first. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun IngredientPicker(burrito: Burrito, prefs: UserPrefs, onPick: (String) -> Unit, onDismiss: () -> Unit) {
    val dark = LocalDarkTheme.current
    val inside = burrito.items.map { it.ingredientId }.toSet()
    val groups = Category.FILLINGS.associateWith { c ->
        INGREDIENTS.filter { it.category == c && it.id !in inside }
            .sortedByDescending { if (prefs.isFavorite(it.id)) 2 else if (prefs.likes(it.id)) 1 else 0 }
    }.filterValues { it.isNotEmpty() }
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)) {
        LazyColumn(
            Modifier.navigationBarsPadding(),
            contentPadding = PaddingValues(start = ScreenPadding, end = ScreenPadding, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            item { Text("Add an ingredient", style = MaterialTheme.typography.headlineSmall) }
            groups.forEach { (c, list) ->
                item(key = c.name) {
                    Text(
                        c.label.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 18.dp, bottom = 6.dp),
                    )
                }
                items(list, key = { it.id }) { ing ->
                    Surface(
                        onClick = { onPick(ing.id) },
                        shape = RoundedCornerShape(16.dp),
                        color = Color.Transparent,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Row(Modifier.padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                            EmojiTile(ing.emoji, size = 40.dp, color = c.color(dark).copy(alpha = 0.16f))
                            Text(ing.name, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f).padding(start = 12.dp))
                            when {
                                prefs.isFavorite(ing.id) -> Text("⭐", modifier = Modifier.padding(end = 8.dp))
                                prefs.likes(ing.id) -> Text("👍", modifier = Modifier.padding(end = 8.dp))
                                ing.id in prefs.disliked -> Text("👎", modifier = Modifier.padding(end = 8.dp))
                            }
                            Text(
                                "${ing.defaultGramsPerBurrito} g",
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
