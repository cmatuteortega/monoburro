package com.cmatuteortega.monoburro.logic

import com.cmatuteortega.monoburro.data.ingredient
import com.cmatuteortega.monoburro.model.Category
import com.cmatuteortega.monoburro.model.FriendlyUnit
import com.cmatuteortega.monoburro.model.Ingredient
import com.cmatuteortega.monoburro.model.ProposalItem
import java.util.Locale
import kotlin.math.roundToInt
import kotlin.math.roundToLong

data class BatchLine(
    val ingredient: Ingredient,
    val gramsPerBurrito: Int,
    val totalGrams: Int,
    /** e.g. "≈ 2½ cans (drained)", or "12 tortillas". Null when no sensible unit. */
    val friendly: String?,
)

/** Scales per-burrito items to the whole batch, in the order given. */
fun scaleBatch(
    items: List<ProposalItem>,
    burritoCount: Int,
    lookup: (String) -> Ingredient = ::ingredient,
): List<BatchLine> {
    require(burritoCount > 0) { "burritoCount must be positive" }
    return items.map { item ->
        val ing = lookup(item.ingredientId)
        val total = item.gramsPerBurrito * burritoCount
        val friendly = if (ing.category == Category.TORTILLA) {
            countLabel(burritoCount, "tortilla", "tortillas")
        } else {
            ing.unit?.let { friendlyAmount(total.toDouble(), it) }
        }
        BatchLine(ing, item.gramsPerBurrito, total, friendly)
    }
}

/** "850 g", "1.2 kg", "2 kg". */
fun formatGrams(grams: Int): String = when {
    grams < 1000 -> "$grams g"
    else -> {
        val kg = (grams / 100.0).roundToLong() / 10.0
        if (kg % 1.0 == 0.0) "${kg.toLong()} kg" else String.format(Locale.US, "%.1f kg", kg)
    }
}

/**
 * "≈ 2½ cans (drained)". Rounded to the nearest quarter under 10 units, to
 * the nearest whole one above. Never shows less than ¼.
 */
fun friendlyAmount(grams: Double, unit: FriendlyUnit): String {
    val qty = grams / unit.grams
    val rounded = if (qty < 10) (qty * 4).roundToInt().coerceAtLeast(1) / 4.0 else qty.roundToInt().toDouble()
    val word = if (rounded <= 1.0) unit.singular else unit.plural
    return "≈ ${formatQuantity(rounded)} $word"
}

/** 2.5 → "2½", 0.25 → "¼", 3.0 → "3". */
fun formatQuantity(q: Double): String {
    val whole = q.toLong()
    val frac = when (((q - whole) * 4).roundToInt()) {
        1 -> "¼"
        2 -> "½"
        3 -> "¾"
        else -> ""
    }
    return if (whole == 0L && frac.isNotEmpty()) frac else "$whole$frac"
}

private fun countLabel(n: Int, singular: String, plural: String) = "$n ${if (n == 1) singular else plural}"
