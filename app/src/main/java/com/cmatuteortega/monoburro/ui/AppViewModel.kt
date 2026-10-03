package com.cmatuteortega.monoburro.ui

import android.app.Activity
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.cmatuteortega.monoburro.BuildConfig
import com.cmatuteortega.monoburro.billing.BillingState
import com.cmatuteortega.monoburro.billing.MonoBilling
import com.cmatuteortega.monoburro.data.ingredient
import com.cmatuteortega.monoburro.logic.addCustomShopping
import com.cmatuteortega.monoburro.logic.addToShopping
import com.cmatuteortega.monoburro.logic.burritoFrom
import com.cmatuteortega.monoburro.logic.emptyBurrito
import com.cmatuteortega.monoburro.logic.rebalance
import com.cmatuteortega.monoburro.logic.suggestedGoals
import com.cmatuteortega.monoburro.logic.with
import com.cmatuteortega.monoburro.logic.withItemGrams
import com.cmatuteortega.monoburro.logic.withRatios
import com.cmatuteortega.monoburro.logic.withTortilla
import com.cmatuteortega.monoburro.logic.without
import com.cmatuteortega.monoburro.model.Burrito
import com.cmatuteortega.monoburro.model.Category
import com.cmatuteortega.monoburro.model.Goal
import com.cmatuteortega.monoburro.model.MacroGoals
import com.cmatuteortega.monoburro.model.Mode
import com.cmatuteortega.monoburro.model.Profile
import com.cmatuteortega.monoburro.model.Proposal
import com.cmatuteortega.monoburro.model.Ratios
import com.cmatuteortega.monoburro.model.Targets
import com.cmatuteortega.monoburro.model.TortillaSize
import com.cmatuteortega.monoburro.model.UserPrefs
import com.cmatuteortega.monoburro.model.can
import com.cmatuteortega.monoburro.model.effectiveGoal
import com.cmatuteortega.monoburro.model.effectiveMode
import com.cmatuteortega.monoburro.storage.AppState
import com.cmatuteortega.monoburro.storage.StateStore
import com.cmatuteortega.monoburro.storage.Step
import com.cmatuteortega.monoburro.storage.Swipe
import com.cmatuteortega.monoburro.storage.Tab
import com.cmatuteortega.monoburro.storage.ThemeMode
import com.cmatuteortega.monoburro.storage.migrated
import com.cmatuteortega.monoburro.ui.screens.BurritoEditor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

/** Where the user is in the main menu. Not persisted: a cold start opens the burrito list. */
data class Nav(
    val tab: Tab = Tab.BURRITOS,
    /** A burrito opened from the list. */
    val burritoId: String? = null,
    /** "New burrito → from your tastes": the three proposals. */
    val suggesting: Boolean = false,
)

class AppViewModel(app: Application) : AndroidViewModel(app), BurritoEditor {
    private val store = StateStore(app)
    private val _state = MutableStateFlow(store.load().migrated(::newId, System.currentTimeMillis()))
    val state: StateFlow<AppState> = _state.asStateFlow()

