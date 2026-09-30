package com.cmatuteortega.monoburro.model

import kotlinx.serialization.Serializable

enum class Category(val label: String, val emoji: String) {
    PROTEIN("Protein", "🍗"),
    CARB("Carb base", "🍚"),
    VEG("Veg", "🥦"),
    CHEESE("Cheese", "🧀"),
    SAUCE("Sauce & extras", "🥑"),

    /** The fixed wrap: never swiped, always in the burrito. */
    TORTILLA("Tortilla", "🌯");

    companion object {
        /** Categories the user swipes through and sets ratios for, in deck order. */
        val FILLINGS = listOf(PROTEIN, CARB, VEG, CHEESE, SAUCE)
    }
}

enum class Tag(val label: String) {
    MEAT("Meat"),
    SEAFOOD("Seafood"),
    VEGETARIAN("Veggie"),
    VEGAN("Vegan"),
    CONTAINS_DAIRY("Dairy"),
    CONTAINS_EGG("Egg"),
    CONTAINS_GLUTEN("Gluten"),
    SPICY("Spicy"),
}

@Serializable
data class Macros(
    val kcal: Double = 0.0,
    val protein: Double = 0.0,
    val carbs: Double = 0.0,
    val fat: Double = 0.0,
) {
    operator fun plus(o: Macros) = Macros(kcal + o.kcal, protein + o.protein, carbs + o.carbs, fat + o.fat)
    operator fun times(f: Double) = Macros(kcal * f, protein * f, carbs * f, fat * f)

    companion object {
        val ZERO = Macros()
    }
}

/** A kitchen-friendly unit, e.g. one drained can of beans = 240 g. */
data class FriendlyUnit(val singular: String, val plural: String, val grams: Double)

data class Ingredient(
    val id: String,
    val name: String,
    val category: Category,
    val emoji: String,
    /** Per 100 g, as cooked / ready to use. */
    val per100g: Macros,
    val defaultGramsPerBurrito: Int,
    val tags: Set<Tag>,
    val unit: FriendlyUnit? = null,
)
