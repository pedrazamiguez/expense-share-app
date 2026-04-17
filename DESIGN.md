# Design System: The Horizon Narrative

## 1. Overview & Creative North Star
**Creative North Star: "The Curated Expedition"**

This design system rejects the "boxed-in" nature of traditional travel interfaces. Instead of a rigid, utility-first grid, we embrace an editorial perspective that treats every screen like a high-end travel journal. The aesthetic is defined by **Optimistic Kineticism**—using the energetic curves of Plus Jakarta Sans and a sophisticated layering of surfaces to create a sense of movement and discovery.

We move beyond the "template" look by utilizing intentional asymmetry, expansive whitespace, and "The Layering Principle." We don't just display information; we curate a destination. The goal is to make the user feel like they have already begun their journey the moment they open the app.
 
---

## 2. Colors & Surface Philosophy
The palette is rooted in deep, trustworthy blues (`primary`) and revitalizing teals (`secondary`), balanced by a warm, off-white foundation (`background`).

### The "No-Line" Rule
**Borders are a relic of the past.** In this system, 1px solid borders for sectioning are strictly prohibited. Boundaries must be defined through:
*   **Background Shifts:** Transitioning from `surface` to `surface-container-low`.
*   **Tonal Transitions:** Using `surface-container-highest` for high-density UI elements against a `surface-bright` background.

### Surface Hierarchy & Nesting
Treat the UI as a physical stack of premium materials.
*   **Level 0 (Foundation):** `surface` (#f9f9ff) — The base canvas.
*   **Level 1 (Sections):** `surface-container-low` (#f2f3fb) — Large layout blocks.
*   **Level 2 (Interaction):** `surface-container` (#ecedf6) — Secondary cards or grouping.
*   **Level 3 (Prominence):** `surface-container-lowest` (#ffffff) — Reserved for primary cards that need to "pop" via tonal contrast.

### The "Glass & Gradient" Rule
To evoke the atmospheric quality of travel (clouds, water, horizons), use **Glassmorphism** for floating headers or navigation bars.
*   **Recipe:** `surface` at 70% opacity + 20px Backdrop Blur.
*   **Signature Textures:** Apply a subtle linear gradient from `primary` (#00478d) to `primary-container` (#005eb8) for Hero CTAs. This adds a "soul" to the action that flat colors cannot replicate.

---

## 3. Typography
Our typography is a dialogue between inspiration and information.

*   **Display & Headlines (Plus Jakarta Sans):** These are our "Energetic Accents." Use `display-lg` and `headline-lg` with tight letter-spacing (-0.02em) to create an optimistic, bold editorial feel. These should often be placed with asymmetrical padding to break the grid.
*   **Body & UI Text (Manrope):** The "Functional Grounding." Manrope provides the clarity needed for travel logistics. Use `body-lg` for immersive descriptions and `label-md` for technical data points.
*   **The Hierarchy Goal:** Large, energetic headers capture the "dream," while clean, functional body text facilitates the "doing."

---

## 4. Elevation & Depth
We eschew the "Shadow-Heavy" look of 2010s Material Design in favor of **Tonal Layering**.

*   **The Layering Principle:** Depth is achieved by "stacking." A `surface-container-lowest` (#ffffff) card placed on a `surface-container-low` (#f2f3fb) section creates a soft, natural lift.
*   **Ambient Shadows:** For floating elements (like a FAB or navigation sheet), use an ultra-diffused shadow: `box-shadow: 0 12px 40px rgba(25, 28, 33, 0.06);`. The shadow must feel like ambient light, not a dark smudge.
*   **The "Ghost Border" Fallback:** If a container sits on an identical color (e.g., in dark mode or high-contrast scenarios), use a `outline-variant` (#c2c6d4) at 15% opacity. Never use 100% opacity for borders.

---

## 5. Components

### Buttons
*   **Primary:** Gradient fill (`primary` to `primary-container`), `full` roundedness, and `title-sm` typography.
*   **Secondary:** `secondary-container` background with `on-secondary-container` text. No border.
*   **Tertiary:** Ghost style using `on-surface-variant` text.

### Cards & Lists
*   **Forbid Dividers:** Do not use lines to separate list items. Use 16px or 24px vertical spacing or a 4px `md` rounded background hover state in `surface-container-high`.
*   **Image Cards:** Use `xl` (1.5rem) corner radius for destination images to lean into the "Welcoming" brand pillar.

### Inputs & Fields
*   **The "Soft Field":** Inputs use `surface-container-highest` background with no border. On focus, transition to an `outline` (#727783) "Ghost Border" at 20% opacity.

### Signature Component: The "Passport Chip"
*   For travel tags (e.g., "Non-stop," "Eco-friendly"), use `secondary-fixed-dim` background with `on-secondary-fixed` text. Shape: `full` roundedness. These should feel like collectible stamps.

---

## 6. Do’s and Don’ts

### Do:
*   **Embrace Asymmetry:** Place a `headline-lg` off-center to lead the eye through a destination photo.
*   **Use Tonal Depth:** Always check if a background color shift can replace a line or a shadow.
*   **Maximize Whitespace:** Travel is about breathing room; give your content 32px-48px of "air" between major sections.

### Don’t:
*   **Don't use pure black:** Use `on-surface` (#191c21) for text to keep the "Premium" feel soft.
*   **Don't use 1px borders:** They create visual noise and make the UI feel like a spreadsheet rather than an experience.
*   **Don't crowd the margins:** Travel interfaces fail when they feel claustrophobic. Avoid "Full Width" containers; use `lg` (1rem) or `xl` (1.5rem) internal padding.

### Accessibility Note:
While we use subtle tonal shifts, always ensure that `on-surface` text against `surface-container` tiers maintains a minimum 4.5:1 contrast ratio. Use the `error` (#ba1a1a) token sparingly for critical feedback, ensuring it is always accompanied by a `label-sm` for clarity.
