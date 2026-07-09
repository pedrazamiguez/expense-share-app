import re

with open("Makefile", "r") as f:
    text = f.read()

doctor_checks = """	@# ─── AI Taxonomy ──────────────────────────────────────────────
	@if [ -d ".agents/rules" ] && [ $$(ls -1 .agents/rules/*.md 2>/dev/null | wc -l) -ge 15 ]; then \\
		printf "  $(GREEN)✅  .agents/rules/ present with enough files$(NC)\\n"; \\
	else \\
		printf "  $(YELLOW)⚠️   .agents/rules/ missing or incomplete$(NC)\\n"; \\
	fi
	@if [ -d ".agents/skills" ]; then \\
		printf "  $(GREEN)✅  .agents/skills/ present$(NC)\\n"; \\
	else \\
		printf "  $(YELLOW)⚠️   .agents/skills/ missing$(NC)\\n"; \\
	fi
	@if grep -q "rules" .opencode/opencode.json 2>/dev/null && grep -q "skills" .opencode/opencode.json 2>/dev/null; then \\
		printf "  $(GREEN)✅  .opencode/opencode.json has rules and skills$(NC)\\n"; \\
	else \\
		printf "  $(YELLOW)⚠️   .opencode/opencode.json missing rules or skills$(NC)\\n"; \\
	fi
	@# ─── AI Code Intelligence Tools ─────────────────────"""

text = text.replace("\t@# ─── AI Code Intelligence Tools ─────────────────────", doctor_checks)

with open("Makefile", "w") as f:
    f.write(text)
