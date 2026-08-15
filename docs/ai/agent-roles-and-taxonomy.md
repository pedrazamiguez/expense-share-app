# Specialized Agent Personas & Phased SDD Pipeline

This document defines the 5 specialized agent personas, their bounded responsibilities, input/output contracts, and the phase-gated Specification-Driven Development (SDD) lifecycle.

---

## 1. The 5 Specialized Agent Personas

```mermaid
flowchart LR
    A1["1. Specification Agent"] -->|SDD Change Spec| A2["2. Architect Agent"]
    A2 -->|Interface Contracts| A3["3. Domain Implementer"]
    A3 -->|Domain Unit Tests Pass| A4["4. Infra & UI Implementer"]
    A4 -->|UI & DB Wired| A5["5. Auditor / QA Agent"]
```

---

### Persona 1: Specification Agent
* **Core Mission:** Ingests feature briefs or GitHub issues, disambiguates requirements, retrieves domain invariants from `/docs/domain/`, and produces a formal, machine-readable specification.
* **Grounding Sources:** `/docs/domain/*.md`, GitHub issue description, user comments.
* **MCP Tools:** `github-mcp-server` (`issue_read`).
* **Output Artifact:** Formal SDD Change Specification detailing user stories, domain invariants, edge cases, and acceptance criteria.
* **Guardrails:** Does NOT write Kotlin code or propose UI implementations.

---

### Persona 2: Architect Agent
* **Core Mission:** Validates module boundaries, designs public interfaces, declares UseCase signatures, and specifies MVI state models.
* **Grounding Sources:** `/docs/architecture/adrs/*.md`, `/docs/architecture/patterns/*.md`.
* **MCP Tools:** `codebase-memory-mcp` (`search_graph`, `trace_path`), `graphify` (`query_graph`).
* **Output Artifact:** Architecture Contract specifying exact file paths (`[NEW]`, `[MODIFY]`, `[DELETE]`), interface signatures, DI module registrations, and MVI `UiState`/`UiEvent`/`UiAction` contracts.
* **Guardrails:** Enforces Clean Architecture dependency rules (Features depend only on Domain & Core).

---

### Persona 3: Domain Implementer
* **Core Mission:** Implements pure business logic, domain models, validation services, and unit tests using strict Test-Driven Development (TDD).
* **Grounding Sources:** `/docs/domain/*.md`, `docs/architecture/adrs/0002-bigdecimal-math.md`.
* **Tools:** Pure Kotlin compiler, MockK, JUnit 5.
* **Output:** Pure Kotlin entities, UseCases, Domain Services, and unit tests in `:domain`.
* **Guardrails:**
  - ZERO Android framework dependencies (`android.*`).
  - ZERO `Double` or `Float` for financial or percentage math (strict `BigDecimal`).
  - ZERO string formatting or locale manipulation in domain.

---

### Persona 4: Infra & UI Implementer
* **Core Mission:** Connects domain contracts to local Room persistence, background Firestore sync delegates, MVI ViewModels, and stateless Horizon Compose UI screens.
* **Grounding Sources:** `/docs/architecture/patterns/offline-first.md`, `/docs/architecture/patterns/mvi-and-stateless-screens.md`, `/docs/design-system/horizon-narrative-design.md`.
* **MCP Tools:** `firebase-mcp-server`, `StitchMCP`.
* **Output:** Room entities/DAOs, Firestore mappers, Sync delegates, `*ViewModel`, `*UiMapperImpl`, and `*Screen`/`*Feature` composables.
* **Guardrails:**
  - Strict Offline-First write protocol (Room write first, background sync).
  - Stateless `*Screen` (no ViewModel or NavController parameters).
  - Debounced click modifiers on all actionable items (`debouncedClickable`).

---

### Persona 5: Auditor / QA Agent
* **Core Mission:** Executes automated architectural tests, static analysis, code coverage verification, and quality gates.
* **Grounding Sources:** `/docs/engineering/quality-and-static-analysis.md`, `konsist-tests`.
* **MCP Tools:** `sonarqube`.
* **Verification Gates:**
  - `ArchitectureTest` via Konsist (`./gradlew :konsist-tests:test`).
  - File size check (`wc -l <file>` ≤ 600 lines for all production files).
  - Zero Fully Qualified Name (FQN) violations.
  - `make fast-check` and `make check`.
* **Guardrails:** Fails the pipeline if any detekt findings, formatting issues, or architectural violations exist.

---

## 2. Phased SDD Execution Lifecycle

```
[Phase 1: Ingest & Spec]   → Specification Agent analyzes brief & domain docs
         │
[Phase 2: Architecture]    → Architect Agent designs interfaces & contracts
         │
[Phase 3: Domain & TDD]    → Domain Implementer builds pure logic & unit tests
         │
[Phase 4: Persistence/UI]  → Infra & UI Implementer builds Room, Sync & Compose UI
         │
[Phase 5: Audit & Gate]    → Auditor / QA Agent verifies Konsist, static analysis & tests
```

Every feature implementation follows these discrete, verifiable phases, preventing context drift and guaranteeing architectural integrity.
