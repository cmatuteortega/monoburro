package com.cmatuteortega.monoburro.logic

import com.cmatuteortega.monoburro.data.INGREDIENTS
import com.cmatuteortega.monoburro.model.Category
import com.cmatuteortega.monoburro.model.UserPrefs
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DeckTest {
    @Test fun startsWithFirstProtein() {
        assertEquals("grilled-chicken", nextCard(UserPrefs(), exhaustive = false)?.id)
    }

    @Test fun skipsCategoryOnceQuotaMet() {
        val prefs = UserPrefs(liked = listOf("grilled-chicken", "carnitas"), favorites = listOf("barbacoa"))
        assertEquals(Category.CARB, nextCard(prefs, exhaustive = false)?.category)
        // Keep-swiping mode still shows the remaining proteins.
        assertEquals("chicken-tinga", nextCard(prefs, exhaustive = true)?.id)
    }

    @Test fun likingEverythingNeededFinishesTheDeck() {
        var prefs = UserPrefs()
        var swipes = 0
        while (true) {
            val card = nextCard(prefs, exhaustive = false) ?: break
            prefs = prefs.copy(liked = prefs.liked + card.id)
            swipes++
        }
        assertTrue(quotaMet(prefs))
        assertEquals(SWIPE_QUOTA.values.sum(), swipes)
    }

    @Test fun dislikingEverythingEndsAfterWholeDeck() {
        var prefs = UserPrefs()
        while (true) {
            val card = nextCard(prefs, exhaustive = false) ?: break
            prefs = prefs.copy(disliked = prefs.disliked + card.id)
        }
        assertFalse(quotaMet(prefs))
        assertEquals(INGREDIENTS.size, prefs.disliked.size)
        assertEquals(0, unseenCount(prefs))
    }

    @Test fun cardAfterPeeksAhead() {
        assertEquals("chicken-tinga", cardAfter(UserPrefs(), exhaustive = false)?.id)
        val almostDone = UserPrefs(disliked = INGREDIENTS.dropLast(1).map { it.id })
        assertNull(cardAfter(almostDone, exhaustive = false))
    }
}
