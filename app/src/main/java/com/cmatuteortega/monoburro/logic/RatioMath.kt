package com.cmatuteortega.monoburro.logic

import com.cmatuteortega.monoburro.model.Category
import com.cmatuteortega.monoburro.model.Ratios
import kotlin.math.floor

/**
 * Sets [category] to [value] and spreads the difference over the other
 * categories in proportion to their current share, so the total stays at
 * exactly 100. If the others are all zero, they share the rest equally.
 */
fun rebalance(ratios: Ratios, category: Category, value: Int): Ratios {
    require(category in Category.FILLINGS)
    val v = value.coerceIn(0, 100)
    val others = Category.FILLINGS - category
    val remaining = 100 - v
    val current = others.associateWith { ratios[it] }
    val sum = current.values.sum()

    val exact: Map<Category, Double> = others.associateWith {
        if (sum == 0) remaining.toDouble() / others.size else current.getValue(it) * remaining.toDouble() / sum
    }
    return Ratios.fromMap(largestRemainder(exact, remaining) + (category to v))
}

/** Normalises any non-negative weights to whole percentages summing to 100. */
fun normalize(weights: Map<Category, Double>): Map<Category, Int> {
    val sum = weights.values.sum()
    if (sum <= 0.0) return weights.mapValues { 0 }
    return largestRemainder(weights.mapValues { it.value * 100.0 / sum }, 100)
}

/** Rounds [exact] down, then hands the leftover units to the largest remainders (ties: declaration order). */
internal fun <K> largestRemainder(exact: Map<K, Double>, total: Int): Map<K, Int> {
    val floors = exact.mapValues { floor(it.value).toInt() }.toMutableMap()
    var leftover = total - floors.values.sum()
    val byRemainder = exact.keys.sortedByDescending { exact.getValue(it) - floors.getValue(it) }
    var i = 0
    while (leftover > 0 && byRemainder.isNotEmpty()) {
        val k = byRemainder[i % byRemainder.size]
        floors[k] = floors.getValue(k) + 1
        leftover--
        i++
    }
    return floors
}
