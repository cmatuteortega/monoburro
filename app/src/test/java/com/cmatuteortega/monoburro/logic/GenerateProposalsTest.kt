package com.cmatuteortega.monoburro.logic

import com.cmatuteortega.monoburro.data.INGREDIENTS
import com.cmatuteortega.monoburro.data.TORTILLA
import com.cmatuteortega.monoburro.data.ingredient
import com.cmatuteortega.monoburro.model.Category
import com.cmatuteortega.monoburro.model.ProposalKind
import com.cmatuteortega.monoburro.model.Ratios
import com.cmatuteortega.monoburro.model.Tag
import com.cmatuteortega.monoburro.model.Targets
import com.cmatuteortega.monoburro.model.TortillaSize
import com.cmatuteortega.monoburro.model.UserPrefs
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.abs

class GenerateProposalsTest {
    private val omnivore = UserPrefs(
        liked = listOf(
            "grilled-chicken", "carnitas", "black-beans",
            "cilantro-lime-rice", "quinoa",
            "fajita-veg", "pico-de-gallo", "roasted-corn", "lettuce",
            "cheddar", "monterey-jack",
            "salsa-verde", "sour-cream",
        ),
        favorites = listOf("barbacoa", "guacamole", "greek-yogurt"),
        disliked = listOf("chorizo", "tofu-sofritas"),
    )

    private fun proteinsOf(p: com.cmatuteortega.monoburro.model.Proposal) =
        p.items.map { ingredient(it.ingredientId) }.filter { it.category == Category.PROTEIN }.map { it.id }

    private fun sauceOf(p: com.cmatuteortega.monoburro.model.Proposal) =
        p.items.map { ingredient(it.ingredientId) }.first { it.category == Category.SAUCE }.id

    @Test fun threeKindsInOrder() {
        val ps = generateProposals(omnivore)
        assertEquals(listOf(ProposalKind.CLASSIC, ProposalKind.HIGH_PROTEIN, ProposalKind.VEGGIE_FORWARD), ps.map { it.kind })
        assertEquals(3, ps.map { it.id }.toSet().size)
    }

    @Test fun isDeterministic() = assertEquals(generateProposals(omnivore), generateProposals(omnivore))

    @Test fun onlyLikedIngredientsAndOneTortillaLast() {
        generateProposals(omnivore).forEach { p ->
            val fillings = p.items.dropLast(1)
            assertEquals(TORTILLA.id, p.items.last().ingredientId)
            assertEquals(70, p.items.last().gramsPerBurrito)
            fillings.forEach { assertTrue(it.ingredientId, omnivore.likes(it.ingredientId)) }
            assertTrue(p.items.all { it.gramsPerBurrito > 0 })
        }
    }

    @Test fun favoritesLeadTheClassic() {
        val classic = generateProposals(omnivore).first()
        assertEquals(listOf("barbacoa"), proteinsOf(classic))
        assertEquals("guacamole", sauceOf(classic))
    }

    @Test fun proposalsDifferInProteinAndSauce() {
        val ps = generateProposals(omnivore)
        assertEquals(3, ps.map { proteinsOf(it).first() }.toSet().size)
        assertEquals(3, ps.map { sauceOf(it) }.toSet().size)
    }

    @Test fun highProteinHasMoreProteinAndVeggieMoreVeg() {
        val (classic, high, veggie) = generateProposals(omnivore)
        assertTrue(high.macrosPerBurrito.protein > classic.macrosPerBurrito.protein)
        fun vegGrams(p: com.cmatuteortega.monoburro.model.Proposal) =
            p.items.filter { ingredient(it.ingredientId).category == Category.VEG }.sumOf { it.gramsPerBurrito }
        assertTrue(vegGrams(veggie) > vegGrams(classic))
        assertTrue(vegGrams(veggie) > vegGrams(high))
        // Veggie-forward prefers the plant protein.
        assertEquals(listOf("black-beans"), proteinsOf(veggie))
    }

