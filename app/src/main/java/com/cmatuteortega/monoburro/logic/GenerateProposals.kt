package com.cmatuteortega.monoburro.logic

import com.cmatuteortega.monoburro.data.INGREDIENTS
import com.cmatuteortega.monoburro.data.TORTILLA
import com.cmatuteortega.monoburro.data.ingredient
import com.cmatuteortega.monoburro.model.Category
import com.cmatuteortega.monoburro.model.Ingredient
import com.cmatuteortega.monoburro.model.Proposal
import com.cmatuteortega.monoburro.model.ProposalItem
import com.cmatuteortega.monoburro.model.ProposalKind
import com.cmatuteortega.monoburro.model.Tag
import com.cmatuteortega.monoburro.model.UserPrefs
import kotlin.math.roundToInt

data class ProposalInput(
    val prefs: UserPrefs,
    val ingredients: List<Ingredient> = INGREDIENTS,
)

/**
 * The seam where an AI call can replace the rule-based logic later: anything
 * that turns preferences into proposals. Implementations should return up to
 * three distinct proposals, fillings in category order and the tortilla last.
 */
fun interface ProposalGenerator {
    fun generate(input: ProposalInput): List<Proposal>
}

fun generateProposals(prefs: UserPrefs): List<Proposal> = RuleBasedProposalGenerator.generate(ProposalInput(prefs))

/** Share of an ingredient's energy that comes from protein (0..1). */
internal fun proteinShare(i: Ingredient): Double = if (i.per100g.kcal > 0) i.per100g.protein * 4 / i.per100g.kcal else 0.0

private const val STEP = 5
private const val FAVORITE_WEIGHT = 3.0
private const val LIKE_WEIGHT = 1.0

/** Penalty for reusing a pick from an earlier proposal: bigger where variety matters most. */
private val REUSE_PENALTY = mapOf(
    Category.PROTEIN to 2.5, Category.SAUCE to 2.5,
    Category.CARB to 0.5, Category.VEG to 0.5, Category.CHEESE to 0.5,
)

private data class Variant(
    val kind: ProposalKind,
    val name: String,
    /** Applied to the user's ratios, then renormalised to 100. */
    val ratioMultiplier: Map<Category, Double>,
    val picks: Map<Category, Int>,
    val bonus: (Ingredient) -> Double,
)

private val VARIANTS = listOf(
    Variant(
        kind = ProposalKind.CLASSIC,
        name = "Classic",
        ratioMultiplier = emptyMap(),
        picks = mapOf(Category.PROTEIN to 1, Category.CARB to 1, Category.VEG to 2, Category.CHEESE to 1, Category.SAUCE to 1),
        bonus = { 0.0 },
    ),
    Variant(
        kind = ProposalKind.HIGH_PROTEIN,
        name = "High protein",
        ratioMultiplier = mapOf(
            Category.PROTEIN to 1.45, Category.CARB to 0.7, Category.VEG to 0.9,
            Category.CHEESE to 0.9, Category.SAUCE to 0.9,
        ),
        picks = mapOf(Category.PROTEIN to 2, Category.CARB to 1, Category.VEG to 2, Category.CHEESE to 1, Category.SAUCE to 1),
        bonus = { proteinShare(it) * 4 },
    ),
    Variant(
        kind = ProposalKind.VEGGIE_FORWARD,
        name = "Veggie-forward",
        ratioMultiplier = mapOf(
            Category.PROTEIN to 0.8, Category.CARB to 0.9, Category.VEG to 1.8,
            Category.CHEESE to 0.8, Category.SAUCE to 1.0,
        ),
        picks = mapOf(Category.PROTEIN to 1, Category.CARB to 1, Category.VEG to 3, Category.CHEESE to 1, Category.SAUCE to 1),
        bonus = { if (it.category == Category.PROTEIN && Tag.VEGETARIAN in it.tags) 2.0 else 0.0 },
    ),
)

/**
 * Deterministic proposals from liked ingredients:
 * - only liked/favorite items that fit the inferred diet are used;
 * - favorites weigh 3x a plain like when picking and get a bigger portion;
 * - each proposal is penalised for reusing earlier proteins and sauces;
 * - filling weight (the tortilla's budget) is split by the user's ratios,
 *   nudged per variant, and a category with nothing liked gives its share away;
 * - an optional kcal or protein target then adjusts the portions (a protein
 *   target also adds a second, lean protein to each proposal).
 */
object RuleBasedProposalGenerator : ProposalGenerator {

    override fun generate(input: ProposalInput): List<Proposal> {
        val prefs = input.prefs
        val diet = inferDiet(prefs, input.ingredients)
        val pools: Map<Category, List<Ingredient>> = Category.FILLINGS.associateWith { c ->
            input.ingredients.filter {
                it.category == c && prefs.likes(it.id) && it.id !in prefs.disliked && diet.allows(it)
            }
        }
        val used = mutableMapOf<Category, MutableSet<String>>()
        return VARIANTS.map { variant ->
            val picks = pick(variant, pools, prefs, used)
            picks.forEach { (c, list) -> used.getOrPut(c) { mutableSetOf() } += list.map { it.id } }
            build(variant, picks, prefs)
        }
    }