    private val _nav = MutableStateFlow(Nav())
    val nav: StateFlow<Nav> = _nav.asStateFlow()

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
        store.save(_state.value)
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
        }
        goTo(prev)
        return true
    }

    fun setRatio(category: Category, value: Int) = updatePrefs { it.copy(ratios = rebalance(it.ratios, category, value)) }
    fun resetRatios() = updatePrefs { it.copy(ratios = Ratios.DEFAULT) }
    fun setBurritoCount(n: Int) = updatePrefs { it.copy(burritoCount = n.coerceIn(1, MAX_BURRITOS)) }
    fun setTortilla(size: TortillaSize) = updatePrefs { it.copy(tortilla = size) }
    fun setTargets(targets: Targets) = updatePrefs { it.copy(targets = targets) }

    /** Onboarding is done once a burrito is picked: it joins the library and the main menu opens on it. */
    fun choose(proposal: Proposal) {
        val burrito = newBurrito(proposal)
        update { it.copy(onboarded = true, step = Step.SWIPE, burritos = it.burritos + burrito) }
        _nav.value = Nav(tab = Tab.BURRITOS, burritoId = burrito.id)
    }

    // --- Main menu navigation ---

    fun openTab(tab: Tab) { _nav.value = Nav(tab = tab) }
    fun openBurrito(id: String) { _nav.value = Nav(tab = Tab.BURRITOS, burritoId = id) }
    fun openSuggestions() { _nav.value = Nav(tab = Tab.BURRITOS, suggesting = true) }

    /** Returns false at the top level (let the system handle back). */
    fun navBack(): Boolean {
        val n = _nav.value
        _nav.value = when {
            n.burritoId != null || n.suggesting -> Nav(tab = n.tab)
            n.tab != Tab.BURRITOS -> Nav()
            else -> return false
        }
        return true
    }

    // --- Burrito library ---

    /** From the main menu's suggestions: saved, then opened. */
    fun addBurrito(proposal: Proposal) {
        val burrito = newBurrito(proposal)
        update { it.copy(burritos = it.burritos + burrito) }
        openBurrito(burrito.id)
    }

    /** "Build your own": an empty tortilla to fill by hand. */
    fun addEmptyBurrito() {
        val s = _state.value
        val burrito = emptyBurrito(
            newId(), "My burrito #${s.burritos.size + 1}", s.prefs.burritoCount, s.prefs.tortilla, System.currentTimeMillis(),
        )
        update { it.copy(burritos = it.burritos + burrito) }
        openBurrito(burrito.id)
    }

    override fun duplicateBurrito(id: String) {
        val original = _state.value.burritos.firstOrNull { it.id == id } ?: return
        val copy = original.copy(id = newId(), name = "${original.name} (copy)", createdAt = System.currentTimeMillis())
        update { it.copy(burritos = it.burritos + copy) }
        openBurrito(copy.id)
    }

    override fun deleteBurrito(id: String) {
        update { s -> s.copy(burritos = s.burritos.filterNot { it.id == id }) }
        if (_nav.value.burritoId == id) _nav.value = Nav()
    }

    override fun renameBurrito(id: String, name: String) = updateBurrito(id) { it.copy(name = name.take(MAX_NAME)) }
    override fun setBurritoEmoji(id: String, emoji: String) = updateBurrito(id) { it.copy(emoji = emoji) }
    override fun setBurritoCount(id: String, n: Int) = updateBurrito(id) { it.copy(count = n.coerceIn(1, MAX_BURRITOS)) }
    override fun setBurritoTortilla(id: String, size: TortillaSize) = updateBurrito(id) { it.withTortilla(size) }
    override fun setBurritoRatios(id: String, ratios: Ratios) = updateBurrito(id) { it.withRatios(ratios) }
    override fun setItemGrams(id: String, ingredientId: String, grams: Int) = updateBurrito(id) { it.withItemGrams(ingredientId, grams) }
    override fun removeItem(id: String, ingredientId: String) = updateBurrito(id) { it.without(ingredientId) }
    override fun addItem(id: String, ingredientId: String) = updateBurrito(id) { it.with(ingredient(ingredientId)) }

    private fun updateBurrito(id: String, f: (Burrito) -> Burrito) = update { s ->
        s.copy(burritos = s.burritos.map { if (it.id == id) f(it) else it })
    }

    private fun newBurrito(proposal: Proposal): Burrito {
        val p = _state.value.prefs
        return burritoFrom(proposal, newId(), p.burritoCount, p.tortilla, System.currentTimeMillis())
    }

    // --- Shopping list ---

    /** The whole batch, or just [ingredientId]. */
    override fun addToShopping(burritoId: String, ingredientId: String?) {
        val burrito = _state.value.burritos.firstOrNull { it.id == burritoId } ?: return
        update { it.copy(shopping = addToShopping(it.shopping, burrito, ::newId, only = ingredientId)) }
        _notice.value = if (ingredientId == null) {
            "${burrito.name} × ${burrito.count} added to your shopping list 🛒"
        } else {
            "${ingredient(ingredientId).name} added to your shopping list 🛒"
        }
    }

    fun addCustomItem(name: String) = update { it.copy(shopping = addCustomShopping(it.shopping, name, ::newId)) }
    fun toggleShopping(id: String) = update { s ->
        s.copy(shopping = s.shopping.map { if (it.id == id) it.copy(checked = !it.checked) else it })
    }
    fun removeShopping(id: String) = update { s -> s.copy(shopping = s.shopping.filterNot { it.id == id }) }
    fun clearChecked() = update { s -> s.copy(shopping = s.shopping.filterNot { it.checked }) }
    fun clearShopping() = update { it.copy(shopping = emptyList()) }

    // --- Goals and profile ---

    fun updateProfile(f: (Profile) -> Profile) = update { it.copy(profile = f(it.profile)) }

    /** Bulk and Cut are Mono-only: returns false when the paywall should open instead. */
    fun setGoal(goal: Goal): Boolean {
        val mode = _state.value.mode ?: Mode.BURRO
        if (!mode.can(goal)) return false
        updateProfile { it.copy(goal = goal) }
        return true
    }

    fun setCustomGoals(goals: MacroGoals?) = update { it.copy(customGoals = goals) }
    fun setBurritosPerDay(n: Int) = update { it.copy(burritosPerDay = n.coerceIn(1, MAX_PER_DAY)) }

    /** New suggestions aim at a slice of the daily calories: daily kcal / burritos per day. */
    fun useGoalsForSuggestions() {
        val s = _state.value
        val perBurrito = s.dailyGoals.kcal / s.burritosPerDay
        updatePrefs { it.copy(targets = Targets(kcal = perBurrito)) }
        _notice.value = "New suggestions will aim for $perBurrito kcal per burrito 🎯"
    }

    fun setTheme(mode: ThemeMode) = update { it.copy(themeMode = mode) }

    /**
     * Back to the taste quiz with fresh swipes and ratios. The burritos, the
     * shopping list, the goals and the profile stay.
     */
    fun restart() {
        update { it.copy(prefs = UserPrefs(), step = Step.SWIPE, swipeOrder = emptyList(), keepSwiping = false, onboarded = false) }
        _nav.value = Nav()
    }

    /** Leave a redone quiz half-way: back to the main menu (only when there are burritos to go back to). */
    fun backToMenu() {
        if (_state.value.burritos.isEmpty()) return
        update { it.copy(onboarded = true, step = Step.SWIPE) }
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

    private fun newId(): String = UUID.randomUUID().toString()

    companion object {
        const val MAX_BURRITOS = 60
        const val MAX_PER_DAY = 6
        const val MAX_NAME = 40
    }
}

/** The goal in use: Bulk and Cut need Mono, so a lapsed subscription runs as Eat. */
val AppState.goal: Goal get() = effectiveGoal(profile.goal, mode ?: Mode.BURRO)

/** Daily goals in use: set by hand, else suggested from the profile, else a sensible default. */
val AppState.dailyGoals: MacroGoals get() = customGoals ?: suggestedGoals(profile, goal) ?: MacroGoals.DEFAULT

/** Mono is unlocked by Google Play, or by a simulated purchase on debug builds. */
val AppState.entitled: Boolean get() = monoEntitled || (BuildConfig.DEBUG && debugMono)

private fun UserPrefs.withoutDecision(id: String) =
    copy(liked = liked - id, favorites = favorites - id, disliked = disliked - id)
