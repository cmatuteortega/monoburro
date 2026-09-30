# Monoburro 🌯

AI-assisted batch cooking, burritos only. Native Android (Kotlin + Jetpack
Compose + Material 3), no backend or accounts: everything stays on the phone
(SharedPreferences, as JSON).

This MVP is the onboarding:

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
   with ingredients, grams and macros per burrito.
4. **Batch plan**: the chosen burrito scaled to the batch, in grams plus a
   friendly unit (cans, cups, avocados…). *Next: after cooking* is a stub.

"Restart onboarding" is in the ⋮ menu; 🌓 cycles system / light / dark.

## Code

```
app/src/main/java/com/cmatuteortega/monoburro/
  model/        Ingredient, UserPrefs (+ Ratios, TortillaSize, Targets), Proposal
  data/         Ingredients.kt: the 40 seeded fillings + the fixed flour tortilla
  logic/        Macros, Scaling (batch + friendly units), RatioMath (sliders),
                Diet, Deck (swipe order and quotas), GenerateProposals
  storage/      AppState + StateStore (SharedPreferences)
  ui/           OnboardingViewModel, MonoburroApp (shell), screens/, theme/
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
./gradlew testDebugUnitTest lintDebug   # JUnit: macros, scaling, ratios, deck, proposals
./gradlew installDebug
```

Every push builds in GitHub Actions (`.github/workflows/android.yml`): tests
and lint first, then the debug APK, uploaded as the `app-debug` artifact and
published to the rolling **debug-latest** prerelease:

https://github.com/cmatuteortega/monoburro/releases/download/debug-latest/monoburro-debug.apk
