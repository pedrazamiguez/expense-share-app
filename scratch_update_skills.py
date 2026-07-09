import os
import re

skills_dir = ".agents/skills"

for skill_folder in os.listdir(skills_dir):
    skill_file = os.path.join(skills_dir, skill_folder, "SKILL.md")
    if not os.path.exists(skill_file):
        continue

    with open(skill_file, "r") as f:
        text = f.read()

    # Find the YAML frontmatter
    match = re.match(r"^---\n(.*?)\n---\n(.*)", text, re.DOTALL)
    if not match:
        continue

    frontmatter = match.group(1)
    body = match.group(2)

    # Replace URL arguments with nothing
    frontmatter = re.sub(r"\s*- name: issue_url\n\s*description:.*?\n\s*required: (true|false)\n", "\n", frontmatter)
    frontmatter = re.sub(r"\s*- name: pr_url\n\s*description:.*?\n\s*required: (true|false)\n", "\n", frontmatter)
    frontmatter = re.sub(r"\s*- name: issue_url_or_number\n\s*description:.*?\n\s*required: (true|false)\n", "\n", frontmatter)

    # Make issue_number and pr_number required
    frontmatter = re.sub(r"(- name: issue_number\n\s*description:.*?)\n\s*required: false", r"\1\n    required: true", frontmatter)
    frontmatter = re.sub(r"(- name: pr_number\n\s*description:.*?)\n\s*required: false", r"\1\n    required: true", frontmatter)

    # In body, replace references to URLs
    body = re.sub(r"- Issue URL: \$ISSUE_URL\n", "", body)
    body = re.sub(r"- PR URL: \$PR_URL\n", "", body)
    body = re.sub(r"- Issue: \$ISSUE_URL_OR_NUMBER\n", "- Issue Number: $ISSUE_NUMBER\n", body)

    # Also make sure there is an issue_number or pr_number block if we removed the url_or_number
    if "sp-replan-issue" in skill_folder and "issue_number" not in frontmatter:
        # Add issue_number
        frontmatter = frontmatter.replace(
            "arguments:",
            "arguments:\n  - name: issue_number\n    description: The number of the GitHub issue to replan.\n    required: true"
        )
    
    # Write it back
    new_text = f"---\n{frontmatter}\n---{body}"
    
    # Remove any extra blank lines created in frontmatter
    new_text = new_text.replace("\n\n---\n", "\n---\n")

    with open(skill_file, "w") as f:
        f.write(new_text)

print("Updated skills.")
