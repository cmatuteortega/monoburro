package com.cmatuteortega.monoburro.logic

import com.cmatuteortega.monoburro.model.Activity
import com.cmatuteortega.monoburro.model.Goal
import com.cmatuteortega.monoburro.model.MacroGoals
import com.cmatuteortega.monoburro.model.Profile
import com.cmatuteortega.monoburro.model.Sex
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class NutritionTest {
    private val man = Profile(age = 30, weightKg = 80.0, heightCm = 180, sex = Sex.MALE, activity = Activity.MODERATE)

    @Test fun mifflinStJeor() {
        // 800 + 1125 − 150 + 5
        assertEquals(1780.0, bmr(man)!!, 0.01)
        assertEquals(1780.0 - 166, bmr(man.copy(sex = Sex.FEMALE))!!, 0.01)
        assertEquals(1780.0 * 1.55, tdee(man)!!, 0.01)
    }

    @Test fun incompleteProfileHasNoSuggestion() {
        assertNull(bmr(Profile(age = 30, weightKg = 80.0)))
        assertNull(suggestedGoals(Profile(), Goal.EAT))
    }

    @Test fun eatIsMaintenance() {
        // 1780 × 1.55 = 2759 → 2760; protein 1.6 × 80 = 128 → 130; fat 30 % → 92 → 90; carbs the rest → 355
        val g = suggestedGoals(man, Goal.EAT)!!
        assertEquals(MacroGoals(kcal = 2760, protein = 130, carbs = 355, fat = 90), g)
        assertTrue(kotlin.math.abs(g.kcalFromMacros() - g.kcal) < 60)
    }

    @Test fun bulkAndCutMoveCaloriesAndProtein() {
        val eat = suggestedGoals(man, Goal.EAT)!!
        val bulk = suggestedGoals(man, Goal.BULK)!!
        val cut = suggestedGoals(man, Goal.CUT)!!
        assertTrue(bulk.kcal > eat.kcal && cut.kcal < eat.kcal)
        assertEquals(2210, cut.kcal)
        assertEquals(175, cut.protein) // 2.2 g/kg
        assertTrue(cut.protein > eat.protein)
    }
}
