package com.cmatuteortega.monoburro.logic

import com.cmatuteortega.monoburro.data.TORTILLA
import com.cmatuteortega.monoburro.data.ingredient
import com.cmatuteortega.monoburro.model.Burrito
import com.cmatuteortega.monoburro.model.Category
import com.cmatuteortega.monoburro.model.Ingredient
import com.cmatuteortega.monoburro.model.Macros
import com.cmatuteortega.monoburro.model.Proposal
import com.cmatuteortega.monoburro.model.ProposalItem
import com.cmatuteortega.monoburro.model.ProposalKind
import com.cmatuteortega.monoburro.model.Ratios
import com.cmatuteortega.monoburro.model.TortillaSize
import kotlin.math.roundToInt

/** Grams per burrito stay within this range when edited by hand. */
const val MAX_ITEM_GRAMS = 400

fun ProposalKind.emoji(): String = when (this) {
    ProposalKind.CLASSIC -> "🌯"
    ProposalKind.HIGH_PROTEIN -> "💪"
    ProposalKind.VEGGIE_FORWARD -> "🥦"
}

/** A library burrito from a picked proposal, batched [count] times in [tortilla]s. */
fun burritoFrom(proposal: Proposal, id: String, count: Int, tortilla: TortillaSize, createdAt: Long): Burrito =
    Burrito(
        id = id,
        name = proposal.tagline.ifEmpty { proposal.name }.replaceFirstChar { it.uppercase() },
        emoji = proposal.kind.emoji(),
        items = withTortilla(proposal.items, tortilla),
        count = count,
        tortilla = tortilla,
        createdAt = createdAt,
    )

/** Just a tortilla: the starting point for "build your own". */
fun emptyBurrito(id: String, name: String, count: Int, tortilla: TortillaSize, createdAt: Long): Burrito =
    Burrito(id, name, "🫓", withTortilla(emptyList(), tortilla), count, tortilla, createdAt)

fun Burrito.fillings(): List<ProposalItem> = items.filter { it.ingredientId != TORTILLA.id }

fun Burrito.macrosPerBurrito(lookup: (String) -> Ingredient = ::ingredient): Macros = totalMacros(items, lookup)

/** Grams of filling per category, for the ratio sliders. */
fun Burrito.categoryGrams(lookup: (String) -> Ingredient = ::ingredient): Map<Category, Int> =
    Category.FILLINGS.associateWith { c -> fillings().filter { lookup(it.ingredientId).category == c }.sumOf { it.gramsPerBurrito } }

/** The burrito's current split as whole percentages; all zero when there's no filling. */
fun Burrito.ratios(lookup: (String) -> Ingredient = ::ingredient): Ratios =
    Ratios.fromMap(normalize(categoryGrams(lookup).mapValues { it.value.toDouble() }))

/**
 * Re-splits the filling by [target], keeping the total filling weight. Within
 * a category items keep their relative weights. Categories with nothing in
 * them can't take a share, so the others split it in proportion.
 */
fun Burrito.withRatios(target: Ratios, lookup: (String) -> Ingredient = ::ingredient): Burrito {
    val byCategory = fillings().groupBy { lookup(it.ingredientId).category }
    val total = fillings().sumOf { it.gramsPerBurrito }
    if (total == 0) return this
    val percent = normalize(Category.FILLINGS.associateWith { c -> if (byCategory[c].isNullOrEmpty()) 0.0 else target[c].toDouble() })
    if (percent.values.sum() == 0) return this
    val regrams = fillings().associateWith { item ->
        val siblings = byCategory.getValue(lookup(item.ingredientId).category)
        val siblingSum = siblings.sumOf { it.gramsPerBurrito }
        val share = if (siblingSum == 0) 1.0 / siblings.size else item.gramsPerBurrito.toDouble() / siblingSum
        (total * percent.getValue(lookup(item.ingredientId).category) / 100.0 * share).roundToInt()
    }
    return copy(items = items.map { if (it in regrams) it.copy(gramsPerBurrito = regrams.getValue(it)) else it })
}

/**
 * Swaps the tortilla, scaling the filling with the new size's budget so the
 * burrito still closes.
 */
fun Burrito.withTortilla(size: TortillaSize): Burrito {
    if (size == tortilla) return this
    val factor = size.fillingBudgetGrams.toDouble() / tortilla.fillingBudgetGrams
    val scaled = fillings().map { it.copy(gramsPerBurrito = (it.gramsPerBurrito * factor).roundToInt().coerceAtLeast(1)) }
    return copy(tortilla = size, items = withTortilla(scaled, size))
}

fun Burrito.withItemGrams(ingredientId: String, grams: Int): Burrito = copy(
    items = items.map {
        if (it.ingredientId == ingredientId && it.ingredientId != TORTILLA.id) it.copy(gramsPerBurrito = grams.coerceIn(0, MAX_ITEM_GRAMS)) else it
    },
)

fun Burrito.without(ingredientId: String): Burrito =
    if (ingredientId == TORTILLA.id) this else copy(items = items.filterNot { it.ingredientId == ingredientId })

/**
 * Adds [ingredient] at its default portion, kept in category order (the
 * tortilla stays last). Already in the burrito: no change.
 */
fun Burrito.with(ingredient: Ingredient, lookup: (String) -> Ingredient = ::ingredient): Burrito {
    if (items.any { it.ingredientId == ingredient.id } || ingredient.category == Category.TORTILLA) return this
    val added = fillings() + ProposalItem(ingredient.id, ingredient.defaultGramsPerBurrito)
    val sorted = added.sortedBy { Category.FILLINGS.indexOf(lookup(it.ingredientId).category) }
    return copy(items = withTortilla(sorted, tortilla))
}

/** Fillings plus the tortilla at [size]'s weight, last. */
private fun withTortilla(items: List<ProposalItem>, size: TortillaSize): List<ProposalItem> =
    items.filter { it.ingredientId != TORTILLA.id } + ProposalItem(TORTILLA.id, size.grams)
