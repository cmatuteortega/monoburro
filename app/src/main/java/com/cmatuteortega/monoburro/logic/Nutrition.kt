package com.cmatuteortega.monoburro.logic

import com.cmatuteortega.monoburro.model.Goal
import com.cmatuteortega.monoburro.model.MacroGoals
import com.cmatuteortega.monoburro.model.Profile
import com.cmatuteortega.monoburro.model.Sex
import kotlin.math.roundToInt

/** Basal metabolic rate (Mifflin–St Jeor), kcal/day. Null until the profile is complete. */
fun bmr(profile: Profile): Double? {
    val age = profile.age ?: return null
    val weight = profile.weightKg ?: return null
    val height = profile.heightCm ?: return null
    val sexTerm = when (profile.sex) {
        Sex.MALE -> 5.0
        Sex.FEMALE -> -161.0
        Sex.UNSPECIFIED -> -78.0 // halfway between the two
    }
    return 10 * weight + 6.25 * height - 5 * age + sexTerm
}

/** Maintenance calories: BMR × activity. */
fun tdee(profile: Profile): Double? = bmr(profile)?.let { it * profile.activity.factor }

private fun Goal.kcalFactor(): Double = when (this) {
    Goal.BULK -> 1.10
    Goal.CUT -> 0.80
    Goal.EAT -> 1.0
}

/** Protein per kg of body weight. */
private fun Goal.proteinPerKg(): Double = when (this) {
    Goal.BULK -> 1.8
    Goal.CUT -> 2.2
    Goal.EAT -> 1.6
}

/** Share of calories from fat. */
private fun Goal.fatShare(): Double = when (this) {
    Goal.BULK -> 0.25
    Goal.CUT -> 0.25
    Goal.EAT -> 0.30
}

/**
 * Daily goals from the profile: maintenance × the goal's surplus or deficit,
 * protein by body weight, fat as a share of calories, carbs fill the rest.
 * Calories round to 10, grams to 5. Null until the profile is complete.
 */
fun suggestedGoals(profile: Profile, goal: Goal): MacroGoals? {
    val maintenance = tdee(profile) ?: return null
    val weight = profile.weightKg ?: return null
    val kcal = maintenance * goal.kcalFactor()
    val protein = weight * goal.proteinPerKg()
    val fat = kcal * goal.fatShare() / 9
    val carbs = ((kcal - protein * 4 - fat * 9) / 4).coerceAtLeast(0.0)
    return MacroGoals(
        kcal = roundTo(kcal, 10),
        protein = roundTo(protein, 5),
        carbs = roundTo(carbs, 5),
        fat = roundTo(fat, 5),
    )
}

/** kcal from the three macros (4 / 4 / 9 per gram). */
fun MacroGoals.kcalFromMacros(): Int = protein * 4 + carbs * 4 + fat * 9

private fun roundTo(x: Double, step: Int): Int = (x / step).roundToInt() * step
