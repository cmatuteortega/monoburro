package com.cmatuteortega.monoburro.data

import com.cmatuteortega.monoburro.model.Category
import com.cmatuteortega.monoburro.model.Category.CARB
import com.cmatuteortega.monoburro.model.Category.CHEESE
import com.cmatuteortega.monoburro.model.Category.PROTEIN
import com.cmatuteortega.monoburro.model.Category.SAUCE
import com.cmatuteortega.monoburro.model.Category.VEG
import com.cmatuteortega.monoburro.model.FriendlyUnit
import com.cmatuteortega.monoburro.model.Ingredient
import com.cmatuteortega.monoburro.model.Macros
import com.cmatuteortega.monoburro.model.Tag
import com.cmatuteortega.monoburro.model.Tag.CONTAINS_DAIRY
import com.cmatuteortega.monoburro.model.Tag.CONTAINS_EGG
import com.cmatuteortega.monoburro.model.Tag.CONTAINS_GLUTEN
import com.cmatuteortega.monoburro.model.Tag.MEAT
import com.cmatuteortega.monoburro.model.Tag.SEAFOOD
import com.cmatuteortega.monoburro.model.Tag.SPICY
import com.cmatuteortega.monoburro.model.Tag.VEGAN
import com.cmatuteortega.monoburro.model.Tag.VEGETARIAN

private val PLANT = arrayOf(VEGAN, VEGETARIAN)

@Suppress("LongParameterList")
private fun ing(
    id: String, name: String, category: Category, emoji: String,
    kcal: Double, protein: Double, carbs: Double, fat: Double,
    grams: Int, vararg tags: Tag, unit: FriendlyUnit? = null,
) = Ingredient(id, name, category, emoji, Macros(kcal, protein, carbs, fat), grams, tags.toSet(), unit)

private fun u(singular: String, plural: String, grams: Double) = FriendlyUnit(singular, plural, grams)

/**
 * The swipe deck: 40 fillings. Macros are per 100 g as cooked / ready to use
 * (USDA-style reference values, rounded). Order within a category is roughly
 * "most classic first", which is also the tie-break when picking.
 */
