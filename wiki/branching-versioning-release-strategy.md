
# Workflow & CI/CD

This project follows a strict **GitFlow** workflow fully automated via GitHub Actions. We prioritize branch protection, ensuring that no direct pushes are ever made to `develop` or `main`.

## 🚀 The Golden Rule

**Never modify `version.properties` manually.** The CI/CD pipelines manage version numbers based on the branch names and PR labels.

---

## ♻️ The Development Lifecycle

### 1. Start with an Issue

* Open a **GitHub Issue** describing the Feature, Bug, or Release.
* **Automation:** The bot creates a branch for you using the format `type/EXSHAPP-xxxx`.
* **Auto-PR:** For **Releases**, the bot *automatically* opens the Pull Request to `main`. For other types, you open the PR manually when ready.

| Issue Label | Branch Created | Base Branch | Target PR Branch |
| --- | --- | --- | --- |
| `feature` | `feature/EXSHAPP-xxxx` | `develop` | `develop` |
| `bug` | `bugfix/EXSHAPP-xxxx` | `develop` | `develop` |
| `enhancement` | `refactor/EXSHAPP-xxxx` | `develop` | `develop` |
| `config` | `internal/EXSHAPP-xxxx` | `develop` | `develop` |
| `release` | `release/EXSHAPP-xxxx` | `develop` | **`main`** (Automatic) |
| `hotfix` | `hotfix/EXSHAPP-xxxx` | `main` | **`main`** |

### 2. Standard Development (Features/Bugs/Refactors)

* Checkout the bot-created branch.
* Work and push commits.
* Open a PR to `develop` manually when ready.

---

## 📦 The Release Process (Step-by-Step)

We treat releases as a specific type of issue.

### Phase 1: Preparation

1. Create an Issue titled **"Release v0.12.0"** (or similar) with label **`release`**.
2. **Bot Action:**
* Creates branch `release/EXSHAPP-xxxx` from `develop`.
* **Automatically opens a PR** from this branch to `main`.


3. **Bot Action (in PR):**
* The *Prepare Version* workflow runs on the `release/` branch (which is unprotected).
* It removes `-SNAPSHOT` from `version.properties`.
* It applies a **Major** version bump if the `major` label is detected on the PR.
* It commits this change to the PR.



### Phase 2: Publishing

1. Review the PR (ensure checks pass).
2. **Merge** the PR into `main`.
3. **Bot Action:** The *Create Release* workflow triggers on `main`:
* Builds the signed APK.
* Publishes a GitHub Release with an automated Changelog based on merged PRs.



### Phase 3: Sync Back (Backmerge)

Once `main` is updated, we must update `develop` with the new version tag and prepare for the next cycle.

1. **Bot Action:** The *Sync* workflow triggers automatically.
* Creates a temporary branch `sync/main-to-develop-xxx`.
* Merges `main` into it.
* Bumps the version (e.g., `0.12.0` -> `0.13.0-SNAPSHOT`).
* **Opens a PR** to `develop`.


2. **Manual Action:** You review and merge this "Sync" PR to update `develop`.

---

## 🚑 Hotfix Process

1. Create an Issue with label **`hotfix`**.
2. **Bot Action:** Creates `hotfix/` branch from `main` (No Auto-PR is created).
3. Fix the bug and push to that branch.
4. **Manual Action:** Open a PR to `main`.
5. **Bot Action (in PR):** Automatically increments the **Patch** version (e.g., `0.12.0` -> `0.12.1`) and removes the snapshot flag.
6. Merge to `main` -> Triggers Release -> Triggers Sync PR to `develop`.

---

## 🛡️ Branch Protection

* **`main`**: Protected. No direct pushes.
* **`develop`**: Protected. No direct pushes.
* **`release/*`, `hotfix/*`, `feature/*`, `refactor/*`, `internal/***`: Unprotected (Bots can write here).
