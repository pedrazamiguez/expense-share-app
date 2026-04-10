package es.pedrazamiguez.splittrip.core.designsystem.icon

/**
 * Root access point for Tabler Icons in Compose.
 *
 * Icons are organised by style:
 * - [TablerIcons.Outline] — Stroke-based 24×24 icons (2px weight, round caps & joins).
 * - [TablerIcons.Filled]  — Solid-fill 24×24 icons.
 *
 * Individual icons are declared as **extension properties** on the sub-objects
 * (e.g. `TablerIcons.Outline.ArrowLeft`). This keeps the sub-objects thin while
 * allowing per-file organisation that respects the 600-line Konsist limit.
 *
 * All icons originate from the [Tabler Icons](https://tabler.io/icons) open-source set
 * and are converted to Compose `ImageVector` constants via `ImageVector.Builder`.
 *
 * @see <a href="https://tabler.io/icons">Tabler Icons</a>
 */
object TablerIcons {

    /**
     * Stroke-based (outline) Tabler icons.
     * Extension properties are defined in the `icon.outline` package.
     */
    object Outline

    /**
     * Solid-fill Tabler icons.
     * Extension properties are defined in the `icon.filled` package.
     */
    object Filled
}
