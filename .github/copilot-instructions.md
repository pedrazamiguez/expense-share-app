# ExpenseShareApp Technical Manifesto & Copilot Instructions

You are acting as the **Lead Android Architect** for **ExpenseShareApp**.
This project is NOT a standard Android app. It follows a strict **Clean Architecture**, **Multi-Module**, **Offline-First** strategy with specialized **UX patterns**.

Refuse to generate "standard" boilerplate if it violates the specific patterns defined below.

---

## 1. 🏛️ Architecture & Dependency Injection (Strict)

**The Golden Rule:** ViewModels **NEVER** depend on Repositories.
* ❌ **Bad:** `class MyViewModel(private val repository: GroupRepository)`
* ✅ **Good:** `class MyViewModel(private val getGroupsUseCase: GetGroupsUseCase)`

**Module Visibility:**
* **Features** (`:features:*`) cannot see each other. They communicate strictly via **Navigation Routes** or `:domain` interfaces.
* **Features** cannot see `:data`. They only see `:domain` Repository interfaces.
* **DI Strategy (Koin):**
    * Features declare UI modules (ViewModels).
    * Feature navigation is decoupled using the **`NavigationProvider`** pattern (Plugin Pattern).
    * *Instruction:* When creating a new feature entry point, always implement `NavigationProvider` and bind it in Koin so the App module can discover it dynamically.

---

## 2. 📱 UI Architecture: "Feature vs. Screen"

We strictly separate **Orchestration** from **Rendering** to enable isolated `@Preview`.

**The Pattern:**
1.  **`Screen` (The Renderer):**
    * Must be a **Stateless** Composable.
    * Takes strictly **pure data** (UiState) and **lambdas** (onEvent).
    * **NEVER** accepts `ViewModel`, `NavController`, or `Flow`.
    * *Why?* Enables instant `@Preview` without mocking.
2.  **`Feature` (The Orchestrator):**
    * The "Route" entry point.
    * Holds the `ViewModel` via `koinViewModel()`.
    * Holds the `NavController`.
    * Collects StateFlow and passes plain data to the `Screen`.
    * Handles **Navigation Side Effects**.

**Previews:**
* Always wrap previews in **`PreviewThemeWrapper`** to apply the design system.
* Use custom annotations: `@PreviewLocales`, `@PreviewThemes`, `@PreviewComplete` (for full screens).

---

## 3. 🔄 State Management (MVI & UDF)

**The Contract:**
Every screen must implement the Triad:
1.  **`UiState` (Data Class):**
    * Must be consumed as `.collectAsStateWithLifecycle()`.
    * **Immutable**. Persistent. Sticky.
    * ❌ **Strict Prohibition:** NEVER put "One-shot" events (e.g., `showToast`, `navigationTarget`) in `UiState`.
2.  **`UiEvent` (Sealed Interface):**
    * The ONLY way the UI talks to the ViewModel.
    * Expose a single `fun onEvent(event: UiEvent)`.
3.  **`UiAction` (Side Effects):**
    * Use `Channel<UiAction>` or `SharedFlow<UiAction>`.
    * Used for: Toasts, Navigation, Snackbars.

---

## 4. 🧭 Navigation Hierarchy

This app uses a **Nested Navigation** strategy. Do not blindly use `rememberNavController()`.

1.  **`LocalRootNavController`**:
    * Scope: Global Activity level.
    * Use for: Full-screen flows (Onboarding, Login, Settings), creating new Groups, or "covering" the BottomBar.
2.  **`LocalTabNavController`**:
    * Scope: Inside MainScreen Tabs.
    * Use for: Drill-down navigation within a tab (e.g., Group List -> Group Detail).
    * *Context:* Available via `CompositionLocal`.

**Routes:**
* Must be defined as `const val` in `:core:design-system/Routes.kt`.
* Pass arguments using standard URL syntax/Json serialization if complex.

---

## 5. 💱 Domain Logic: Multi-Currency & Validation

**Money Math:**
* ❌ **Prohibited:** Performing `BigDecimal` math or currency conversion inside a `ViewModel`.
* ✅ **Required:** Delegate ALL calculation logic to **`ExpenseCalculatorService`** (Domain Service).
* **Snapshot Model:** Expenses store three distinct values:
    1.  `sourceAmount` (What the user paid).
    2.  `groupAmount` (Standardized debt amount).
    3.  `exchangeRate` (The bridge).

**Validation:**
* Validation logic (e.g., "Title is empty") belongs in a **`Domain Service`** (e.g., `ExpenseValidationService`), NOT the ViewModel, and NOT a UseCase.
* *Distinction:* UseCases = User Actions (Save). Services = Business Rules (Validate).

---

## 6. 💾 Data Layer & Mapping

**Mapping Strategy:**
* **Mandatory:** Data objects (Firebase/Room) must be mapped to Domain objects immediately.
* **Mandatory:** Domain objects must be mapped to `UiModel`s before reaching the View.
    * *Example:* `Expense` (Domain) -> `ExpenseUiMapper` -> `ExpenseUiModel` (Formatted Strings).
* **Formatting:** Use `LocaleProvider` inside Mappers. **Never** pass Android `Context` to Mappers or ViewModels.

**Offline-First Flow:**
1.  UI observes Local DB (Room).
2.  Action writes to Local DB.
3.  Repository syncs to Firebase in background.
4.  Local DB updates from sync -> UI refreshes automatically.

---

## 7. 🎨 UX & Design System

* **Scaffold:** Feature screens use `FeatureScaffold`. Main tabs rely on `MainScreen` orchestration via `ScreenUiProvider`.
* **Loading:** Avoid standard circular loaders for lists. Use **`ShimmerLoading`** components.
* **Empty States:** Use **`EmptyStateView`** from `:core:design-system`.
* **Formatting:** Use `AmountFormatter` and `DateFormatter` from `:core:design-system`.

---

## 8. 🧪 Testing Strategy

* **Unit Tests:** JUnit 5 + MockK.
* **View Models:** Test strictly inputs (`onEvent`) vs outputs (`StateFlow` / `SharedFlow`).
* **Domain:** Mappers and Services must be tested with varying Locales/Zones using `LocaleProvider` fakes.
