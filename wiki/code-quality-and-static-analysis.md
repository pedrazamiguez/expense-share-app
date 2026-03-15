This project enforces consistent code formatting and static analysis across all modules using **Ktlint** and **Detekt**, with a custom rule module for project-specific checks. This article explains how the tools work, how they integrate with the IDE, the CI pipeline, and your local development workflow.

## Tools Overview

| Tool | Purpose | Plugin |
|---|---|---|
| **Ktlint** | Code formatting (import order, indentation, trailing commas, etc.) | `org.jlleitschuh.gradle.ktlint` |
| **Detekt** | Static code analysis (complexity, code smells, naming conventions) | `io.gitlab.arturbosch.detekt` |
| **Custom Detekt Rules** | Project-specific rules (e.g., explicit typing in Koin `get<T>()`) | `:detekt-rules` module |

Both tools are applied to **every subproject** via the root `build.gradle.kts` and share a single configuration:

* **Ktlint** reads its rules from `.editorconfig` at the project root.
* **Detekt** reads its rules from `config/detekt/detekt.yml`.
* **Custom rules** live in the `:detekt-rules` module and are wired as a `detektPlugins` dependency.

---

## IDE Integration

### What happens automatically

The `.editorconfig` file is natively supported by **Android Studio / IntelliJ IDEA**. When you open the project, the IDE automatically picks up:

* Indent style (4 spaces), charset (UTF-8), final newline, trailing whitespace trimming
* Max line length (120 characters for Kotlin files)

When you press **Cmd+Option+L** (macOS) or **Ctrl+Alt+L** (Windows/Linux) to reformat code, the IDE follows these rules.

### What does NOT happen automatically

* **Ktlint-specific rules** (import ordering, argument wrapping, trailing commas, etc.) are **not** enforced in real-time by the IDE. They are checked by Gradle tasks.
* **Detekt analysis** is not built into the IDE by default.

### Optional: Detekt IDE Plugin

You can install the **Detekt** IntelliJ plugin to see Detekt warnings inline as you code:

1. Go to **Settings → Plugins → Marketplace** → search **"Detekt"**
2. Install and restart the IDE
3. Go to **Settings → Tools → Detekt** and configure:
   * **Configuration file**: point to `config/detekt/detekt.yml`
   * **Baseline file**: point to the module's `detekt-baseline.xml` (optional)

---

## Local Development Workflow

### Pre-commit hook (recommended)

After cloning the project, install the Git hook:

```bash
./gradlew installGitHooks
```

This copies `scripts/pre-commit` into `.git/hooks/`. From that point on, **every `git commit`** automatically runs:

1. `./gradlew ktlintCheck` — blocks the commit if formatting is wrong
2. `./gradlew detekt` — blocks the commit if code smells are found

If the commit is blocked, the hook tells you what to do:

* **Ktlint failure** → run `./gradlew ktlintFormat` to auto-fix, then commit again
* **Detekt failure** → check the reports, fix the issue manually, then commit again

> **Note:** The hook uses `--daemon` and `--quiet` for speed. On a warm Gradle daemon, it typically completes in a few seconds.

### Gradle tasks reference

| Task | What it does |
|---|---|
| `./gradlew ktlintCheck` | Check formatting across all modules (read-only) |
| `./gradlew ktlintFormat` | Auto-fix formatting issues across all modules |
| `./gradlew detekt` | Run static analysis across all modules |
| `./gradlew ktlintCheck detekt` | Run both checks at once |
| `./gradlew installGitHooks` | Install the pre-commit hook |

### Typical workflow

```
1. Write code
2. git add .
3. git commit -m "feat: ..."
   └── pre-commit hook runs ktlintCheck + detekt
       ├── ✅ All passed → commit proceeds
       └── ❌ Failed → commit blocked
           ├── Ktlint? → ./gradlew ktlintFormat → git add . → git commit again
           └── Detekt? → fix manually → git add . → git commit again
```

---

## Configuration Details

### Ktlint (`.editorconfig`)

Ktlint reads formatting rules from the root `.editorconfig`. Key settings:

```ini
[*.{kt,kts}]
max_line_length = 120
ktlint_code_style = android_studio
```

The following rules are **disabled** because they conflict with Android/Compose conventions:

