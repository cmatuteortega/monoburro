package com.cmatuteortega.monoburro.logic

import com.cmatuteortega.monoburro.model.Category
import com.cmatuteortega.monoburro.model.Ratios
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RatioMathTest {
    @Test fun defaultSumsTo100() = assertEquals(100, Ratios.DEFAULT.total)

    @Test fun rebalanceKeepsTotalAt100ForEveryValue() {
        Category.FILLINGS.forEach { c ->
            (0..100).forEach { v ->
                val r = rebalance(Ratios.DEFAULT, c, v)
                assertEquals("$c=$v -> $r", 100, r.total)
                assertEquals(v, r[c])
                Category.FILLINGS.forEach { assertTrue(r[it] >= 0) }
            }
        }
    }

    @Test fun rebalanceIsProportional() {
        // Protein 35 -> 45: the other 65 shrink to 55, keeping their 30:20:10:5 shape
        // (25.4, 16.9, 8.5, 4.2 -> largest remainders get the two leftover points).
        val r = rebalance(Ratios.DEFAULT, Category.PROTEIN, 45)
        assertEquals(Ratios(45, 25, 17, 9, 4), r)
    }

    @Test fun fromZeroOthersShareEqually() {
        val allProtein = Ratios(100, 0, 0, 0, 0)
        assertEquals(Ratios(60, 10, 10, 10, 10), rebalance(allProtein, Category.PROTEIN, 60))
    }

    @Test fun clampsOutOfRange() {
        assertEquals(100, rebalance(Ratios.DEFAULT, Category.VEG, 150)[Category.VEG])
        assertEquals(0, rebalance(Ratios.DEFAULT, Category.VEG, -5)[Category.VEG])
    }

    @Test fun chainOfMovesStaysValid() {
        var r = Ratios.DEFAULT
        listOf(Category.SAUCE to 40, Category.PROTEIN to 3, Category.CHEESE to 77, Category.CARB to 0, Category.VEG to 51)
            .forEach { (c, v) -> r = rebalance(r, c, v); assertEquals(100, r.total) }
    }

    @Test fun normalizeSumsTo100() {
        val n = normalize(mapOf(Category.PROTEIN to 1.0, Category.CARB to 1.0, Category.VEG to 1.0))
        assertEquals(100, n.values.sum())
        assertEquals(34, n[Category.PROTEIN])
    }
}
