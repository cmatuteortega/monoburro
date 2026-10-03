package com.cmatuteortega.monoburro.logic

import com.cmatuteortega.monoburro.data.TORTILLA
import com.cmatuteortega.monoburro.model.Burrito
import com.cmatuteortega.monoburro.model.ProposalItem
import com.cmatuteortega.monoburro.model.TortillaSize
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ShoppingTest {
    private var n = 0
    private val ids = { "id${n++}" }

    private fun burrito(name: String, count: Int, vararg items: Pair<String, Int>) = Burrito(
        id = name,
        name = name,
        items = items.map { ProposalItem(it.first, it.second) } + ProposalItem(TORTILLA.id, 70),
        count = count,
        tortilla = TortillaSize.LARGE,
    )

    @Test fun addsTheWholeBatch() {
        val list = addToShopping(emptyList(), burrito("A", 10, "black-beans" to 60), ids)
        assertEquals(2, list.size)
        assertEquals(600, list[0].grams)
        assertEquals("600 g · ≈ 2½ cans (drained)", list[0].amountLabel())
        assertEquals(10, list[1].pieces)
        assertEquals("10 tortillas", list[1].amountLabel())
        assertEquals(listOf("A"), list[0].sources)
    }

    @Test fun mergesUntickedLinesAcrossBurritos() {
        var list = addToShopping(emptyList(), burrito("A", 10, "black-beans" to 60), ids)
        list = addToShopping(list, burrito("B", 5, "black-beans" to 40, "guacamole" to 30), ids)
        val beans = list.single { it.key == "black-beans" }
        assertEquals(800, beans.grams)
        assertEquals(listOf("A", "B"), beans.sources)
        assertEquals(15, list.single { it.ingredientId == TORTILLA.id }.pieces)
        assertEquals(3, list.size)
    }

    @Test fun tickedLinesAreNotMergedInto() {
        val first = addToShopping(emptyList(), burrito("A", 10, "black-beans" to 60), ids, only = "black-beans")
        val ticked = first.map { it.copy(checked = true) }
        val list = addToShopping(ticked, burrito("A", 10, "black-beans" to 60), ids, only = "black-beans")
        assertEquals(2, list.size)
        assertEquals(600, list[1].grams)
    }

    @Test fun singleIngredientAndZeroGramsSkipped() {
        val list = addToShopping(emptyList(), burrito("A", 4, "guacamole" to 0, "grilled-chicken" to 90), ids, only = "guacamole")
        assertEquals(emptyList<Any>(), list)
    }

    @Test fun customItemsMergeByNameAndIgnoreBlanks() {
        var list = addCustomShopping(emptyList(), "  Limes ", ids)
        list = addCustomShopping(list, "limes", ids)
        list = addCustomShopping(list, "   ", ids)
        assertEquals(1, list.size)
        assertEquals("Limes", list.single().name)
        assertNull(list.single().amountLabel())
    }
}