val INGREDIENTS: List<Ingredient> = listOf(
    // Protein
    ing("grilled-chicken", "Grilled chicken", PROTEIN, "🍗", 165.0, 31.0, 0.0, 3.6, 90, MEAT),
    ing("chicken-tinga", "Chicken tinga", PROTEIN, "🌶️", 150.0, 21.0, 4.0, 5.5, 90, MEAT, SPICY),
    ing("carnitas", "Pork carnitas", PROTEIN, "🐖", 230.0, 25.0, 0.5, 14.0, 85, MEAT),
    ing("barbacoa", "Beef barbacoa", PROTEIN, "🐄", 205.0, 26.0, 1.5, 10.5, 85, MEAT),
    ing("ground-beef", "Seasoned ground beef", PROTEIN, "🥩", 250.0, 24.0, 2.0, 16.0, 80, MEAT),
    ing("chorizo", "Chorizo", PROTEIN, "🌭", 455.0, 24.0, 2.0, 38.0, 40, MEAT, SPICY),
    ing("garlic-shrimp", "Garlic shrimp", PROTEIN, "🦐", 120.0, 23.0, 1.0, 2.5, 85, SEAFOOD),
    ing("black-beans", "Black beans", PROTEIN, "🥫", 132.0, 8.9, 23.7, 0.5, 70, *PLANT,
        unit = u("can (drained)", "cans (drained)", 240.0)),
    ing("refried-beans", "Refried pinto beans", PROTEIN, "🥣", 91.0, 5.4, 15.0, 1.2, 70, *PLANT,
        unit = u("can", "cans", 450.0)),
    ing("tofu-sofritas", "Tofu sofritas", PROTEIN, "🌱", 150.0, 12.0, 5.0, 9.0, 90, *PLANT, SPICY,
        unit = u("block of tofu", "blocks of tofu", 400.0)),
    ing("tempeh", "Smoky tempeh", PROTEIN, "🟫", 192.0, 20.0, 7.6, 11.0, 80, *PLANT,
        unit = u("pack", "packs", 200.0)),
    ing("scrambled-egg", "Scrambled egg", PROTEIN, "🥚", 149.0, 10.0, 1.6, 11.0, 80, VEGETARIAN, CONTAINS_EGG,
        unit = u("egg", "eggs", 50.0)),

    // Carb base
    ing("cilantro-lime-rice", "Cilantro-lime rice", CARB, "🍚", 140.0, 2.7, 28.0, 2.0, 80, *PLANT,
        unit = u("cup cooked", "cups cooked", 185.0)),
    ing("brown-rice", "Brown rice", CARB, "🌾", 123.0, 2.7, 25.6, 1.0, 80, *PLANT,
        unit = u("cup cooked", "cups cooked", 195.0)),
    ing("spanish-rice", "Spanish tomato rice", CARB, "🍅", 150.0, 3.0, 28.0, 3.0, 80, *PLANT,
        unit = u("cup cooked", "cups cooked", 190.0)),
    ing("roast-potatoes", "Roasted potatoes", CARB, "🥔", 145.0, 2.5, 22.0, 5.0, 80, *PLANT,
        unit = u("medium potato", "medium potatoes", 170.0)),
    ing("quinoa", "Quinoa", CARB, "🥗", 120.0, 4.4, 21.3, 1.9, 75, *PLANT,
        unit = u("cup cooked", "cups cooked", 185.0)),
    ing("sweet-potato", "Roasted sweet potato", CARB, "🍠", 90.0, 2.0, 20.7, 0.2, 80, *PLANT,
        unit = u("sweet potato", "sweet potatoes", 200.0)),

    // Veg
    ing("fajita-veg", "Fajita peppers & onions", VEG, "🥘", 60.0, 1.2, 7.0, 3.0, 40, *PLANT,
        unit = u("bell pepper", "bell peppers", 120.0)),
    ing("pico-de-gallo", "Pico de gallo", VEG, "🍅", 20.0, 0.8, 4.5, 0.1, 30, *PLANT,
        unit = u("tomato", "tomatoes", 120.0)),
    ing("roasted-corn", "Roasted corn", VEG, "🌽", 96.0, 3.4, 21.0, 1.5, 30, *PLANT,
        unit = u("cup", "cups", 150.0)),
    ing("lettuce", "Shredded lettuce", VEG, "🥬", 15.0, 1.4, 2.9, 0.2, 15, *PLANT,
        unit = u("cup", "cups", 47.0)),
    ing("pickled-onion", "Pickled red onion", VEG, "🧅", 40.0, 0.9, 9.0, 0.1, 15, *PLANT,
        unit = u("red onion", "red onions", 150.0)),
    ing("jalapenos", "Jalapeños", VEG, "🌶️", 29.0, 0.9, 6.5, 0.4, 10, *PLANT, SPICY,
        unit = u("jalapeño", "jalapeños", 14.0)),
    ing("zucchini", "Grilled zucchini", VEG, "🥒", 35.0, 1.2, 3.5, 2.0, 35, *PLANT,
        unit = u("zucchini", "zucchini", 200.0)),
    ing("spinach", "Garlicky spinach", VEG, "🍃", 35.0, 3.0, 3.8, 1.5, 25, *PLANT,
        unit = u("bag of fresh spinach", "bags of fresh spinach", 100.0)),
    ing("mushrooms", "Sautéed mushrooms", VEG, "🍄", 45.0, 3.0, 4.0, 2.5, 30, *PLANT,
        unit = u("punnet (250 g raw)", "punnets (250 g raw)", 160.0)),
    ing("cabbage-slaw", "Red cabbage slaw", VEG, "🥗", 25.0, 1.3, 5.8, 0.1, 25, *PLANT,
        unit = u("cup", "cups", 90.0)),

    // Cheese
    ing("cheddar", "Sharp cheddar", CHEESE, "🧀", 403.0, 25.0, 1.3, 33.0, 25, VEGETARIAN, CONTAINS_DAIRY,
        unit = u("cup shredded", "cups shredded", 113.0)),
    ing("monterey-jack", "Monterey Jack", CHEESE, "🟨", 373.0, 24.0, 0.7, 30.0, 25, VEGETARIAN, CONTAINS_DAIRY,
        unit = u("cup shredded", "cups shredded", 113.0)),
    ing("queso-fresco", "Queso fresco", CHEESE, "🤍", 299.0, 18.0, 3.0, 24.0, 25, VEGETARIAN, CONTAINS_DAIRY,
        unit = u("cup crumbled", "cups crumbled", 120.0)),
    ing("cotija", "Cotija", CHEESE, "🧂", 366.0, 20.0, 4.0, 30.0, 15, VEGETARIAN, CONTAINS_DAIRY,
        unit = u("cup crumbled", "cups crumbled", 120.0)),
    ing("vegan-cheese", "Vegan cheese shreds", CHEESE, "🌿", 280.0, 1.0, 23.0, 20.0, 25, *PLANT,
        unit = u("cup shredded", "cups shredded", 110.0)),

    // Sauce & extras
    ing("guacamole", "Guacamole", SAUCE, "🥑", 155.0, 2.0, 8.5, 14.0, 30, *PLANT,
        unit = u("avocado", "avocados", 150.0)),
    ing("sour-cream", "Sour cream", SAUCE, "🥛", 198.0, 2.4, 4.6, 19.0, 20, VEGETARIAN, CONTAINS_DAIRY,
        unit = u("tub (250 g)", "tubs (250 g)", 250.0)),
    ing("salsa-roja", "Salsa roja", SAUCE, "🔥", 36.0, 1.5, 7.0, 0.2, 25, *PLANT, SPICY,
        unit = u("jar (450 g)", "jars (450 g)", 450.0)),
    ing("salsa-verde", "Salsa verde", SAUCE, "🟢", 30.0, 1.0, 6.0, 0.5, 25, *PLANT, SPICY,
        unit = u("jar (450 g)", "jars (450 g)", 450.0)),
    ing("chipotle-crema", "Chipotle crema", SAUCE, "🌋", 180.0, 2.0, 4.0, 18.0, 20, VEGETARIAN, CONTAINS_DAIRY, SPICY),
    ing("greek-yogurt", "Lime Greek yogurt", SAUCE, "🍋", 97.0, 9.0, 3.9, 5.0, 25, VEGETARIAN, CONTAINS_DAIRY,
        unit = u("tub (500 g)", "tubs (500 g)", 500.0)),
    ing("cashew-crema", "Cashew crema", SAUCE, "🥜", 250.0, 7.0, 12.0, 20.0, 20, *PLANT),
)

/** The fixed wrap (large flour tortilla, 70 g). Other sizes: [com.cmatuteortega.monoburro.model.TortillaSize]. */
val TORTILLA: Ingredient = ing(
    "flour-tortilla", "Flour tortilla", Category.TORTILLA, "🌯",
    304.0, 8.1, 50.0, 7.7, 70, VEGAN, VEGETARIAN, CONTAINS_GLUTEN,
)

val ALL_INGREDIENTS: List<Ingredient> = INGREDIENTS + TORTILLA

private val BY_ID: Map<String, Ingredient> = ALL_INGREDIENTS.associateBy { it.id }

fun ingredientOrNull(id: String): Ingredient? = BY_ID[id]

fun ingredient(id: String): Ingredient = BY_ID[id] ?: error("Unknown ingredient: $id")
