package com.cmatuteortega.monoburro.logic

import com.cmatuteortega.monoburro.model.Ingredient
import com.cmatuteortega.monoburro.model.Tag
import com.cmatuteortega.monoburro.model.UserPrefs

enum class Diet { OMNIVORE, PESCATARIAN, VEGETARIAN, VEGAN }

/**
 * Infers the diet from what the user likes: no meat liked means no meat is
 * ever added, and so on. Vegan only when every liked item is vegan.
 */
fun inferDiet(prefs: UserPrefs, ingredients: List<Ingredient>): Diet {
    val liked = ingredients.filter { prefs.likes(it.id) }
    return when {
        liked.any { Tag.MEAT in it.tags } -> Diet.OMNIVORE
        liked.any { Tag.SEAFOOD in it.tags } -> Diet.PESCATARIAN
        liked.isNotEmpty() && liked.all { Tag.VEGAN in it.tags } -> Diet.VEGAN
        else -> Diet.VEGETARIAN
    }
}

fun Diet.allows(ingredient: Ingredient): Boolean = when (this) {
    Diet.OMNIVORE -> true
    Diet.PESCATARIAN -> Tag.MEAT !in ingredient.tags
    Diet.VEGETARIAN -> Tag.MEAT !in ingredient.tags && Tag.SEAFOOD !in ingredient.tags
    Diet.VEGAN -> Tag.VEGAN in ingredient.tags
}
