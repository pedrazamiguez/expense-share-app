---
name: sp-verify-architecture
description: Run automated architectural verification (Konsist rules, 600-line file limit, 0 FQN violations, and fast static checks).
mode: agent
tools:
  - codebase
  - terminalLastCommand
arguments: []
---# Verify Architecture

Execute automated architectural validation across the SplitTrip codebase:
---

## Step 1 — Konsist Architectural Rule Tests

Run Konsist test suite to verify:
- Clean Architecture layer boundaries (Features depend only on Domain and Core).
- ViewModel injection restrictions (no Repositories, Context, or ViewModels).
- Single Composable per file rule.
- Presentation mapper naming conventions (`*UiMapper` / `*UiMapperImpl`).
- Zero Fully Qualified Names (No FQN) in production code.

```bash
./gradlew :konsist-tests:test
```

---

## Step 2 — 600-Line File Size Guard

Verify that zero production Kotlin files exceed the 600-line hard limit:

```bash
find . -type f -name "*.kt" ! -path "*/test/*" ! -path "*/androidTest/*" ! -path "*/build/*" -exec wc -l {} + | awk '$1 > 600 {print $0}'
```

If any file exceeds 600 lines, extract event handlers, delegates, or smaller composables before proceeding.

---

## Step 3 — Fast Quality Gate (`make fast-check`)

Run ktlint, detekt, and fast incremental checks:

```bash
make fast-check > build.log 2>&1 && echo "Fast check passed" || (echo "Fast check failed. Last 100 lines:" && tail -n 100 build.log)
```

---

## Step 4 — Report Results

Summarize test execution results and architectural compliance status.
