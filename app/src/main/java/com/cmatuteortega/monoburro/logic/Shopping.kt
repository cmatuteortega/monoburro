package com.cmatuteortega.monoburro.logic

import com.cmatuteortega.monoburro.data.TORTILLA
import com.cmatuteortega.monoburro.data.ingredientOrNull
import com.cmatuteortega.monoburro.model.Burrito
import com.cmatuteortega.monoburro.model.Ingredient
import com.cmatuteortega.monoburro.model.ShoppingItem
import com.cmatuteortega.monoburro.model.TortillaSize
import com.cmatuteortega.monoburro.data.ingredient as lookupIngredient

/**
 * Adds what [burrito]'s whole batch needs to [list]. Pass [only] to add a
 * single ingredient. Lines merge with an unticked line for the same thing
 * (amounts add up); a ticked one is already bought, so a new line starts.
 */
fun addToShopping(
    list: List<ShoppingItem>,
    burrito: Burrito,
    newId: () -> String,
    only: String? = null,
    lookup: (String) -> Ingredient = ::lookupIngredient,
): List<ShoppingItem> = burrito.items
    .filter { only == null || it.ingredientId == only }
    .filter { it.gramsPerBurrito > 0 }
    .fold(list) { acc, item ->
        val line = if (item.ingredientId == TORTILLA.id) {
            tortillaLine(burrito.tortilla, burrito.count, newId)
        } else {
            val ing = lookup(item.ingredientId)
            ShoppingItem(
                id = newId(),
                key = ing.id,
                name = ing.name,
                emoji = ing.emoji,
                ingredientId = ing.id,
                grams = item.gramsPerBurrito * burrito.count,
            )
        }
        merge(acc, line.copy(sources = listOf(burrito.name)))
    }

/** A typed-in line ("limes", "foil"). Blank names are ignored; repeats merge. */
fun addCustomShopping(list: List<ShoppingItem>, name: String, newId: () -> String): List<ShoppingItem> {
    val clean = name.trim()
    if (clean.isEmpty()) return list
    return merge(list, ShoppingItem(id = newId(), key = "custom:${clean.lowercase()}", name = clean))
}

/** "1.2 kg · ≈ 6½ cups cooked", "12 tortillas", or null for a typed-in line. */
fun ShoppingItem.amountLabel(): String? {
    pieces?.let { return "$it ${if (it == 1) "tortilla" else "tortillas"}" }
    val g = grams ?: return null
    val friendly = ingredientId?.let(::ingredientOrNull)?.unit?.let { friendlyAmount(g.toDouble(), it) }
    return listOfNotNull(formatGrams(g), friendly).joinToString(" · ")
}

private fun tortillaLine(size: TortillaSize, count: Int, newId: () -> String) = ShoppingItem(
    id = newId(),
    key = "${TORTILLA.id}:${size.name}",
    name = "${size.label} ${size.inches}\" flour tortillas",
    emoji = TORTILLA.emoji,
    ingredientId = TORTILLA.id,
    pieces = count,
)

private fun merge(list: List<ShoppingItem>, line: ShoppingItem): List<ShoppingItem> {
    val i = list.indexOfFirst { it.key == line.key && !it.checked }
    if (i < 0) return list + line
    val old = list[i]
    val merged = old.copy(
        grams = sumOrNull(old.grams, line.grams),
        pieces = sumOrNull(old.pieces, line.pieces),
        sources = (old.sources + line.sources).distinct(),
    )
    return list.toMutableList().also { it[i] = merged }
}

private fun sumOrNull(a: Int?, b: Int?): Int? = if (a == null && b == null) null else (a ?: 0) + (b ?: 0)
