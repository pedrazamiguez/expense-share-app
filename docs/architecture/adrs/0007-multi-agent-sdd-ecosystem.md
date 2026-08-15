# ADR-0007: Specialized Multi-Agent and Skill-Driven Development Ecosystem

* **Status:** Accepted
* **Deciders:** SplitTrip Engineering Team
* **Date:** 2026-08-15
* **Tags:** ai, agents, skills, sdd, mcp, architecture

---

## Context and Problem Statement

Monolithic single-pass AI prompt invocations attempt to handle requirement ingestion, domain modeling, implementation, UI wiring, and quality verification in a single context window. This creates severe context contention, increases hallucination risk, and leads to subtle architectural drift (such as violating Clean Architecture boundaries or introducing float math).

How do we transition AI-assisted development into a specialized, phase-gated multi-agent workflow grounded in authoritative domain documentation and MCP tooling?

## Decision Drivers

* Elimination of context window saturation and reasoning degradation.
* Strict grounding in living domain invariants and architectural decision records.
* Discrete Specification-Driven Development (SDD) phases with clear contracts between stages.
* Full integration with Model Context Protocol (MCP) servers and automated quality verification.

## Considered Options

1. **Monolithic Single-Prompt Execution:** All instructions dumped into a single agent turn. High failure rate for non-trivial features.
2. **Ad-Hoc Unstructured Multi-Prompting:** Developers manually run disjointed prompts without standardized grounding or schemas.
3. **Specialized Multi-Agent SDD Pipeline Grounded in `/docs/` (Chosen):** Phased pipeline with 5 specialized agent personas, standardized `/docs/` grounding hierarchy, dedicated scaffolding skills, and automated verification gates.

## Decision Outcome

Chosen option: **Option 3 (Specialized Multi-Agent SDD Pipeline Grounded in `/docs/`)**.

### Phased SDD Pipeline & Agent Personas
1. **Specification Agent:** Ingests the feature brief, retrieves business invariants from `/docs/domain/`, and outputs a machine-readable SDD change specification.
2. **Architect Agent:** Reads the spec and `/docs/architecture/adrs/`, validates module boundaries, and outputs formal interface contracts, UseCase signatures, and MVI state models.
3. **Domain Implementer:** Implements pure business logic, models, and domain services in `:domain` with TDD (MockK/JUnit 5 unit tests). Enforces zero Android dependencies and zero string formatting in domain.
4. **Infra & UI Implementer:** Implements Room DAOs, Firestore sync delegates, MVI ViewModels, and stateless Horizon Compose UI screens.
5. **Auditor / QA Agent:** Runs Konsist architecture tests, checks 600-line file limits and 0 FQN violations, and executes `make fast-check` / `make check`.

### Grounding Documentation Hierarchy
* The legacy `wiki/` directory is replaced by a structured, version-controlled `/docs/` hierarchy:
  - `/docs/domain/`: Living business invariants and ubiquitous language.
  - `/docs/architecture/adrs/`: Formal MADRs (0001–0007+).
  - `/docs/architecture/patterns/`: Clean Arch, Offline-First, MVI, and navigation patterns.
  - `/docs/design-system/`: Horizon narrative tokens and Compose guidelines.
  - `/docs/engineering/`: Quality gates, Konsist rules, and release protocols.
  - `/docs/ai/`: Agent taxonomy, MCP catalog, and intelligence tools.

## Consequences

### Positive
* High accuracy and zero architectural drift during AI-assisted feature development.
* Clear traceability and modular contracts between specification, domain logic, and UI.
* Seamless MCP orchestration across GitHub, Firebase, and Codebase Graph tools.

### Negative / Trade-offs
* Multi-agent execution requires structured phase coordination.

## References
* [`docs/ai/ai-ecosystem.md`](../../ai/ai-ecosystem.md)
* [`docs/ai/agent-roles-and-taxonomy.md`](../../ai/agent-roles-and-taxonomy.md)
* [`docs/README.md`](../../README.md)
