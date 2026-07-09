import re

with open("scripts/ai-setup.sh", "r") as f:
    text = f.read()

new_step_8 = """# ─── Step 8: Merge MCP configs into opencode.jsonc ──────────────────────────
merge_opencode_config() {
  log_info "Merging MCP entry, rules, and skills into ${OPENCODE_CONFIG}..."
  python3 -c "
import os, re, json

path = os.path.expanduser('${OPENCODE_CONFIG}')
cbm_bin = os.path.expanduser('${CBM_BIN}')
project_root = os.path.expanduser('${PROJECT_ROOT}')

if not os.path.exists(path):
    print('  Config file not found — skipping')
    exit(0)

with open(path) as f:
    try:
        config = json.load(f)
    except json.JSONDecodeError:
        print('  Failed to parse opencode.jsonc (maybe it contains comments)')
        exit(0)

# Merge MCP
servers = config.setdefault('mcp', {})
if 'codebase-memory-mcp' not in servers:
    servers['codebase-memory-mcp'] = {
        'type': 'local',
        'command': [cbm_bin],
        'enabled': True
    }
    print('  Added codebase-memory-mcp to opencode.jsonc')

# Merge Rules and Skills
rules_path = os.path.join(project_root, '.agents/rules')
skills_path = os.path.join(project_root, '.agents/skills')

rules = config.setdefault('rules', [])
skills = config.setdefault('skills', [])

if rules_path not in rules:
    rules.append(rules_path)
    print('  Added rules path to opencode.jsonc')

if skills_path not in skills:
    skills.append(skills_path)
    print('  Added skills path to opencode.jsonc')

# Write back (Note: this drops comments, but opencode.jsonc is usually managed programmatically)
with open(path, 'w') as f:
    json.dump(config, f, indent=4)
"
  log_ok "Opencode config merged"
}"""

text = re.sub(r"# ─── Step 8: Merge MCP configs into opencode.jsonc ──────────────────────────.*?(?=# ─── Summary ─────────────────────────────────────────────────────────────────)", new_step_8 + "\n\n", text, flags=re.DOTALL)

with open("scripts/ai-setup.sh", "w") as f:
    f.write(text)
