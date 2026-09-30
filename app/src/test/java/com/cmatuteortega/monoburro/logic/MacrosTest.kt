package com.cmatuteortega.monoburro.logic

import com.cmatuteortega.monoburro.data.INGREDIENTS
import com.cmatuteortega.monoburro.data.TORTILLA
import com.cmatuteortega.monoburro.data.ingredient
import com.cmatuteortega.monoburro.model.Category
import com.cmatuteortega.monoburro.model.Ingredient
import com.cmatuteortega.monoburro.model.Macros
import com.cmatuteortega.monoburro.model.ProposalItem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MacrosTest {
    private val fake = Ingredient("x", "X", Category.PROTEIN, "x", Macros(200.0, 20.0, 10.0, 8.0), 50, emptySet())

    @Test fun macrosScaleLinearlyWithGrams() {
        val m = macrosFor(fake, 150)
        assertEquals(300.0, m.kcal, 1e-9)
        assertEquals(30.0, m.protein, 1e-9)
        assertEquals(15.0, m.carbs, 1e-9)
        assertEquals(12.0, m.fat, 1e-9)
    }

    @Test fun zeroGramsIsZero() {
        assertEquals(Macros.ZERO, macrosFor(fake, 0))
    }

    @Test fun totalSumsItems() {
        val items = listOf(ProposalItem("grilled-chicken", 100), ProposalItem(TORTILLA.id, 70))
        val m = totalMacros(items)
        assertEquals(165.0 + 304.0 * 0.7, m.kcal, 1e-9)
        assertEquals(31.0 + 8.1 * 0.7, m.protein, 1e-9)
        assertEquals(50.0 * 0.7, m.carbs, 1e-9)
        assertEquals(3.6 + 7.7 * 0.7, m.fat, 1e-9)
    }

    @Test fun totalUsesInjectedLookup() {
        val m = totalMacros(listOf(ProposalItem("x", 50), ProposalItem("x", 50))) { fake }
        assertEquals(200.0, m.kcal, 1e-9)
    }

    @Test fun roundedRoundsEachField() {
        assertEquals(Macros(101.0, 11.0, 0.0, 3.0), Macros(100.5, 10.6, 0.4, 2.5).rounded())
    }

    @Test fun seedDataIsSane() {
        assertEquals(40, INGREDIENTS.size)
        assertEquals(INGREDIENTS.size, INGREDIENTS.map { it.id }.toSet().size)
        (INGREDIENTS + TORTILLA).forEach { i ->
            val m = i.per100g
            // Energy from macros (4/4/9) should roughly match the stated kcal.
            val atwater = m.protein * 4 + m.carbs * 4 + m.fat * 9
            assertTrue("${i.id}: $atwater vs ${m.kcal}", kotlin.math.abs(atwater - m.kcal) <= m.kcal * 0.2 + 10)
            assertTrue(i.defaultGramsPerBurrito > 0)
        }
        assertEquals(70, ingredient(TORTILLA.id).defaultGramsPerBurrito)
        Category.FILLINGS.forEach { c -> assertTrue(INGREDIENTS.count { it.category == c } >= 5) }
    }
}