    @Test fun fillingFollowsBudgetAndRatios() {
        val classic = generateProposals(omnivore).first()
        val filling = classic.items.dropLast(1)
        val total = filling.sumOf { it.gramsPerBurrito }
        assertTrue("total $total", abs(total - 250) <= 15)
        val protein = filling.filter { ingredient(it.ingredientId).category == Category.PROTEIN }.sumOf { it.gramsPerBurrito }
        assertEquals(0.35 * 250, protein.toDouble(), 5.0)
    }

    @Test fun macrosMatchItems() {
        generateProposals(omnivore).forEach { assertEquals(totalMacros(it.items), it.macrosPerBurrito) }
    }

    @Test fun vegetarianLikesNeverGetMeat() {
        val veggie = UserPrefs(
            liked = listOf("black-beans", "tofu-sofritas", "scrambled-egg", "brown-rice", "fajita-veg", "spinach", "mushrooms", "cheddar", "salsa-roja"),
        )
        assertEquals(Diet.VEGETARIAN, inferDiet(veggie, INGREDIENTS))
        generateProposals(veggie).forEach { p ->
            p.items.forEach {
                val tags = ingredient(it.ingredientId).tags
                assertFalse(Tag.MEAT in tags || Tag.SEAFOOD in tags)
            }
        }
    }

    @Test fun veganDietExcludesDairyAndEgg() {
        val vegan = UserPrefs(liked = listOf("black-beans", "tempeh", "quinoa", "zucchini", "vegan-cheese", "guacamole"))
        assertEquals(Diet.VEGAN, inferDiet(vegan, INGREDIENTS))
        assertFalse(Diet.VEGAN.allows(ingredient("cheddar")))
        assertTrue(Diet.PESCATARIAN.allows(ingredient("garlic-shrimp")))
        assertFalse(Diet.PESCATARIAN.allows(ingredient("carnitas")))
    }

    @Test fun emptyCategoryGivesItsShareAway() {
        val noCheese = omnivore.copy(liked = omnivore.liked - listOf("cheddar", "monterey-jack"))
        generateProposals(noCheese).forEach { p ->
            assertTrue(p.items.none { ingredient(it.ingredientId).category == Category.CHEESE })
            assertTrue(abs(p.items.dropLast(1).sumOf { it.gramsPerBurrito } - 250) <= 15)
        }
    }

    @Test fun tortillaSizeChangesBudget() {
        val xl = generateProposals(omnivore.copy(tortilla = TortillaSize.XL)).first()
        assertEquals(95, xl.items.last().gramsPerBurrito)
        assertTrue(abs(xl.items.dropLast(1).sumOf { it.gramsPerBurrito } - 320) <= 15)
    }

    @Test fun kcalTargetMovesTowardsTarget() {
        val plain = generateProposals(omnivore).first().macrosPerBurrito.kcal
        val low = generateProposals(omnivore.copy(targets = Targets(kcal = 550))).first().macrosPerBurrito.kcal
        val high = generateProposals(omnivore.copy(targets = Targets(kcal = 850))).first().macrosPerBurrito.kcal
        assertTrue(low < plain && high > plain)
        assertEquals(550.0, low, 40.0)
        assertEquals(850.0, high, 40.0)
    }

    @Test fun proteinTargetIsReachedWhenPossible() {
        val plain = generateProposals(omnivore)
        val (classic, high, veggie) = generateProposals(omnivore.copy(targets = Targets(protein = 50)))
        assertTrue(classic.macrosPerBurrito.protein >= 50)
        assertTrue(high.macrosPerBurrito.protein >= 50)
        // Veg and carbs keep a minimum portion, so veggie-forward only gets closer.
        assertTrue(veggie.macrosPerBurrito.protein > plain[2].macrosPerBurrito.protein + 10)
    }

    @Test fun customRatiosAreUsed() {
        val proteinHeavy = omnivore.copy(ratios = Ratios(60, 10, 20, 5, 5))
        val classic = generateProposals(proteinHeavy).first()
        val protein = classic.items.filter { ingredient(it.ingredientId).category == Category.PROTEIN }.sumOf { it.gramsPerBurrito }
        assertEquals(150.0, protein.toDouble(), 5.0)
        assertNotEquals(generateProposals(omnivore).first(), classic)
    }
}
