package com.cmatuteortega.monoburro.model

import kotlinx.serialization.Serializable

/**
 * What the user eats for. Eat 🌯 is the Burro default: burritos, no agenda.
 * Bulk and Cut tune the macro goals and are Mono features.
 */
@Serializable
enum class Goal(val emoji: String, val label: String, val blurb: String, val monoOnly: Boolean) {
    BULK("💪", "Bulk", "Calorie surplus, plenty of protein.", monoOnly = true),
    CUT("🔥", "Cut", "Calorie deficit, protein kept high.", monoOnly = true),
    EAT("🌯", "Eat", "Just eat burritos. Maintenance.", monoOnly = false),
}

@Serializable
enum class Sex(val label: String) { MALE("Male"), FEMALE("Female"), UNSPECIFIED("Skip") }

@Serializable
enum class Activity(val label: String, val blurb: String, val factor: Double) {
    SEDENTARY("Sedentary", "Desk job, little exercise", 1.2),
    LIGHT("Light", "Exercise 1–3 days a week", 1.375),
    MODERATE("Moderate", "Exercise 3–5 days a week", 1.55),
    ACTIVE("Very active", "Hard exercise 6–7 days a week", 1.725),
}

@Serializable
data class Profile(
    val age: Int? = null,
    val weightKg: Double? = null,
    val heightCm: Int? = null,
    val sex: Sex = Sex.UNSPECIFIED,
    val activity: Activity = Activity.MODERATE,
    val favoriteFood: String = "",
    val goal: Goal = Goal.EAT,
) {
    /** Age, weight and height are all set: enough to estimate energy needs. */
    val complete: Boolean get() = age != null && weightKg != null && heightCm != null
}

/** Daily macro goals. */
@Serializable
data class MacroGoals(val kcal: Int, val protein: Int, val carbs: Int, val fat: Int) {
    companion object {
        /** Used until the profile has age, weight and height. */
        val DEFAULT = MacroGoals(kcal = 2200, protein = 110, carbs = 270, fat = 75)
    }
}
