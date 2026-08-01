# UI Clicks & Navigation — Debouncing

> [!IMPORTANT]
> **Always use debounced modifiers for UI navigation or expensive side-effects.**

To prevent multiple rapid navigations or API calls from "double tapping" UI components:
- NEVER use standard `Modifier.clickable` or `Modifier.combinedClickable` directly on list items, action buttons, or FABs.
- ALWAYS use `Modifier.debouncedClickable` and `Modifier.debouncedCombinedClickable` from `es.pedrazamiguez.splittrip.core.designsystem.extension.ModifierExtensions`.

**Why:** Compose can process multiple touch events before a navigation transition completes, pushing the destination route multiple times onto the back stack.

### Examples

```kotlin
// ❌ BAD
Modifier.clickable { navigate() }

// ✅ GOOD
Modifier.debouncedClickable { navigate() }
```

```kotlin
// ❌ BAD
Modifier.combinedClickable(
    onClick = { navigateToDetail() },
    onLongClick = { showOptions() }
)

// ✅ GOOD
Modifier.debouncedCombinedClickable(
    onClick = { navigateToDetail() },
    onLongClick = { showOptions() }
)
```
