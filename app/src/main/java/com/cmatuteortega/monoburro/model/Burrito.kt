package com.cmatuteortega.monoburro.model

import kotlinx.serialization.Serializable

/**
 * A saved burrito in the user's library: what goes in one (fillings in
 * category order, the tortilla last) and how many to batch-cook.
 */
@Serializable
data class Burrito(
    val id: String,
    val name: String,
    val emoji: String = "🌯",
    val items: List<ProposalItem>,
    val count: Int = 12,
    val tortilla: TortillaSize = TortillaSize.LARGE,
    val createdAt: Long = 0,
)

/** One line of the shopping list, either from a burrito or typed in by hand. */
@Serializable
data class ShoppingItem(
    val id: String,
    /** Lines with the same key merge when added again (ingredient id, tortilla size, or the typed name). */
    val key: String,
    val name: String,
    val emoji: String = "🛒",
    val ingredientId: String? = null,
    /** Total weight to buy, cooked / ready to use. Null for typed-in items. */
    val grams: Int? = null,
    /** For things bought by the piece (tortillas). */
    val pieces: Int? = null,
    val checked: Boolean = false,
    /** Names of the burritos this line is for. */
    val sources: List<String> = emptyList(),
)
