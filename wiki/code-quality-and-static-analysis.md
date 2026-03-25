# Code Quality & Static Analysis

This project uses six complementary code quality tools, each addressing a distinct concern.

---

## Tool Overview

| Tool | Purpose | Scope | GitHub Integration |
|---|---|---|---|
| **CodeQL** | Security vulnerability detection | Data-flow analysis: injection, XSS, credentials | Security tab, inline PR annotations |
| **Detekt** | Kotlin code quality & complexity | Code smells, cognitive complexity, naming, empty blocks | Security tab, inline PR annotations (via SARIF) |
| **Android Lint** | Android framework issues | Hardcoded text, missing translations, API level, accessibility, manifest | Security tab, inline PR annotations (via SARIF, `/tool:android-lint` category) |
| **Ktlint** | Kotlin formatting & style | Whitespace, imports, indentation, trailing commas | CI check (pass/fail) |
| **CPD** | Code duplication detection | Duplicate code blocks (≥100 tokens) across all modules | Security tab as `Note`-level findings (via SARIF, `/tool:cpd` category), inline PR annotations |
| **JaCoCo** | Code coverage enforcement | Unit test line/branch coverage — 80% required on overall and changed files | PR comment via `madrapps/jacoco-report` (overall + per-changed-file, pass 🟢 / fail 🔴) |
| **Konsist** | Architecture rule enforcement | Naming conventions, dependency rules, structural patterns | Check Run with per-rule pass/fail annotations in PR Checks tab |

**CodeQL and Detekt are complementary, not competing.** CodeQL focuses on security patterns; Detekt focuses on code quality. Both upload SARIF with different categories (`/language:java-kotlin` vs `/tool:detekt`), so findings appear separately in the Security tab.

---

## Configuration Files

| File | Purpose |
|---|---|
| `config/detekt/detekt.yml` | Detekt rule configuration (thresholds, enabled/disabled rules) |
| `.editorconfig` | Ktlint formatting rules (line length, disabled rules for Compose conventions) |
| `build.gradle.kts` (root) | CPD, JaCoCo, Detekt, Ktlint — configured for all subprojects |
| `konsist-tests/` | Konsist architecture tests (`:konsist-tests` module) |
| `.github/workflows/static-analysis.yml` | CI workflow: runs ktlint + detekt + CPD in parallel |
| `.github/workflows/coverage-and-architecture.yml` | CI workflow: runs JaCoCo coverage + Konsist architecture tests |
| `.github/workflows/build-and-test.yml` | CI workflow: lint, unit tests, build, APK upload |
| `.github/workflows/codeql.yml` | CI workflow: runs CodeQL security analysis |
| `scripts/pre-commit` | Git pre-commit hook (ktlint only) |

---

## CI Workflows

All workflows are independent and run in parallel on every push to `main`/`develop` and every PR targeting those branches.

### Static Analysis (`static-analysis.yml`)

Four parallel jobs:

1. **Ktlint (Formatting):** Runs `./gradlew ktlintCheck`. Fails the check if any formatting violations are found.
2. **Detekt (Code Quality):** Runs `./gradlew detekt --continue`. Uploads a merged SARIF report to GitHub Code Scanning. The Gradle task itself does not fail (`ignoreFailures = true`); gating is handled by GitHub's **"Code scanning results"** check.
3. **CPD (Duplication Detection):** Runs `./gradlew cpdCheck --continue`. Converts the CPD XML report into SARIF and uploads it to **GitHub Code Scanning** with category `/tool:cpd`. Each duplication block becomes a `Note`-level finding pointing to the primary file location, with the secondary location(s) attached as `relatedLocations` (visible in the finding detail). Findings appear in the Security → Code Scanning tab alongside Detekt and CodeQL. Uses `ignoreFailures = true` — duplications never block PRs by default (Note severity is below the standard gating threshold).
4. **Android Lint:** Runs `./gradlew lintDebug` with `continue-on-error: true` (because `abortOnError = true` in `build.gradle.kts` causes a non-zero exit when findings are present; reports are still generated before the abort). Merges all per-module `lint-results-debug.sarif` files and uploads to GitHub Code Scanning with category `/tool:android-lint`. Findings appear in the Security tab alongside Detekt/CodeQL. **Note:** `build-and-test.yml` already runs `lintDebug` as a hard blocking gate (`abortOnError = true`); this job is purely for Code Scanning visibility and PR annotations.

### Coverage and Architecture (`coverage-and-architecture.yml`)

Two parallel jobs — completely independent from `build-and-test.yml`:

1. **Konsist Architecture Tests:** Runs `./gradlew :konsist-tests:test`. Enforces naming conventions, dependency rules, and structural patterns. Failures block the PR. After the test run, `dorny/test-reporter` publishes a **Check Run** named "Konsist Architecture Tests" — visible in the PR's **Checks** tab with per-rule pass/fail and inline annotations pinpointing the exact violation. No need to download any artifact.
2. **JaCoCo Coverage Report:** Runs `testDebugUnitTest`, `:domain:test`, then `jacocoMergedReport`. On PRs, `madrapps/jacoco-report@v1.7.2` posts a PR comment showing overall coverage and per-changed-file coverage with pass 🟢 / fail 🔴 indicators. **Enforces 80% minimum** on both overall project coverage and on the files changed in the PR — the CI check fails if either threshold is not met.

### Build and Test (`build-and-test.yml`)

Unchanged. Runs lint, unit tests, and assembles a debug APK. Publishes test results and uploads artifacts. This workflow is untouched.

### How GitHub Code Scanning Gating Works

- On pushes to `main`/`develop`, GitHub indexes ALL detekt findings as the **baseline** for that branch.
- On a PR, GitHub compares the PR's SARIF against the base branch's SARIF.
- **Only NEW alerts** (introduced by the PR) cause the check to fail.
- **Existing alerts** are visible but don't block the PR.
- This is the same mechanism CodeQL already uses.

---

## CPD (Copy-Paste Detector)

CPD uses PMD's Kotlin tokenizer to detect duplicated code blocks. It is configured at the root `build.gradle.kts` level using the `de.aaschmid.cpd` Gradle plugin.

### Configuration

- **Minimum token count:** 100 (blocks with fewer than 100 identical tokens are ignored)
- **Language:** Kotlin
- **Scope:** All `src/main/kotlin` directories across all subprojects
- **Reports:** XML + text (saved to `build/reports/cpd/`)
- **Failure behavior:** `ignoreFailures = true` (informational only)

### Interpreting Reports

Each duplication entry in the CPD report shows:
- The two (or more) file locations with duplicated code
- The number of duplicated tokens and lines
- The duplicated code fragment

Use this to identify extraction candidates — duplicated logic should be centralized into domain services or shared utilities.

---

## JaCoCo (Code Coverage)

JaCoCo is configured for all subprojects via the root `build.gradle.kts`. It supports both Android modules (library/application) and pure JVM modules (`:domain`).

### How It Works

- **Android modules:** Coverage is collected from `testDebugUnitTest` execution data (`build/jacoco/testDebugUnitTest.exec`). Classes are read from `build/tmp/kotlin-classes/debug`.
- **JVM modules:** Coverage is collected from `test` execution data (`build/jacoco/test.exec`). Classes are read from `build/classes/kotlin/main`.
- **Merged report:** The `jacocoMergedReport` task aggregates all subproject execution data into a single HTML + XML report at `build/reports/jacoco/merged/`.

### Excluded Classes

The following generated classes are excluded from coverage measurements:
- `R.class`, `R$*.class`, `BuildConfig.*`, `Manifest*.*` (Android generated)
- `*_Factory.*`, `*_MembersInjector.*` (DI generated)
- `*Module.*`, `*Module$*.*` (Koin DI modules)
- `*ComposableSingletons*.*` (Compose generated)
- `*_Impl.*`, `*Dao_Impl.*` (Room generated)
- `*PreviewHelper*.*` (debug preview helpers)
- `*$Companion.*` (companion objects)

### Per-Module Reports

Each subproject generates its own `jacocoTestReport` after unit tests run. Reports are saved to `<module>/build/reports/jacoco/`.

---

## Konsist (Architecture Rule Enforcement)

