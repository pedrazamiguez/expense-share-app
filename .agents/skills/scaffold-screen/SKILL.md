---
name: sp-scaffold-screen
description: Scaffold a complete Horizon Compose MVI screen (stateless Screen, Feature orchestrator, UiState/UiEvent/UiAction, ViewModel, UiMapper, and Koin registration).
mode: agent
tools:
  - codebase
  - terminalLastCommand
arguments:
  - name: feature_module
    description: The target feature module (e.g. features/expenses, features/groups).
    required: true
  - name: screen_name
    description: The base PascalCase name of the screen (e.g. ExpenseDetail).
    required: true
---# Scaffold Compose MVI Screen

Scaffold an MVI screen following the Feature vs. Screen architecture and Horizon Narrative guidelines:
- Feature Module: $FEATURE_MODULE
- Screen Name: $SCREEN_NAME
---

## Step 1 — Scaffold MVI Contracts

In the presentation contract package (`${FEATURE_MODULE}/src/main/kotlin/.../presentation/model/`):
1. **`UiState`:** Immutable data class (`data class ${SCREEN_NAME}UiState(...)`) with default arguments and `ImmutableList` collections.
2. **`UiEvent`:** Sealed interface (`sealed interface ${SCREEN_NAME}UiEvent`) representing user intentions.
3. **`UiAction`:** Sealed interface (`sealed interface ${SCREEN_NAME}UiAction`) for one-shot side-effects (e.g. navigation, error notifications).

---

## Step 2 — Scaffold Stateless `*Screen`

Create `${SCREEN_NAME}Screen.kt`:
1. Single top-level Composable matching the file name.
2. Pure rendering: Accepts `uiState: ${SCREEN_NAME}UiState` and lambda callbacks (`onEvent: (${SCREEN_NAME}UiEvent) -> Unit`).
3. ZERO ViewModel or NavController references.
4. Uses `Modifier.debouncedClickable` on interactive components (`docs/architecture/adrs/0005-debounced-ui-interactions.md`).
5. Applies `LocalBottomPadding.current` for bottom spacing when inside tabs.

---

## Step 3 — Scaffold Stateful `*Feature` Orchestrator

Create `${SCREEN_NAME}Feature.kt`:
1. Injects `viewModel: ${SCREEN_NAME}ViewModel = koinViewModel()`.
2. Collects `uiState` via `collectAsStateWithLifecycle()`.
3. Observes `viewModel.actions` with `ObserveAsEvents { action -> ... }`.
4. Wraps content with `FeatureScaffold(currentRoute = ...)` if appropriate, or handles route navigation.

---

## Step 4 — Scaffold `*ViewModel` & `*UiMapper`

1. **`*ViewModel`:**
   - Injects ONLY UseCases, Mappers, and Domain Services.
   - NEVER injects `Context`, `LocaleProvider`, Repositories, or other ViewModels.
   - Exposes `val uiState: StateFlow<${SCREEN_NAME}UiState>` and `val actions: SharedFlow<${SCREEN_NAME}UiAction>`.
   - Single entry point: `fun onEvent(event: ${SCREEN_NAME}UiEvent)`.
2. **`*UiMapper`:**
   - End with `UiMapper` (or `UiMapperImpl`).
   - Injects `LocaleProvider` and/or `FormattingHelper` for all string/amount formatting.

---

## Step 5 — DI Wiring & Route Registration

1. Register `viewModel`, `mapper`, and optional `ScreenUiProvider` / `TabGraphContributor` in the feature's `*UiModule.kt`.
2. Add route constant to `:core:design-system`'s `Routes.kt`.

---

## Step 6 — Verification Gate

1. Verify file line counts are under 600 lines (`wc -l <files>`).
2. Run fast check:
   ```bash
   make fast-check
   ```