    private fun pick(
        variant: Variant,
        pools: Map<Category, List<Ingredient>>,
        prefs: UserPrefs,
        used: Map<Category, Set<String>>,
    ): Map<Category, List<Ingredient>> = Category.FILLINGS.associateWith { c ->
        // With a protein target, every proposal gets a second, lean protein to shift weight into.
        val proteinTarget = prefs.targets.protein != null && c == Category.PROTEIN
        val n = (variant.picks[c] ?: 0).let { if (proteinTarget) maxOf(it, 2) else it }
        pools.getValue(c)
            // sortedByDescending is stable: ties keep deck order.
            .sortedByDescending { ing ->
                val base = if (prefs.isFavorite(ing.id)) FAVORITE_WEIGHT else LIKE_WEIGHT
                val reuse = if (used[c]?.contains(ing.id) == true) REUSE_PENALTY.getValue(c) else 0.0
                val lean = if (proteinTarget) proteinShare(ing) * 4 else 0.0
                base + variant.bonus(ing) + lean - reuse
            }
            .take(n)
    }

    private fun build(variant: Variant, picks: Map<Category, List<Ingredient>>, prefs: UserPrefs): Proposal {
        val budget = prefs.tortilla.fillingBudgetGrams
        val weights = Category.FILLINGS.associateWith { c ->
            if (picks.getValue(c).isEmpty()) 0.0 else prefs.ratios[c] * (variant.ratioMultiplier[c] ?: 1.0)
        }
        val percent = normalize(weights)

        var fillings: List<ProposalItem> = Category.FILLINGS.flatMap { c ->
            val items = picks.getValue(c)
            val categoryGrams = budget * percent.getValue(c) / 100.0
            val portion = items.associateWith { it.defaultGramsPerBurrito * if (prefs.isFavorite(it.id)) 1.5 else 1.0 }
            val portionSum = portion.values.sum()
            items.map { ProposalItem(it.id, roundToStep(categoryGrams * portion.getValue(it) / portionSum)) }
        }
        val tortilla = ProposalItem(TORTILLA.id, prefs.tortilla.grams)

        prefs.targets.kcal?.let { fillings = fitKcal(fillings, tortilla, it) }
        prefs.targets.protein?.let { fillings = fitProtein(fillings, tortilla, it) }

        val items = fillings.filter { it.gramsPerBurrito > 0 } + tortilla
        return Proposal(
            id = "${variant.kind.name.lowercase()}-${items.joinToString(",") { "${it.ingredientId}:${it.gramsPerBurrito}" }.hashCode()}",
            kind = variant.kind,
            name = variant.name,
            tagline = tagline(picks),
            items = items,
            macrosPerBurrito = totalMacros(items),
        )
    }

    /** Scales all fillings so the burrito lands near [target] kcal (within 60–140 % of the planned size). */
    private fun fitKcal(fillings: List<ProposalItem>, tortilla: ProposalItem, target: Int): List<ProposalItem> {
        val fillingKcal = totalMacros(fillings).kcal
        if (fillingKcal <= 0) return fillings
        val factor = ((target - totalMacros(listOf(tortilla)).kcal) / fillingKcal).coerceIn(0.6, 1.4)
        return fillings.map { it.copy(gramsPerBurrito = roundToStep(it.gramsPerBurrito * factor)) }
    }

    /**
     * Moves weight from carbs (then veg) to the leanest protein, 5 g at a
     * time, until the burrito reaches [target] g protein. Total weight stays
     * the same; carbs and veg keep at least 15 g per item.
     */
    private fun fitProtein(fillings: List<ProposalItem>, tortilla: ProposalItem, target: Int): List<ProposalItem> {
        val items = fillings.toMutableList()
        val lookup = ::ingredient
        val recipient = items.indices
            .filter { lookup(items[it].ingredientId).category == Category.PROTEIN }
            .maxByOrNull { proteinShare(lookup(items[it].ingredientId)) } ?: return fillings
        repeat(60) {
            if (totalMacros(items + tortilla).protein >= target) return items
            val donor = listOf(Category.CARB, Category.VEG).firstNotNullOfOrNull { c ->
                items.indices
                    .filter { lookup(items[it].ingredientId).category == c && items[it].gramsPerBurrito > 15 }
                    .maxByOrNull { items[it].gramsPerBurrito }
            } ?: return items
            items[donor] = items[donor].copy(gramsPerBurrito = items[donor].gramsPerBurrito - STEP)
            items[recipient] = items[recipient].copy(gramsPerBurrito = items[recipient].gramsPerBurrito + STEP)
        }
        return items
    }

    private fun tagline(picks: Map<Category, List<Ingredient>>): String {
        val proteins = picks.getValue(Category.PROTEIN).map { it.name }
        val sauce = picks.getValue(Category.SAUCE).firstOrNull()?.name?.lowercase()
        val head = proteins.joinToString(" & ").ifEmpty { picks.getValue(Category.VEG).joinToString(" & ") { it.name } }
        return listOfNotNull(head.ifEmpty { null }, sauce).joinToString(" with ")
    }

    private fun roundToStep(grams: Double): Int =
        if (grams <= 0) 0 else ((grams / STEP).roundToInt() * STEP).coerceAtLeast(STEP)
}