[Konsist](https://docs.konsist.lemonappdev.com/) is a Kotlin architecture testing library that enforces structural rules at test time. Rules are written as JUnit 5 tests in the `:konsist-tests` module.

### Architecture Rules Enforced

#### Naming Conventions
- Domain services must end with `Service`
- Use cases must end with `UseCase`
- Repository interfaces must end with `Repository`
- ViewModels must end with `ViewModel`
- Event handlers must end with `EventHandler`
- Data sources must end with `DataSource`

#### ViewModel Dependency Rules
- ViewModels must NOT import Repository classes
- ViewModels must NOT import data layer packages
- ViewModels must NOT import `android.content.Context`
- ViewModels must NOT import `LocaleProvider`

#### Handler Isolation
- Event handlers must NOT depend on other event handlers (via constructor)

#### Feature Module Isolation
- Feature modules must NOT import from the data layer
- Feature modules must NOT import from other feature modules

#### Domain Layer Purity
- Domain module must NOT import Android framework classes (`android.*`, `androidx.*`)
- Domain module must NOT import data layer
- Domain services must NOT contain formatting/display methods

#### Screen Statelessness
- Screen composables must NOT import ViewModel classes

### Adding New Rules

To add a new architecture rule:
1. Open `konsist-tests/src/test/kotlin/.../konsist/ArchitectureTest.kt`
2. Add a new `@Test` method in the appropriate `@Nested` class
3. Use Konsist's fluent API to scope → filter → assert
4. Run `./gradlew :konsist-tests:test` to verify

## Reading Findings on GitHub

### Security Tab (`Security → Code Scanning`)

Go to **Security → Code Scanning** to see all findings:
- Filter by **Tool** to see findings from each tool separately: `CodeQL`, `detekt`, `CPD`, `Android Lint`.
- Filter by **Branch** to compare across branches.
- Each finding shows: file, line, rule name, severity, and description.
- You can **dismiss** false positives with a reason.

**CPD findings** appear as `Note`-level entries under tool `CPD`. Existing duplications form the baseline and won't re-flag on PRs unless new ones are introduced.

**Android Lint findings** appear under tool `Android Lint`. The same baseline mechanism applies — only new findings introduced by a PR will create PR annotations.

### PR Inline Annotations (Security tab / Checks Tab)

- **Detekt** findings appear as inline annotations on the PR diff. New findings are highlighted; existing ones are marked as pre-existing.
- **Android Lint** findings appear as inline annotations on the PR diff for newly introduced issues.
- **CPD** findings appear as inline `Note` annotations if the duplicated block touches a changed line.

### Konsist Check Run (PR Checks Tab)

After every CI run, a **"Konsist Architecture Tests"** Check Run appears in the PR's **Checks** tab. Each failed architecture rule is listed as a named test with its error message. If all rules pass, the check is green.

### JaCoCo PR Comment

On every PR, `madrapps/jacoco-report` posts a comment showing:
- **Overall project coverage** against the 80% threshold
- **Per-changed-file coverage** — every `.kt` file in the PR diff with its line and branch coverage

The check **fails** (blocks the PR) if either overall coverage or any changed file is below **80%**. The comment is updated in place on each push to the PR branch.

### Artifacts

HTML/XML/SARIF reports are uploaded as build artifacts for deep-dive offline review when needed:
- **detekt-reports:** HTML + SARIF reports from Detekt
- **cpd-reports:** XML + SARIF reports from CPD
- **android-lint-reports:** HTML + SARIF reports from Android Lint
- **jacoco-coverage-reports:** Merged HTML + XML coverage reports from JaCoCo
- **konsist-test-results:** HTML test result report from Konsist
- **unit-test-reports:** HTML test result reports

---

## Local Development Commands

| Command | Purpose | Speed |
|---|---|---|
| `./gradlew ktlintCheck` | Check formatting (all modules) | ~15s |
| `./gradlew ktlintFormat` | Auto-fix formatting (all modules) | ~15s |
| `./gradlew detekt` | Run full static analysis | ~30-45s |
| `./gradlew cpdCheck` | Run copy-paste detection | ~15s |
| `./gradlew jacocoMergedReport` | Generate merged coverage report | ~2-3min (runs all tests) |
| `./gradlew :domain:jacocoTestReport` | Coverage report for a single module | ~30s |
| `./gradlew :konsist-tests:test` | Run architecture rule tests | ~30s |
| `./gradlew :app:ktlintCheck` | Check formatting (single module) | ~5s |
| `./gradlew :app:detekt` | Run analysis (single module) | ~10s |

---

## Pre-Commit Hook

A Git pre-commit hook runs **ktlint only** (not detekt, CPD, or Konsist) on every commit that includes Kotlin files. It is:

- **Fast:** ~5-10 seconds with the Gradle daemon.
- **Verbose:** Full error output with file paths, line numbers, and rule names. Never piped to `/dev/null`.
- **Helpful:** On failure, it shows how to auto-fix (`./gradlew ktlintFormat`) and how to bypass (`git commit --no-verify`).
- **Smart:** Skips entirely if no `.kt` or `.kts` files are staged.

### Installation

The hook is auto-installed when Gradle syncs the project (via the `installGitHooks` task). You can also install it manually:

```bash
./gradlew installGitHooks
```

### Escape Hatch

If you need to commit without running the hook (e.g., WIP commit, documentation-only changes):

```bash
git commit --no-verify -m "your message"
```

---

## IDE Integration

### IntelliJ / Android Studio — Detekt Plugin

Install the [Detekt IntelliJ Plugin](https://plugins.jetbrains.com/plugin/10761-detekt) for real-time in-editor feedback:

1. **Settings → Plugins → Marketplace** → Search "Detekt" → Install
2. **Settings → Tools → Detekt** → Enable, point to `config/detekt/detekt.yml`
3. Findings appear as warnings/errors directly in the editor

### EditorConfig

Android Studio/IntelliJ natively respects `.editorconfig` for formatting (indent, charset, line length). No plugin needed — it works out of the box.

---

## Triaging Existing Findings

After the initial SARIF upload, the Security tab will show ALL existing detekt findings. To manage them:

1. **Review** findings by severity (Error / Warning / Note).
2. **Dismiss** false positives with a documented reason ("Won't fix", "Used in tests", etc.).
3. **Create follow-up issues** for legitimate findings, grouped by category (e.g., "Reduce complexity in `:features:expenses`").
4. Track progress — as you fix findings, they automatically disappear from the Security tab on the next push.
