# Tabler Icons Maintenance Guide

This article documents how the SplitTrip project manages its icon set — from the initial migration away from Material 3 icons to day-to-day maintenance of the Tabler Icons constants.

---

## Architecture Overview

### Why Custom ImageVector Constants?

SplitTrip uses **hand-crafted Compose `ImageVector` constants** generated from [Tabler Icons](https://tabler.io/icons) SVGs, rather than a third-party library or Android drawable XMLs. This approach was chosen because:

1. **Zero blast radius** — All existing component signatures remain `ImageVector` (no `Painter` migration needed).
2. **Compile-time type safety** — Each icon is a statically typed Kotlin property.
3. **Minimal APK footprint** — Only the ~70 icons actually used are included (vs. the full 5,000+ Tabler set or the ~2.5 MB `material-icons-extended` library that was removed).
4. **Filled/outline variants** — Supports paired icon variants for navigation bar selected/unselected states.

### File Organisation

```
core/design-system/src/main/kotlin/es/pedrazamiguez/splittrip/core/designsystem/icon/
├── TablerIcons.kt                  ← Root object with Outline / Filled sub-objects
├── outline/
│   ├── NavigationIcons.kt          ← Arrows, chevrons, people, home
│   ├── ActionIcons.kt              ← Plus, edit, trash, search, check, etc.
│   ├── FinanceIcons.kt             ← Credit card, wallet, cash, currency, etc.
│   ├── StatusIcons.kt              ← Alert, info, lock, shield, clock, eye
│   ├── ContentIcons.kt             ← Mail, bell, camera, photo, etc.
│   └── MiscIcons.kt                ← Book, settings, language, layout, etc.
└── filled/
    └── FilledIcons.kt              ← Caret, home, lock, receipt, scale, user
```

Each file is a collection of **extension properties** on `TablerIcons.Outline` or `TablerIcons.Filled`. This keeps the root objects thin and allows per-file organisation that respects the **600-line Konsist limit**.

### Icon Property Pattern

Every icon follows the same cached-property pattern:

```kotlin
// Outline icon (stroke-based, 2px weight, round caps & joins)
val TablerIcons.Outline.Heart: ImageVector
    get() = _Heart ?: ImageVector.Builder(
        name = "Outline.Heart",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).addPath(
        pathData = addPathNodes("M19.5 12.572l-7.5 7.428l..."),
        stroke = SolidColor(Color.Black),
        strokeLineWidth = 2f,
        strokeLineCap = StrokeCap.Round,
        strokeLineJoin = StrokeJoin.Round
    ).build().also { _Heart = it }

private var _Heart: ImageVector? = null

// Filled icon (solid fill, no stroke)
val TablerIcons.Filled.HeartFilled: ImageVector
    get() = _HeartFilled ?: ImageVector.Builder(
        name = "Filled.HeartFilled",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).addPath(
        pathData = addPathNodes("M6.979 3.074a6 6 0 0 1 ..."),
        fill = SolidColor(Color.Black)
    ).build().also { _HeartFilled = it }

private var _HeartFilled: ImageVector? = null
```

Key characteristics:
- **`Color.Black` is a placeholder** — the actual colour is applied at render-time via `Icon(tint = ...)` or `LocalContentColor`.
- **Multi-path icons** chain `.addPath(...)` calls for each SVG `<path>` element.
- **Caching** uses the `_BackingField` pattern (lazy on first access, cached thereafter).

---

## Maintenance Script

The project includes an automated tool for icon management:

```
scripts/tabler-icons.py
```

### Prerequisites

- Python 3.10+ (uses `list[str]` type hints and `str | None` union syntax)
- Internet access (downloads SVGs from GitHub)

### Commands

#### `add` — Add a new icon

```bash
# Add an outline icon (appended to MiscIcons.kt by default)
./scripts/tabler-icons.py add outline heart Heart

# Add to a specific group file
./scripts/tabler-icons.py add outline heart Heart --file ContentIcons

# Add a filled variant
./scripts/tabler-icons.py add filled heart HeartFilled
```

The script will:
1. Check for duplicate names across all icon files.
2. Download the SVG from `github.com/tabler/tabler-icons`.
3. Extract `<path d="...">` data (skipping the transparent background path).
4. Generate the Kotlin `ImageVector` extension property.
5. Verify the target file stays under 600 lines.
6. Append the code block to the file.

#### `remove` — Remove an existing icon

```bash
./scripts/tabler-icons.py remove outline Heart
```

The script will:
1. Search all icon files in the style directory for the given name.
2. Remove the property + backing field code block.
3. Print a reminder to remove any imports from consuming files.

> **Important:** After removing an icon, search the codebase for any remaining references:
> ```bash
> grep -rn 'Heart' --include='*.kt' | grep -v build/
> ```

#### `update` — Re-download and regenerate

```bash
./scripts/tabler-icons.py update outline heart Heart
```

Use this when:
- Tabler has updated an icon's SVG paths (e.g., visual refresh).
- You want to ensure the local constant matches the latest upstream version.

The script removes the old code block and appends the freshly generated one.

#### `list` — List all registered icons

```bash
# List everything
./scripts/tabler-icons.py list

# Filter by style
./scripts/tabler-icons.py list --style outline
./scripts/tabler-icons.py list --style filled
```

Shows all icons grouped by file, with line counts and remaining capacity.

#### `check` — Validate file sizes

```bash
./scripts/tabler-icons.py check
```

Verifies all icon files stay under the **600-line Konsist limit**. Returns exit code 1 if any file exceeds the limit.

---

## Manual Procedures

### Adding an Icon (Manual Fallback)

If the script is unavailable, follow these steps:

1. **Find the Tabler icon** at [tabler.io/icons](https://tabler.io/icons).
2. **Download the SVG** from the [tabler-icons GitHub repo](https://github.com/tabler/tabler-icons/tree/main/icons):
   - Outline: `icons/outline/{name}.svg`
   - Filled: `icons/filled/{name}.svg`
3. **Extract the `<path d="...">` values** from the SVG. Skip the background path (`d="M0 0h24v24H0z"`).
4. **Choose a target file** based on the icon's category. Check remaining capacity with `wc -l` (must stay under 600 lines).
5. **Write the Kotlin code** following the pattern in [Icon Property Pattern](#icon-property-pattern) above.
6. **Run formatting**: `./gradlew ktlintFormat`
7. **Verify build**: `./gradlew :core:design-system:compileDebugKotlin`

### Choosing a Kotlin Property Name

| Convention | Example |
|---|---|
| Outline icons: PascalCase matching the Tabler name | `arrow-left` → `ArrowLeft` |
| Filled icons: PascalCase + `Filled` suffix | `heart` → `HeartFilled` |
| Compound names: join with PascalCase | `credit-card-pay` → `CreditCardPay` |
| Single-word icons: capitalize | `trash` → `Trash` |

### Choosing a Target File

| Category | File | Typical icons |
|---|---|---|
| Navigation, people, arrows | `NavigationIcons.kt` | ArrowLeft, ChevronDown, User, Home |
| CRUD actions, controls | `ActionIcons.kt` | Plus, Edit, Trash, Search, Check |
| Money, payments, data | `FinanceIcons.kt` | CreditCard, Wallet, Cash, ChartBar |
| Alerts, security, status | `StatusIcons.kt` | AlertTriangle, Lock, Shield, Eye |
| Communication, media | `ContentIcons.kt` | Mail, Bell, Camera, Photo |
| Everything else | `MiscIcons.kt` | Settings, Language, Book, Inbox |
| All filled variants | `FilledIcons.kt` | CaretDownFilled, HomeFilled |

If a file is approaching 600 lines (~550+), create a new group file (e.g., `ActionIcons2.kt` or a more descriptive name like `FormIcons.kt`).

### Moving an Icon Between Files

1. Cut the icon's code block (property + backing field) from the source file.
2. Paste it into the target file (before the final newline).
3. Run `./gradlew ktlintFormat` to fix import ordering.
4. No import changes needed in consuming files — the extension property package doesn't change within the same style.

> **Note:** Moving an icon between `outline/` and `filled/` directories **does** change the import path and requires updating all consumers.

### Removing the `material-icons-extended` Dependency

The `material-icons-extended` library (~2.5 MB) was removed as part of the initial migration. If you ever need to re-add it temporarily:

```kotlin
// core/design-system/build.gradle.kts
api(libs.material.icons.extended)
```

```toml
# gradle/libs.versions.toml
materialIconsExtended = "1.7.8"
material-icons-extended = { group = "androidx.compose.material", name = "material-icons-extended", version.ref = "materialIconsExtended" }
```

---

## Using Icons in Code

### Import Pattern

```kotlin
import es.pedrazamiguez.splittrip.core.designsystem.icon.TablerIcons
import es.pedrazamiguez.splittrip.core.designsystem.icon.outline.Heart
import es.pedrazamiguez.splittrip.core.designsystem.icon.filled.HeartFilled

// Usage
Icon(
    imageVector = TablerIcons.Outline.Heart,
    contentDescription = "Favourite"
)
```

### Navigation Bar Icons (Selected / Unselected Pairs)

For bottom navigation, use outline for unselected and either the same outline (with `NavigationBarIcon`'s built-in selected styling) or a filled variant for selected:

```kotlin
// Option A: Outline only (NavigationBarIcon handles selected styling)
icon = TablerIcons.Outline.UsersGroup

// Option B: Filled/outline pair
icon = if (isSelected) TablerIcons.Filled.ReceiptFilled else TablerIcons.Outline.Receipt
```

### Default Icon Parameters

When a composable accepts a default icon parameter, use the Tabler equivalent:

```kotlin
@Composable
fun EmptyStateView(
    title: String,
    icon: ImageVector = TablerIcons.Outline.Inbox,  // Not Icons.Outlined.Inbox
    // ...
)
```

---

## Complete Icon Inventory

Run `./scripts/tabler-icons.py list` for the current inventory, or see below for the initial mapping from Material 3:

<details>
<summary>Material 3 → Tabler Icon Mapping (77 icons)</summary>

| # | Material Icon | Tabler Equivalent |
|---|---|---|
| 1 | `Icons.Outlined.Groups` / `Groups2` | `TablerIcons.Outline.UsersGroup` |
| 2 | `Icons.Outlined.Group` | `TablerIcons.Outline.Users` |
| 3 | `Icons.Outlined.Person` | `TablerIcons.Outline.User` |
| 4 | `Icons.Outlined.Edit` | `TablerIcons.Outline.Edit` |
| 5 | `Icons.Outlined.Delete` | `TablerIcons.Outline.Trash` |
| 6 | `Icons.Outlined.Receipt` | `TablerIcons.Outline.Receipt` |
| 7 | `Icons.Outlined.Add` | `TablerIcons.Outline.Plus` |
| 8 | `Icons.Outlined.Settings` | `TablerIcons.Outline.Settings` |
| 9 | `Icons.Outlined.Email` | `TablerIcons.Outline.Mail` |
| 10 | `Icons.Outlined.Warning` | `TablerIcons.Outline.AlertTriangle` |
| 11 | `Icons.Default.Check` | `TablerIcons.Outline.Check` |
| 12 | `Icons.Default.Close` | `TablerIcons.Outline.X` |
| 13 | `Icons.Default.Search` | `TablerIcons.Outline.Search` |
| 14 | `Icons.Default.Refresh` | `TablerIcons.Outline.Refresh` |
| 15 | `Icons.Default.ArrowDropDown` | `TablerIcons.Filled.CaretDownFilled` |
| 16 | `Icons.Filled.Person` | `TablerIcons.Filled.UserFilled` |
| 17 | `Icons.Filled.Lock` | `TablerIcons.Filled.LockFilled` |
| 18 | `Icons.AutoMirrored.Filled.ArrowBack` | `TablerIcons.Outline.ArrowLeft` |
| 19 | `Icons.AutoMirrored.Filled.ArrowForward` | `TablerIcons.Outline.ArrowRight` |
| … | *(see full mapping in issue #868 comment)* | |

</details>

---

## Troubleshooting

### Build fails after adding an icon

1. Run `./gradlew ktlintFormat` — the script generates valid code but imports may need reordering.
2. Run `./gradlew :core:design-system:compileDebugKotlin` to isolate compilation issues.
3. Check that the target file doesn't exceed 600 lines: `./scripts/tabler-icons.py check`.

### Icon renders as a black square

The SVG path data may be malformed. Compare the generated `addPathNodes("...")` string with the original SVG's `<path d="...">` attribute. Ensure the transparent background path (`M0 0h24v24H0z`) was not included.

### Icon renders at wrong size

All Tabler icons use a 24×24 viewport. If the icon appears too large or small, check that the `Icon()` call doesn't override `modifier.size()` to a non-standard value.

### Script returns "Icon not found in Tabler"

- Verify the icon slug at [tabler.io/icons](https://tabler.io/icons).
- Some icons have different names in the URL vs. the display name (e.g., `layers-intersect` not `layers`).
- Not all outline icons have filled variants — check the Tabler site for availability.

### Konsist test fails with file-size violation

Split the oversized file into two. For example, rename `ActionIcons.kt` to keep existing icons and create `FormIcons.kt` for the new additions. No import changes needed in consumers since the extension property's import path is based on the property name, not the file name.

