---
name: sp-scaffold-usecase
description: Scaffold a clean :domain UseCase interface, implementation, DI factory registration, and JUnit 5 unit test.
mode: agent
tools:
  - codebase
  - terminalLastCommand
arguments:
  - name: feature_name
    description: The target feature or domain area (e.g. expenses, groups, balances).
    required: true
  - name: usecase_name
    description: The PascalCase name of the UseCase (e.g. CalculateExpenseSplitsUseCase).
    required: true
---# Scaffold UseCase

Scaffold a domain UseCase adhering strictly to Clean Architecture and project guidelines:
- Feature / Domain Area: $FEATURE_NAME
- UseCase Name: $USECASE_NAME
---

## Step 1 — Verify Domain Contracts & Structure

Ensure the UseCase adheres to:
1. **Interface Contract:** Define `interface $USECASE_NAME` in `domain/src/main/kotlin/es/pedrazamiguez/splittrip/domain/usecase/` (or subpackage).
   - Use `operator fun invoke(...)` with explicit return types and suspending if async.
2. **Implementation:** Define `class ${USECASE_NAME}Impl(...) : $USECASE_NAME` in `domain/src/main/kotlin/es/pedrazamiguez/splittrip/domain/usecase/impl/`.
   - Inject repository interfaces, calculators, or domain services.
   - ZERO Android framework dependencies.
   - Strict `BigDecimal` math (`docs/architecture/adrs/0002-bigdecimal-math.md`).
   - ZERO string formatting or UI text conversions.

---

## Step 2 — Register in Koin DI

1. Locate the corresponding domain Koin module (e.g., `domain/src/main/kotlin/.../${FEATURE_NAME}DomainModule.kt`).
2. Add factory registration using full camelCase variable names:
   ```kotlin
   factory<${USECASE_NAME}> {
       ${USECASE_NAME}Impl(
           // injected dependencies
       )
   }
   ```

---

## Step 3 — Scaffold Unit Test

1. Create test file in `domain/src/test/kotlin/.../${USECASE_NAME}ImplTest.kt`.
2. Use JUnit 5 (`org.junit.jupiter.api.Test`, `Assertions.assertEquals`) and MockK (`mockk()`, `coEvery`, `coVerify`).
3. NEVER use Kotlin's `assert()`.
4. Test both standard execution paths and edge cases (e.g., empty lists, division remainder handling).

---

## Step 4 — Verification Gate

1. Run Konsist and unit tests:
   ```bash
   ./gradlew :domain:test :konsist-tests:test
   ```
2. Verify line counts are well under the 600-line hard limit:
   ```bash
   wc -l <path/to/new_files>
   ```
