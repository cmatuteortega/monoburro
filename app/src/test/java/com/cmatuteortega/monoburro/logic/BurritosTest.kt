package com.cmatuteortega.monoburro.logic

import com.cmatuteortega.monoburro.data.TORTILLA
import com.cmatuteortega.monoburro.data.ingredient
import com.cmatuteortega.monoburro.model.Burrito
import com.cmatuteortega.monoburro.model.Category
import com.cmatuteortega.monoburro.model.Macros
import com.cmatuteortega.monoburro.model.Proposal
import com.cmatuteortega.monoburro.model.ProposalItem
import com.cmatuteortega.monoburro.model.ProposalKind
import com.cmatuteortega.monoburro.model.Ratios
import com.cmatuteortega.monoburro.model.TortillaSize
import com.cmatuteortega.monoburro.model.UserPrefs
import com.cmatuteortega.monoburro.storage.AppState
import com.cmatuteortega.monoburro.storage.migrated
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class BurritosTest {
    private fun burrito(vararg items: Pair<String, Int>, tortilla: TortillaSize = TortillaSize.LARGE) = Burrito(
        id = "b",
        name = "Test",
        items = items.map { ProposalItem(it.first, it.second) } + ProposalItem(TORTILLA.id, tortilla.grams),
        tortilla = tortilla,
    )

    private val proposal = Proposal(
        id = "p",
        kind = ProposalKind.HIGH_PROTEIN,
        name = "High protein",
        tagline = "Grilled chicken with guacamole",
        items = listOf(ProposalItem("grilled-chicken", 100), ProposalItem("guacamole", 30), ProposalItem(TORTILLA.id, 70)),
        macrosPerBurrito = Macros.ZERO,
    )

    @Test fun fromProposalKeepsItemsAndSetsTheTortilla() {
        val b = burritoFrom(proposal, "id", count = 8, tortilla = TortillaSize.XL, createdAt = 1)
        assertEquals("Grilled chicken with guacamole", b.name)
        assertEquals("💪", b.emoji)
        assertEquals(8, b.count)
        assertEquals(ProposalItem(TORTILLA.id, TortillaSize.XL.grams), b.items.last())
        assertEquals(2, b.fillings().size)
    }

    @Test fun ratiosFollowCategoryWeights() {
        val b = burrito("grilled-chicken" to 100, "cilantro-lime-rice" to 50, "pico-de-gallo" to 50)
        assertEquals(Ratios(protein = 50, carb = 25, veg = 25, cheese = 0, sauce = 0), b.ratios())
    }

    @Test fun withRatiosKeepsTotalAndSplitsWithinCategory() {
        val b = burrito("grilled-chicken" to 60, "black-beans" to 20, "cilantro-lime-rice" to 120)
        val r = b.withRatios(Ratios(protein = 60, carb = 40, veg = 0, cheese = 0, sauce = 0))
        val grams = r.fillings().associate { it.ingredientId to it.gramsPerBurrito }
        assertEquals(200, grams.values.sum())
        assertEquals(90, grams["grilled-chicken"]) // 120 g protein, 3:1
        assertEquals(30, grams["black-beans"])
        assertEquals(80, grams["cilantro-lime-rice"])
        assertEquals(TORTILLA.id, r.items.last().ingredientId)
    }

    @Test fun withRatiosGivesAbsentCategoriesShareAway() {
        val b = burrito("grilled-chicken" to 100, "cilantro-lime-rice" to 100)
        // Veg has nothing in it: protein and carb split 100 % as 30:30.
        val r = b.withRatios(Ratios(protein = 30, carb = 30, veg = 40, cheese = 0, sauce = 0))
        assertEquals(listOf(100, 100), r.fillings().map { it.gramsPerBurrito })
    }

    @Test fun tortillaSizeScalesFilling() {
        val b = burrito("grilled-chicken" to 100, "cilantro-lime-rice" to 50)
        val small = b.withTortilla(TortillaSize.SMALL) // 160 / 250 budget
        assertEquals(listOf(64, 32), small.fillings().map { it.gramsPerBurrito })
        assertEquals(ProposalItem(TORTILLA.id, TortillaSize.SMALL.grams), small.items.last())
        assertSame(b, b.withTortilla(TortillaSize.LARGE))
    }

    @Test fun editItemsKeepCategoryOrderAndTheTortilla() {
        val b = burrito("cilantro-lime-rice" to 80)
            .with(ingredient("guacamole"))
            .with(ingredient("grilled-chicken"))
        assertEquals(listOf("grilled-chicken", "cilantro-lime-rice", "guacamole", TORTILLA.id), b.items.map { it.ingredientId })
        assertEquals(b, b.with(ingredient("guacamole")))

        val edited = b.withItemGrams("grilled-chicken", 9999).withItemGrams(TORTILLA.id, 1)
        assertEquals(MAX_ITEM_GRAMS, edited.items.first().gramsPerBurrito)
        assertEquals(TortillaSize.LARGE.grams, edited.items.last().gramsPerBurrito)

        assertEquals(listOf("grilled-chicken", "cilantro-lime-rice", TORTILLA.id), b.without("guacamole").without(TORTILLA.id).items.map { it.ingredientId })
    }

    @Test fun emptyBurritoIsJustATortilla() {
        val b = emptyBurrito("e", "Mine", 6, TortillaSize.MEDIUM, 0)
        assertEquals(listOf(ProposalItem(TORTILLA.id, TortillaSize.MEDIUM.grams)), b.items)
        assertEquals(Ratios(0, 0, 0, 0, 0), b.ratios())
        assertSame(b, b.withRatios(Ratios.DEFAULT))
        assertTrue(b.categoryGrams().values.all { it == 0 })
        assertEquals(Category.FILLINGS.toSet(), b.categoryGrams().keys)
    }

    @Test fun legacyBatchPickMigratesToTheLibrary() {
        val legacy = AppState(prefs = UserPrefs(burritoCount = 10), chosen = proposal)
        val migrated = legacy.migrated({ "new" }, now = 5)
        assertTrue(migrated.onboarded)
        assertNull(migrated.chosen)
        assertEquals("new", migrated.burritos.single().id)
        assertEquals(10, migrated.burritos.single().count)

        val fresh = AppState()
        assertSame(fresh, fresh.migrated({ "x" }, 0))
    }
}
