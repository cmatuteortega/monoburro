package com.cmatuteortega.monoburro.logic

import com.cmatuteortega.monoburro.data.ingredient
import com.cmatuteortega.monoburro.model.Ingredient
import com.cmatuteortega.monoburro.model.Macros
import com.cmatuteortega.monoburro.model.ProposalItem

/** Macros for [grams] of [ingredient] (its values are per 100 g). */
fun macrosFor(ingredient: Ingredient, grams: Number): Macros = ingredient.per100g * (grams.toDouble() / 100.0)

/** Sum of macros for a list of items. [lookup] is injectable for tests. */
fun totalMacros(
    items: List<ProposalItem>,
    lookup: (String) -> Ingredient = ::ingredient,
): Macros = items.fold(Macros.ZERO) { acc, item -> acc + macrosFor(lookup(item.ingredientId), item.gramsPerBurrito) }

/** Rounded for display: whole kcal and grams. */
fun Macros.rounded(): Macros = Macros(
    kcal = Math.round(kcal).toDouble(),
    protein = Math.round(protein).toDouble(),
    carbs = Math.round(carbs).toDouble(),
    fat = Math.round(fat).toDouble(),
)
