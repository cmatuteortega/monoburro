package com.cmatuteortega.monoburro.logic

import com.cmatuteortega.monoburro.data.INGREDIENTS
import com.cmatuteortega.monoburro.model.Category
import com.cmatuteortega.monoburro.model.Ingredient
import com.cmatuteortega.monoburro.model.UserPrefs

/** Likes needed per category before the deck stops. */
val SWIPE_QUOTA: Map<Category, Int> = mapOf(
    Category.PROTEIN to 3,
    Category.CARB to 1,
    Category.VEG to 3,
    Category.CHEESE to 1,
    Category.SAUCE to 1,
)

fun likedCount(prefs: UserPrefs, category: Category, deck: List<Ingredient> = INGREDIENTS): Int =
    deck.count { it.category == category && prefs.likes(it.id) }

fun quotaMet(prefs: UserPrefs, deck: List<Ingredient> = INGREDIENTS): Boolean =
    SWIPE_QUOTA.all { (c, n) -> likedCount(prefs, c, deck) >= n }

/**
 * The next card to show, grouped by category. Normally a category is skipped
 * once its quota is met, so the user only swipes what's needed; with
 * [exhaustive] (the user chose "keep swiping") every unseen card is shown.
 * Null means the deck is done.
 */
fun nextCard(prefs: UserPrefs, exhaustive: Boolean, deck: List<Ingredient> = INGREDIENTS): Ingredient? =
    deck.firstOrNull { card ->
        !prefs.seen(card.id) &&
            (exhaustive || likedCount(prefs, card.category, deck) < (SWIPE_QUOTA[card.category] ?: 0))
    }

/** The card after [nextCard], for the stacked preview behind it. */
fun cardAfter(prefs: UserPrefs, exhaustive: Boolean, deck: List<Ingredient> = INGREDIENTS): Ingredient? {
    val current = nextCard(prefs, exhaustive, deck) ?: return null
    // Assume the user dislikes the current card: the only outcome that never changes quotas.
    return nextCard(prefs.copy(disliked = prefs.disliked + current.id), exhaustive, deck)
}

fun unseenCount(prefs: UserPrefs, deck: List<Ingredient> = INGREDIENTS): Int = deck.count { !prefs.seen(it.id) }
