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
 * Everything that can differ between modes. For now every feature is built for
 * both, so [monoOnly] is false across the board; flip it to gate a feature
 * behind the subscription and check it with [Mode.can].
 */
enum class Feature(val monoOnly: Boolean) {
    TASTE_SWIPE(monoOnly = false),
    RATIOS(monoOnly = false),
    PROPOSALS(monoOnly = false),
    BATCH_PLAN(monoOnly = false),
}

fun Mode.can(feature: Feature): Boolean = this == Mode.MONO || !feature.monoOnly

/**
 * The mode the app should actually run in: Mono needs an active subscription,
 * so a Mono pick without one falls back to Burro. Null means not chosen yet
 * (show the landing).
 */
fun effectiveMode(chosen: Mode?, entitled: Boolean): Mode? =
    if (chosen == Mode.MONO && !entitled) Mode.BURRO else chosen
