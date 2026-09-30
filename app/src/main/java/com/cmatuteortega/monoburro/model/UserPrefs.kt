package com.cmatuteortega.monoburro.model

import kotlinx.serialization.Serializable

/** Share of filling weight per category in whole percent. Always sums to 100. */
@Serializable
data class Ratios(
    val protein: Int,
    val carb: Int,
    val veg: Int,
    val cheese: Int,
    val sauce: Int,
) {
    operator fun get(c: Category): Int = when (c) {
        Category.PROTEIN -> protein
        Category.CARB -> carb
        Category.VEG -> veg
        Category.CHEESE -> cheese
        Category.SAUCE -> sauce
        Category.TORTILLA -> 0
    }

    val total: Int get() = protein + carb + veg + cheese + sauce

    fun toMap(): Map<Category, Int> = Category.FILLINGS.associateWith { get(it) }

    companion object {
        val DEFAULT = Ratios(protein = 35, carb = 30, veg = 20, cheese = 10, sauce = 5)

        fun fromMap(m: Map<Category, Int>) = Ratios(
            protein = m[Category.PROTEIN] ?: 0,
            carb = m[Category.CARB] ?: 0,
            veg = m[Category.VEG] ?: 0,
            cheese = m[Category.CHEESE] ?: 0,
            sauce = m[Category.SAUCE] ?: 0,
        )
    }
}

@Serializable
enum class TortillaSize(val label: String, val inches: Int, val grams: Int, val fillingBudgetGrams: Int) {
    SMALL("Small", 8, 45, 160),
    MEDIUM("Medium", 10, 55, 200),
    LARGE("Large", 12, 70, 250),
    XL("XL", 14, 95, 320),
}

/** Optional per-burrito target. At most one of the two is set. */
@Serializable
data class Targets(val kcal: Int? = null, val protein: Int? = null)

@Serializable
data class UserPrefs(
    val liked: List<String> = emptyList(),
    val favorites: List<String> = emptyList(),
    val disliked: List<String> = emptyList(),
    val ratios: Ratios = Ratios.DEFAULT,
    val burritoCount: Int = 12,
    val tortilla: TortillaSize = TortillaSize.LARGE,
    val targets: Targets = Targets(),
) {
    /** Liked or favorite: both count as "likes" for the deck quota and proposals. */
    fun likes(id: String) = id in liked || id in favorites
    fun isFavorite(id: String) = id in favorites
    fun seen(id: String) = likes(id) || id in disliked
}
