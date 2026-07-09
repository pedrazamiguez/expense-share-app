# AI Infrastructure

This project uses a standardized taxonomy for AI assistant behaviors and workflows, integrating with OpenCode, Antigravity, and GitHub Copilot.

## Taxonomy

### 1. Rules (`.agents/rules/`)
Rules are passive, always-on constraints. They never trigger an action on their own. Instead, the agent reads them at startup or when prompted to ensure it doesn't violate project boundaries.
- **Scope:** Single-focus, usually one file per constraint.
- **Location:** `.agents/rules/*.md`
- **Examples:** `big-decimal-math.md`, `offline-first-protocol.md`.
- **How to create:** Create a new markdown file in the rules directory explaining the constraint clearly. No YAML frontmatter needed.

### 2. Skills (`.agents/skills/`)
Skills are active workflows invoked by name (e.g., `/sp-plan-issue`). 
- **Scope:** Defines a specific sequence of actions, tool usage, and required arguments for a repeatable workflow.
- **Location:** `.agents/skills/<skill_name>/SKILL.md`
- **Examples:** `sp-plan-issue`, `sp-start-issue`, `sp-review-pr`.
- **How to create:** Create a folder with a `SKILL.md` containing YAML frontmatter defining the skill's name, description, tools, and arguments, followed by markdown instructions.

### 3. Workflows (`.agents/workflows/`)
Workflows are cross-cutting orchestration directives.
- **Location:** `.agents/workflows/*.md`

## Configuration Hierarchy

- **Global:** `~/.config/opencode/opencode.jsonc` or `~/.gemini/config/`
- **Project:** `.opencode/opencode.json` (registers local rules and skills to the project environment)
- **Tooling:** The `scripts/ai-setup.sh` script installs required MCP servers (like codebase-memory-mcp and graphify) and merges the required configurations so the AI agent recognizes the local rules and skills automatically.
