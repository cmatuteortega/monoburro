package com.cmatuteortega.monoburro.storage

import com.cmatuteortega.monoburro.model.Proposal
import com.cmatuteortega.monoburro.model.UserPrefs
import kotlinx.serialization.Serializable

@Serializable
enum class Step { SWIPE, RATIOS, PROPOSALS, BATCH }

@Serializable
enum class ThemeMode { SYSTEM, LIGHT, DARK }

@Serializable
enum class Swipe { LIKE, NEVER, FAVORITE }

/** Everything the onboarding needs to survive a restart. */
@Serializable
data class AppState(
    val prefs: UserPrefs = UserPrefs(),
    val step: Step = Step.SWIPE,
    /** Ids in the order they were swiped, for undo. */
    val swipeOrder: List<String> = emptyList(),
    /** The user chose to see the rest of the deck after meeting the quota. */
    val keepSwiping: Boolean = false,
    val chosen: Proposal? = null,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
)