| Rule | Reason |
|---|---|
| `backing-property-naming` | Idiomatic `_uiState` → `uiState` pattern in ViewModels |
| `function-signature` | Causes cascading indent issues with Compose functions |
| `class-signature` | Same cascading indent problem |
| `function-expression-body` | Subjective preference, hard to auto-fix |
| `function-naming` | Compose uses PascalCase for `@Composable` functions |
| `no-wildcard-imports` | Handled by Detekt's `WildcardImport` rule instead |
| `package-name` | Android convention allows underscores in test packages |

### Detekt (`config/detekt/detekt.yml`)

Key thresholds:

| Rule | Threshold | Notes |
|---|---|---|
| `CognitiveComplexMethod` | 15 | Max cognitive complexity per method |
| `LongMethod` | 60 | Max lines per method |
| `LongParameterList` | 6 (functions), 7 (constructors) | `@Composable` functions are excluded |
| `NestedBlockDepth` | 4 | Max nesting depth |
| `TooManyFunctions` | 15 | Per file/class; `@Preview` functions excluded |
| `MaxLineLength` | 120 | Consistent with Ktlint |
| `ReturnCount` | 3 | Guard clauses excluded |

The built-in `formatting` ruleset in Detekt is **disabled** — Ktlint handles formatting separately to allow auto-fixing via `ktlintFormat`.

### Detekt Baselines

Pre-existing issues are captured in **per-module baseline files** (`detekt-baseline.xml` in each module directory). This means:

* **New code** must pass all Detekt rules — the build fails on any new violation.
* **Existing issues** are suppressed and can be addressed incrementally over time.
* To regenerate baselines (e.g., after bulk-fixing issues): `./gradlew detektBaseline`

---

## Custom Detekt Rules (`:detekt-rules`)

The `:detekt-rules` module contains project-specific rules. Currently implemented:

### `KoinExplicitTypeRule`

Flags Koin `get()` calls that lack an explicit type argument.

```kotlin
// ❌ BAD — implicit type resolution
val service = get()

// ✅ GOOD — explicit type
val service = get<ValidationService>()
```

**Why:** Implicit `get()` in Koin modules can lead to hard-to-debug runtime errors when the wrong type is injected. Explicit typing makes DI wiring self-documenting and catches mistakes at compile time.

### Adding new rules

1. Create a new class in `detekt-rules/src/main/kotlin/.../detekt/` extending `Rule(config)`
2. Register it in `CustomRuleSetProvider.instance()`
3. Add configuration in `config/detekt/detekt.yml` under the `KoinRules:` section
4. Write tests using `detekt-test` utilities (see `KoinExplicitTypeRuleTest` as reference)

---

## CI Integration

The CI pipeline (`.github/workflows/build-and-test.yml`) runs both tools **before** the build/test phase. If either fails, the entire workflow fails and the PR is blocked.

### Pipeline order

```
1. ktlintCheck          → Formatting gate
2. detekt               → Static analysis gate
3. lintDebug            → Android Lint
4. testDebugUnitTest    → Unit tests
5. assembleDebug        → Build
```

### Reports on GitHub

* **Code Scanning (SARIF):** Detekt findings are uploaded to GitHub's **Security → Code scanning** tab via SARIF format. On pull requests, findings appear as **inline annotations** on the changed files in the diff view.

* **Workflow Artifacts:** Downloadable from any workflow run's **Summary** page:
  * `detekt-reports` — HTML reports from every module (always uploaded)
  * `ktlint-reports` — Text reports (uploaded only on failure for diagnosis)

* **Workflow Steps:** The "Run Code Formatting Check (Ktlint)" and "Run Static Code Analysis (Detekt)" steps show pass/fail directly in the workflow log.

### Viewing Detekt reports locally

After running `./gradlew detekt`, HTML reports are generated per module:

```
<module>/build/reports/detekt/detekt.html
```

Open any of these in a browser for a detailed, navigable view of all findings.

---

## Known Limitations

### Detekt deprecation warning on Gradle 9.x

Detekt 1.23.8 emits a `ReportingExtension.file(String)` deprecation warning on Gradle 9.x. This is a [known upstream issue](https://github.com/detekt/detekt/issues/8452) — the fix is on Detekt's `main` branch but will only ship with **Detekt 2.0.0 stable** (currently alpha). The warning is cosmetic and does not affect functionality.

### Ktlint false positives on type-parameter-only imports

Ktlint's `no-unused-imports` rule occasionally removes imports that are only referenced as type parameters (e.g., `get<AuthenticationService>()`). If you encounter a compilation error after running `ktlintFormat`, check if an import was incorrectly removed and add it back manually.

