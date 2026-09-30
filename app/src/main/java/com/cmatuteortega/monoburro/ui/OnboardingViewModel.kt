package com.cmatuteortega.monoburro.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.cmatuteortega.monoburro.logic.rebalance
import com.cmatuteortega.monoburro.model.Category
import com.cmatuteortega.monoburro.model.Proposal
import com.cmatuteortega.monoburro.model.Ratios
import com.cmatuteortega.monoburro.model.Targets
import com.cmatuteortega.monoburro.model.TortillaSize
import com.cmatuteortega.monoburro.model.UserPrefs
import com.cmatuteortega.monoburro.storage.AppState
import com.cmatuteortega.monoburro.storage.StateStore
import com.cmatuteortega.monoburro.storage.Step
import com.cmatuteortega.monoburro.storage.Swipe
import com.cmatuteortega.monoburro.storage.ThemeMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class OnboardingViewModel(app: Application) : AndroidViewModel(app) {
    private val store = StateStore(app)
    private val _state = MutableStateFlow(store.load())
    val state: StateFlow<AppState> = _state.asStateFlow()

    private fun update(f: (AppState) -> AppState) {
        val next = f(_state.value)
        _state.value = next
        store.save(next)
    }

    private fun updatePrefs(f: (UserPrefs) -> UserPrefs) = update { it.copy(prefs = f(it.prefs)) }

    fun swipe(id: String, swipe: Swipe) = update { s ->
        val p = s.prefs.withoutDecision(id)
        val prefs = when (swipe) {
            Swipe.LIKE -> p.copy(liked = p.liked + id)
            Swipe.NEVER -> p.copy(disliked = p.disliked + id)
            Swipe.FAVORITE -> p.copy(favorites = p.favorites + id)
        }
        s.copy(prefs = prefs, swipeOrder = s.swipeOrder - id + id)
    }

    fun undo() = update { s ->
        val last = s.swipeOrder.lastOrNull() ?: return@update s
        s.copy(prefs = s.prefs.withoutDecision(last), swipeOrder = s.swipeOrder.dropLast(1))
    }

    fun keepSwiping() = update { it.copy(keepSwiping = true) }

    fun goTo(step: Step) = update { it.copy(step = step) }

    /** Returns false when already at the first step (let the system handle back). */
    fun back(): Boolean {
        val prev = when (_state.value.step) {
            Step.SWIPE -> return false
            Step.RATIOS -> Step.SWIPE
            Step.PROPOSALS -> Step.RATIOS
            Step.BATCH -> Step.PROPOSALS
        }
        goTo(prev)
        return true
    }

    fun setRatio(category: Category, value: Int) = updatePrefs { it.copy(ratios = rebalance(it.ratios, category, value)) }
    fun resetRatios() = updatePrefs { it.copy(ratios = Ratios.DEFAULT) }
    fun setBurritoCount(n: Int) = updatePrefs { it.copy(burritoCount = n.coerceIn(1, MAX_BURRITOS)) }
    fun setTortilla(size: TortillaSize) = updatePrefs { it.copy(tortilla = size) }
    fun setTargets(targets: Targets) = updatePrefs { it.copy(targets = targets) }

    fun choose(proposal: Proposal) = update { it.copy(chosen = proposal, step = Step.BATCH) }

    fun cycleTheme() = update {
        it.copy(themeMode = ThemeMode.entries[(it.themeMode.ordinal + 1) % ThemeMode.entries.size])
    }

    /** Wipes all answers but keeps the theme choice. */
    fun restart() = update { AppState(themeMode = it.themeMode) }

    companion object {
        const val MAX_BURRITOS = 60
    }
}

private fun UserPrefs.withoutDecision(id: String) =
    copy(liked = liked - id, favorites = favorites - id, disliked = disliked - id)
