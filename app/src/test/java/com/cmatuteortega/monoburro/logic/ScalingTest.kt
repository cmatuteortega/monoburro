package com.cmatuteortega.monoburro.logic

import com.cmatuteortega.monoburro.data.TORTILLA
import com.cmatuteortega.monoburro.model.FriendlyUnit
import com.cmatuteortega.monoburro.model.ProposalItem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ScalingTest {
    @Test fun scalesGramsByCount() {
        val lines = scaleBatch(listOf(ProposalItem("grilled-chicken", 90), ProposalItem("black-beans", 60)), 12)
        assertEquals(1080, lines[0].totalGrams)
        assertEquals(90, lines[0].gramsPerBurrito)
        assertEquals(720, lines[1].totalGrams)
        // 720 g / 240 g per can = 3 cans
        assertEquals("≈ 3 cans (drained)", lines[1].friendly)
    }

    @Test fun noFriendlyUnitWhenIngredientHasNone() {
        assertNull(scaleBatch(listOf(ProposalItem("grilled-chicken", 90)), 4).single().friendly)
    }

    @Test fun tortillasAreCounted() {
        assertEquals("12 tortillas", scaleBatch(listOf(ProposalItem(TORTILLA.id, 70)), 12).single().friendly)
        assertEquals("1 tortilla", scaleBatch(listOf(ProposalItem(TORTILLA.id, 70)), 1).single().friendly)
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsZeroCount() {
        scaleBatch(listOf(ProposalItem("grilled-chicken", 90)), 0)
    }

    @Test fun formatsGrams() {
        assertEquals("850 g", formatGrams(850))
        assertEquals("1 kg", formatGrams(1000))
        assertEquals("1.1 kg", formatGrams(1080))
        assertEquals("2.5 kg", formatGrams(2460))
    }

    @Test fun friendlyAmountRoundsToQuarters() {
        val can = FriendlyUnit("can", "cans", 240.0)
        assertEquals("≈ 2½ cans", friendlyAmount(600.0, can))
        assertEquals("≈ 1 can", friendlyAmount(250.0, can))
        assertEquals("≈ ¼ can", friendlyAmount(10.0, can))
        assertEquals("≈ 1¾ cans", friendlyAmount(420.0, can))
        assertEquals("≈ 13 cans", friendlyAmount(3100.0, can))
    }

    @Test fun formatsQuantities() {
        assertEquals("3", formatQuantity(3.0))
        assertEquals("½", formatQuantity(0.5))
        assertEquals("2¾", formatQuantity(2.75))
    }
}
