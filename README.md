# Monoburro 🌯

AI-assisted batch cooking, burritos only. Native Android (Kotlin + Jetpack
Compose + Material 3), no backend or accounts: everything stays on the phone
(SharedPreferences, as JSON).

On launch, a landing screen pitches the app (and the case for eating nothing
but burritos) and asks you to pick a mode:

- **Mono 🐒** (left): premium, a €5/month Google Play subscription that pays for
  the AI features (AI burrito chef, macro coach, smart shopping list, remixes).
- **Burro 🫏** (right): free, the basics.

The chosen mode sits top right for the rest of the app (a gold *Mono* pill, or
a *Go Mono* pill for Burros); tapping it opens
the Mono paywall (Burro) or the subscription sheet (Mono). For now every feature
is built for both modes: `model/Mode.kt` has the `Feature` list and
`Mode.can(feature)`; flip a feature's `monoOnly` to gate it. If a Mono
subscription lapses, the app drops back to Burro.

Then the onboarding:

1. **Your tastes**: Tinder-style cards for 40 fillings, grouped by category.
   Swipe → like, ← never, ↑ favourite (or use the buttons; ↶ undoes). A
   category is skipped once it has enough likes (3 proteins, 1 carb base,
   3 veg, 1 cheese, 1 sauce); the deck stops when all are met or it runs out,
   with an option to keep swiping.
2. **Your ratios**: five sliders (protein / carb base / veg / cheese / sauce as a
   share of filling weight, default 35/30/20/10/5) that always sum to 100,
   burrito count (default 12), tortilla size, and an optional kcal or protein
   target per burrito.
3. **Pick a burrito**: three proposals (Classic, High protein, Veggie-forward)
   with ingredients, grams and macros per burrito. Picking one saves it and
   opens the main menu on it.

## Main menu

Four tabs on a bottom bar:

- **🌯 Burritos**: the saved burritos. *New burrito* makes another one, either
  from your tastes (the three proposals again) or from scratch (an empty
  tortilla). Opening one shows its macros (and its share of your daily goal),
  what's inside per burrito and for the whole batch (grams + friendly unit),
  and lets you change how many, the tortilla size (filling scales to fit), the
  proportions (same rebalancing sliders as the onboarding, keeping the total
  filling weight), the grams of each ingredient (±5 g), add or remove
  ingredients, rename, change its emoji, duplicate or delete it. 🛒 on an
  ingredient, or *Add all to shopping list*, sends it to the list.
- **🛒 Shopping**: everything added from burritos, grouped by aisle. Adding
  the same thing again merges (amounts add up, "For …" lists the burritos);
  ticked items count as bought, so they don't merge. Type in extras by hand.
- **🎯 Goals**: daily kcal / protein / carbs / fat, suggested from the profile
  (Mifflin–St Jeor × activity; Bulk +10 %, Cut −20 %, Eat maintenance) or set
  by hand; burritos per day, the per-burrito budget (and a button to aim new
  proposals at it), and how each saved burrito fits.
- **👤 Profile**: mode, goal, age, weight, height, sex, activity, favourite
  food (and your ⭐ fillings), appearance (auto / light / dark) and *Redo the
  taste quiz*.

The goal is **Eat** by default, and it's the only one in Burro mode: **Bulk**
and **Cut** are Mono features (`Feature.BULK_CUT`). If Mono lapses, a Bulk or
Cut pick runs as Eat until it comes back.

"Redo taste quiz" (⋮ menu or Profile) clears swipes and ratios only; burritos,
shopping list, goals and profile stay. Saved
state from the old four-step onboarding (which ended on a batch plan) is
migrated: that burrito becomes the first one in the library.

## Design

`ui/theme/Theme.kt` holds the tokens: the warm taqueria palette (light and
dark), one colour per filling category and per macro (kcal salsa, protein
avocado, carbs gold, fat plum, the same everywhere), shapes, and the type
scale. Headlines, titles and the big numbers use **Bricolage Grotesque**
(`res/font/`, SIL OFL, see `licenses/`); body text stays on the system font.
`ui/Components.kt` has the shared blocks every screen is built from: cards,
section headers, `MacroStats` / `MacroLine`, `CompositionBar`, `EmojiTile`,
`Pill`, `ChoiceRow`, `Stepper`, `EmptyState` and the pinned `BottomAction`.

Tabs get a large title with the mode pill; screens opened on top (a burrito,
the proposals) get a back arrow instead of the bottom bar.

## Billing (Mono subscription)

`billing/MonoBilling.kt` uses Google Play Billing Library 8. To sell Mono, in the
Play Console create a subscription with product id **`mono_monthly`** and an
auto-renewing monthly base plan at **€5.00** (let Play set the other countries).
The paywall shows Play's localized price, with "€5" until it loads.

There's no backend: entitlement is what Play reports on the device. Purchases are
acknowledged in the app and re-checked on every resume. Purchases only work for
builds installed from a Play track (internal testing is enough, with your account
as a licence tester). Sideloaded debug APKs can't reach the product, so debug
builds show **Simulate purchase** on the paywall (and *End simulated subscription*
on the Mono sheet to try the downgrade).

## Code

```
app/src/main/java/com/cmatuteortega/monoburro/
  model/        Ingredient, UserPrefs (+ Ratios, TortillaSize, Targets), Proposal,
                Burrito + ShoppingItem, Profile (+ Goal, MacroGoals),
                Mode (Mono / Burro, Feature gating)
  billing/      MonoBilling: Google Play subscription
  data/         Ingredients.kt: the 40 seeded fillings + the fixed flour tortilla
  logic/        Macros, Scaling (batch + friendly units), RatioMath (sliders),
                Diet, Deck (swipe order and quotas), GenerateProposals,
                Burritos (editing), Shopping (merging), Nutrition (goals)
  storage/      AppState + StateStore (SharedPreferences)
  ui/           AppViewModel, MonoburroApp (shell, onboarding, mode badge), MainMenu
                (bottom bar), ModeSheets (paywall, Mono sheet), screens/ (Landing,
                the three onboarding steps, the four tabs + burrito detail), theme/
```

Proposal logic is deterministic and lives behind `ProposalGenerator` in
`logic/GenerateProposals.kt`, so an LLM-backed implementation can replace
`RuleBasedProposalGenerator` later. It uses only liked items that fit the
inferred diet (no meat liked → never meat; all-vegan likes → vegan), weights
favourites higher, penalises reusing a protein or sauce across proposals,
splits the tortilla's filling budget (≈250 g for a large one) by the user's
ratios, and then nudges portions towards any kcal / protein target.

## Build and test

JDK 17 and the Android SDK (compileSdk 36):

```sh
./gradlew testDebugUnitTest lintDebug   # JUnit: macros, scaling, ratios, deck, proposals, modes, burritos, shopping, nutrition
./gradlew installDebug
```

Every push builds in GitHub Actions (`.github/workflows/android.yml`): tests
and lint first, then the debug APK, uploaded as the `app-debug` artifact and
published to the rolling **debug-latest** prerelease:

https://github.com/cmatuteortega/monoburro/releases/download/debug-latest/monoburro-debug.apk
