package com.cmatuteortega.monoburro.ui

import android.app.Activity
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.cmatuteortega.monoburro.BuildConfig
import com.cmatuteortega.monoburro.billing.BillingState
import com.cmatuteortega.monoburro.billing.MonoBilling
import com.cmatuteortega.monoburro.logic.rebalance
import com.cmatuteortega.monoburro.model.Category
import com.cmatuteortega.monoburro.model.Mode
import com.cmatuteortega.monoburro.model.Proposal
import com.cmatuteortega.monoburro.model.Ratios
import com.cmatuteortega.monoburro.model.Targets
import com.cmatuteortega.monoburro.model.TortillaSize
import com.cmatuteortega.monoburro.model.UserPrefs
import com.cmatuteortega.monoburro.model.effectiveMode
import com.cmatuteortega.monoburro.storage.AppState
import com.cmatuteortega.monoburro.storage.StateStore
import com.cmatuteortega.monoburro.storage.Step
import com.cmatuteortega.monoburro.storage.Swipe
import com.cmatuteortega.monoburro.storage.ThemeMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class OnboardingViewModel(app: Application) : AndroidViewModel(app) {
    private val store = StateStore(app)
    private val _state = MutableStateFlow(store.load())
    val state: StateFlow<AppState> = _state.asStateFlow()

    private val billing = MonoBilling(app)
    val billingState: StateFlow<BillingState> = billing.state

    /** One-off messages for the snackbar (e.g. Mono ended). */
    private val _notice = MutableStateFlow<String?>(null)
    val notice: StateFlow<String?> = _notice.asStateFlow()

    /** The user asked for Mono and is in the purchase flow: switch as soon as Play confirms. */
    private var wantsMono = false

    init {
        viewModelScope.launch {
            billing.state.collect { b ->
                val subscribed = b.subscribed ?: return@collect
                update { it.copy(monoEntitled = subscribed) }
                if (subscribed && wantsMono) {
                    wantsMono = false
                    update { it.copy(mode = Mode.MONO) }
                }
            }
        }
        billing.refresh()
    }

    private fun update(f: (AppState) -> AppState) {
        val next = f(_state.value).let { s ->
            // Mono without a subscription (expired, refunded, cancelled) drops to Burro.
            val mode = effectiveMode(s.mode, s.entitled)
            if (mode != s.mode) _notice.value = "Your Mono subscription is no longer active. You're a Burro now 🫏"
            s.copy(mode = mode)
        }
        if (next == _state.value) return
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

    /** Wipes all answers but keeps the theme, the mode and the subscription. */
    fun restart() = update {
        AppState(themeMode = it.themeMode, mode = it.mode, monoEntitled = it.monoEntitled, debugMono = it.debugMono)
    }

    /** Burro is free: pick it straight away. Mono needs the subscription first. */
    fun chooseBurro() = update { it.copy(mode = Mode.BURRO) }

    /** Returns true when Mono was selected directly (already subscribed), false when a paywall is needed. */
    fun chooseMono(): Boolean {
        if (!_state.value.entitled) return false
        update { it.copy(mode = Mode.MONO) }
        return true
    }

    fun subscribe(activity: Activity) {
        wantsMono = true
        billing.purchase(activity)
    }

    /** Debug builds only: unlock Mono without Google Play. */
    fun simulateMono() {
        if (!BuildConfig.DEBUG) return
        update { it.copy(debugMono = true, mode = Mode.MONO) }
    }

    /** Debug builds only: end the simulated subscription, to try the downgrade. */
    fun endSimulatedMono() = update { it.copy(debugMono = false) }

    fun refreshBilling() = billing.refresh()
    fun clearBillingMessage() = billing.clearMessage()
    fun clearNotice() { _notice.value = null }

    /** The user closed the paywall: don't switch to Mono behind their back later. */
    fun cancelMono() { wantsMono = false }

    override fun onCleared() {
        billing.close()
    }

    companion object {
        const val MAX_BURRITOS = 60
    }
}

/** Mono is unlocked by Google Play, or by a simulated purchase on debug builds. */
val AppState.entitled: Boolean get() = monoEntitled || (BuildConfig.DEBUG && debugMono)

private fun UserPrefs.withoutDecision(id: String) =
    copy(liked = liked - id, favorites = favorites - id, disliked = disliked - id)
