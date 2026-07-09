import re

with open("AGENTS.md", "r") as f:
    agents_text = f.read()

# Add Source of Truth notice at the top
if "# Source of Truth" not in agents_text:
    agents_text = agents_text.replace(
        "# AGENTS.md — SplitTrip",
        "# AGENTS.md — SplitTrip\n\n> [!IMPORTANT]\n> **Source of Truth:** This file is the single source of truth for architectural constraints and agent behaviors. Rules are broken out into `.agents/rules/`."
    )

# Replace the specific rules with references to the .agents/rules directory
agents_text = re.sub(
    r"## Architecture Constraints\n\n(.*?)\n\n## Navigation",
    "## Architecture Constraints\n\nSee the standalone rule files in `.agents/rules/` for detailed constraints:\n- `.agents/rules/viewmodel-rules.md`\n- `.agents/rules/mvi-triad.md`\n- `.agents/rules/formatting-in-mappers.md`\n- `.agents/rules/big-decimal-math.md`\n- `.agents/rules/file-size-limit.md`\n- `.agents/rules/enum-centralization.md`\n- `.agents/rules/single-composable-per-file.md`\n- `.agents/rules/feature-screen-pattern.md`\n\n## Navigation",
    agents_text,
    flags=re.DOTALL
)

agents_text = re.sub(
    r"## AI Agent Behavior Rules \(CRITICAL\)\n\n(.*?)\n\n## Workspace Resolution Protocol",
    "## AI Agent Behavior Rules (CRITICAL)\n\nSee the standalone rule files in `.agents/rules/` for behavioral constraints:\n- `.agents/rules/no-git-operations.md`\n- `.agents/rules/no-pragmatic-patches.md`\n- `.agents/rules/make-check-gate.md`\n- `.agents/rules/commenting-policy.md`\n- `.agents/rules/agent-plan-strict-stop.md`\n\n## Workspace Resolution Protocol",
    agents_text,
    flags=re.DOTALL
)

# Add unique content from copilot-instructions.md
extra_testing = """
### Coroutine Testing (CRITICAL - Prevents Flaky Tests)

When testing classes that launch background coroutines (e.g., Repositories with `syncScope.launch {}`), you **MUST** inject the `CoroutineDispatcher` to ensure deterministic test behavior.
- Always inject `CoroutineDispatcher` into classes that launch background coroutines.
- Provide a default (`= Dispatchers.IO`) so production code doesn't need to specify it.
- Use `StandardTestDispatcher()` in tests and pass it to both the class and `runTest()`.
- Call `runTest(testDispatcher)` - the dispatcher must match what's used in the class.
- Call `advanceUntilIdle()` before assertions to ensure background work completes.
"""

if "Coroutine Testing" not in agents_text:
    agents_text = agents_text.replace("## Testing\n", "## Testing\n" + extra_testing + "\n")

extra_offline = """
### 🛑 The "True Offline" Write Protocol

We use a strictly **"Offline-First"** approach. The UI only observes the Local DB. The Cloud is a replication target, not the source of truth for the UI.
1. **Local ID Generation:** NEVER let Firestore generate the ID. ALWAYS generate a `UUID` locally.
2. **Local Metadata Generation:** Generate `createdAt = System.currentTimeMillis()` locally.
3. **Repository Write Order:** Save to Room (Local) FIRST -> Launch Background Job -> Sync to Cloud (Upsert).

**Critical: Subcollection Cleanup on Deletion**
Firestore does **NOT** auto-delete subcollections when a parent document is deleted. If a real-time listener watches a subcollection, you **MUST** delete subcollection documents **BEFORE** the parent document.
"""

if "True Offline" not in agents_text:
    agents_text = agents_text.replace("## Offline-First Data Flow\n", "## Offline-First Data Flow\n" + extra_offline + "\n")

with open("AGENTS.md", "w") as f:
    f.write(agents_text)
