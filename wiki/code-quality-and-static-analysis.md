# Code Quality & Static Analysis

This project uses three complementary static analysis tools, each addressing a distinct concern. All three integrate with GitHub's **Security → Code Scanning** tab via SARIF reports.

---

## Tool Overview

| Tool | Purpose | Scope | GitHub Integration |
|---|---|---|---|
| **CodeQL** | Security vulnerability detection | Data-flow analysis: injection, XSS, credentials | Security tab, inline PR annotations |
| **Detekt** | Kotlin code quality & complexity | Code smells, cognitive complexity, naming, empty blocks | Security tab, inline PR annotations (via SARIF) |
| **Ktlint** | Kotlin formatting & style | Whitespace, imports, indentation, trailing commas | CI check (pass/fail) |

**CodeQL and Detekt are complementary, not competing.** CodeQL focuses on security patterns; Detekt focuses on code quality. Both upload SARIF with different categories (`/language:java-kotlin` vs `/tool:detekt`), so findings appear separately in the Security tab.

---

## Configuration Files

| File | Purpose |
|---|---|
| `config/detekt/detekt.yml` | Detekt rule configuration (thresholds, enabled/disabled rules) |
| `.editorconfig` | Ktlint formatting rules (line length, disabled rules for Compose conventions) |
| `.github/workflows/static-analysis.yml` | CI workflow: runs ktlint + detekt in parallel |
| `.github/workflows/codeql.yml` | CI workflow: runs CodeQL security analysis |
| `scripts/pre-commit` | Git pre-commit hook (ktlint only) |

---

## CI Workflow

The `static-analysis.yml` workflow runs on every push to `main`/`develop` and every PR targeting those branches. It runs **in parallel** with the existing `build-and-test.yml` — it does NOT modify or slow down the build pipeline.

### Two Parallel Jobs

1. **Ktlint (Formatting):** Runs `./gradlew ktlintCheck`. Fails the check if any formatting violations are found.
2. **Detekt (Code Quality):** Runs `./gradlew detekt --continue`. Uploads a merged SARIF report to GitHub Code Scanning. The Gradle task itself does not fail (`ignoreFailures = true`); gating is handled by GitHub's **"Code scanning results"** check.

### How GitHub Code Scanning Gating Works

- On pushes to `main`/`develop`, GitHub indexes ALL detekt findings as the **baseline** for that branch.
- On a PR, GitHub compares the PR's SARIF against the base branch's SARIF.
- **Only NEW alerts** (introduced by the PR) cause the check to fail.
- **Existing alerts** are visible but don't block the PR.
- This is the same mechanism CodeQL already uses.

---

## Reading Findings on GitHub

### Security Tab

Go to **Security → Code Scanning** to see all findings:
- Filter by **Tool** (CodeQL, detekt) to see findings from each tool separately.
- Filter by **Branch** to compare across branches.
- Each finding shows: file, line, rule name, severity, and description.
- You can **dismiss** false positives with a reason.

### PR Inline Annotations

Detekt findings appear as inline annotations on the PR diff — same as CodeQL. New findings are highlighted; existing ones are marked as pre-existing.

### Artifacts

HTML reports are also uploaded as build artifacts for detailed offline review. These are available under the workflow run's **Artifacts** section.

---

## Local Development Commands

| Command | Purpose | Speed |
|---|---|---|
| `./gradlew ktlintCheck` | Check formatting (all modules) | ~15s |
| `./gradlew ktlintFormat` | Auto-fix formatting (all modules) | ~15s |
| `./gradlew detekt` | Run full static analysis | ~30-45s |
| `./gradlew :app:ktlintCheck` | Check formatting (single module) | ~5s |
| `./gradlew :app:detekt` | Run analysis (single module) | ~10s |

---

## Pre-Commit Hook

A Git pre-commit hook runs **ktlint only** (not detekt) on every commit that includes Kotlin files. It is:

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

