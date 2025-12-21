package es.pedrazamiguez.expenseshareapp.core.designsystem.foundation

import androidx.compose.ui.graphics.Color

// --- Refined Ocean Voyage Palette ---
// Inspired by sophisticated travel/finance apps with a modern Material 3 Expressive feel

// Primary: Deep Teal (Trust, Finance, Stability)
// Vibrant and confident
val OceanTeal = Color(0xFF00897B)           // Vibrant teal (slightly brighter)
val OceanTealLight = Color(0xFFB2DFDB)      // Soft mint for containers
val OceanTealDark = Color(0xFF4DB6AC)       // Brighter for dark mode visibility

// --- Secondary: Solar Gold (Replaces dull SlateBlue) ---
// Warm, inviting, represents "Value/Cost" without being grey
val SolarGold = Color(0xFFFFC107)           // Vibrant Amber/Gold
val SolarGoldContainer = Color(0xFFFFE082)  // Soft Sand
val SolarGoldDark = Color(0xFFFFD54F)       // Lighter Gold for Dark Mode

// --- Tertiary: Berry/Bougainvillea (Replaces ugly Coral) ---
// Expressive, fun, floral. Great for "New" tags or accents.
val BerryMagenta = Color(0xFFC2185B)        // Rich Pink/Magenta
val BerryContainer = Color(0xFFF48FB1)      // Soft Pink
val BerryDark = Color(0xFFF06292)           // Vibrant Pink for Dark Mode

// Backgrounds & Surfaces
// Clean but with subtle teal tint for cohesion
val SurfaceDay = Color(0xFFFAFDFC)          // Very subtle teal tint (was pure grey)
val SurfaceNight = Color(0xFF0F1514)        // Dark with subtle teal undertone

// Surface Container variants - TINTED with primary color for cohesion
// This is the key to avoiding the "grey" feeling
val SurfaceContainerLowest = Color(0xFFFFFFFF)
val SurfaceContainerLow = Color(0xFFF5F9F8)     // Subtle teal tint
val SurfaceContainer = Color(0xFFECF3F2)        // More visible teal tint
val SurfaceContainerHigh = Color(0xFFE0EEEC)    // Clear teal tint
val SurfaceContainerHighest = Color(0xFFD4E8E5) // Strong teal tint

// Dark Surface Container variants - tinted with teal
val SurfaceContainerLowestDark = Color(0xFF0A0F0E)
val SurfaceContainerLowDark = Color(0xFF151D1C)
val SurfaceContainerDark = Color(0xFF1C2625)
val SurfaceContainerHighDark = Color(0xFF263332)
val SurfaceContainerHighestDark = Color(0xFF313F3D)

// Error
val ErrorRed = Color(0xFFD32F2F)
val ErrorRedContainer = Color(0xFFFFCDD2)
