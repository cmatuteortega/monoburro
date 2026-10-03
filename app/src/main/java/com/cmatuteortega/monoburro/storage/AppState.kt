package com.cmatuteortega.monoburro.storage

import com.cmatuteortega.monoburro.logic.burritoFrom
import com.cmatuteortega.monoburro.model.Burrito
import com.cmatuteortega.monoburro.model.MacroGoals
import com.cmatuteortega.monoburro.model.Mode
import com.cmatuteortega.monoburro.model.Profile
import com.cmatuteortega.monoburro.model.Proposal
import com.cmatuteortega.monoburro.model.ShoppingItem
import com.cmatuteortega.monoburro.model.UserPrefs
import kotlinx.serialization.Serializable

@Serializable
enum class Step { SWIPE, RATIOS, PROPOSALS }

/** The main menu's four sections, in bottom-bar order. */
enum class Tab(val emoji: String, val label: String) {
    BURRITOS("🌯", "Burritos"),
    SHOPPING("🛒", "Shopping"),
    GOALS("🎯", "Goals"),
    PROFILE("👤", "Profile"),
}

@Serializable
enum class ThemeMode { SYSTEM, LIGHT, DARK }

@Serializable
enum class Swipe { LIKE, NEVER, FAVORITE }

/** Everything the app needs to survive a restart. */
@Serializable
data class AppState(
    val prefs: UserPrefs = UserPrefs(),
    val step: Step = Step.SWIPE,
    /** Ids in the order they were swiped, for undo. */
    val swipeOrder: List<String> = emptyList(),
    /** The user chose to see the rest of the deck after meeting the quota. */
    val keepSwiping: Boolean = false,
    /** Before the burrito library existed, onboarding ended on a batch plan for this. Migrated on load. */
    val chosen: Proposal? = null,
    /** A burrito was picked: the main menu replaces the onboarding. */
    val onboarded: Boolean = false,
    val burritos: List<Burrito> = emptyList(),
    val shopping: List<ShoppingItem> = emptyList(),
    val profile: Profile = Profile(),
    /** Set by hand on the Goals tab; null means "suggest from the profile". */
    val customGoals: MacroGoals? = null,
    val burritosPerDay: Int = 2,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    /** Picked on the landing screen; null shows the landing. */
    val mode: Mode? = null,
    /** Last subscription status Google Play reported, so Mono survives an offline start. */
    val monoEntitled: Boolean = false,
    /** Debug builds only: a simulated Mono purchase, since sideloaded APKs can't buy from Play. */
    val debugMono: Boolean = false,
)

/**
 * Older versions ended the onboarding on a batch plan for [AppState.chosen].
 * That pick becomes the first burrito in the library and the user lands on
 * the main menu.
 */
fun AppState.migrated(newId: () -> String, now: Long): AppState {
    val legacy = chosen ?: return this
    if (onboarded) return copy(chosen = null)
    val burrito = burritoFrom(legacy, newId(), prefs.burritoCount, prefs.tortilla, now)
    return copy(chosen = null, onboarded = true, burritos = burritos + burrito)
}
