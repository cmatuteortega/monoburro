package com.cmatuteortega.monoburro.model

import kotlinx.serialization.Serializable

/**
 * How the user runs the app. Mono 🐒 is the paid tier (monthly subscription),
 * Burro 🫏 the free one. Chosen on the landing screen and shown top right
 * everywhere after that.
 */
@Serializable
enum class Mode(val emoji: String, val fallbackEmoji: String, val label: String, val tagline: String) {
    MONO("🐒", "🐒", "Mono", "Clever monkey. AI does the thinking."),
    // 🫏 is Emoji 15 (Android 14+); older fonts draw a box, so those get a horse.
    BURRO("🫏", "🐴", "Burro", "Stubborn donkey. Free, does the job."),
}

/**
 * Everything that can differ between modes. Flip [monoOnly] to gate a feature
 * behind the subscription and check it with [Mode.can].
 */
enum class Feature(val monoOnly: Boolean) {
    TASTE_SWIPE(monoOnly = false),
    RATIOS(monoOnly = false),
    PROPOSALS(monoOnly = false),
    BATCH_PLAN(monoOnly = false),
    BURRITO_LIBRARY(monoOnly = false),
    SHOPPING_LIST(monoOnly = false),
    MACRO_GOALS(monoOnly = false),
    PROFILE(monoOnly = false),

    /** The Bulk and Cut goals; Burro always runs in Eat. */
    BULK_CUT(monoOnly = true),
}

fun Mode.can(feature: Feature): Boolean = this == Mode.MONO || !feature.monoOnly

fun Mode.can(goal: Goal): Boolean = !goal.monoOnly || can(Feature.BULK_CUT)

/**
 * The goal the app actually uses: a Mono-only goal picked while subscribed
 * falls back to Eat when running as Burro (it comes back if Mono does).
 */
fun effectiveGoal(chosen: Goal, mode: Mode): Goal = if (mode.can(chosen)) chosen else Goal.EAT

/**
 * The mode the app should actually run in: Mono needs an active subscription,
 * so a Mono pick without one falls back to Burro. Null means not chosen yet
 * (show the landing).
 */
fun effectiveMode(chosen: Mode?, entitled: Boolean): Mode? =
    if (chosen == Mode.MONO && !entitled) Mode.BURRO else chosen
