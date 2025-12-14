package es.pedrazamiguez.expenseshareapp.core.designsystem.theme

import androidx.compose.ui.graphics.Color

// --- Refined Ocean Voyage Palette ---
// Inspired by sophisticated travel/finance apps with a modern Material 3 Expressive feel

// Primary: Deep Teal (Trust, Finance, Stability)
// Vibrant and confident
val OceanTeal = Color(0xFF00897B)           // Vibrant teal (slightly brighter)
val OceanTealLight = Color(0xFFB2DFDB)      // Soft mint for containers
val OceanTealDark = Color(0xFF4DB6AC)       // Brighter for dark mode visibility

// Secondary: Warm Slate (Professional, grounded)
// Warmer undertones for a more inviting feel
val SlateBlue = Color(0xFF546E7A)           // Blue-grey with warmth
val SlateBlueContainer = Color(0xFFCFD8DC)  // Light grey-blue container
val SlateBlueDark = Color(0xFF90A4AE)       // Brighter slate for dark mode

// Tertiary: Warm Coral/Amber (Action, Expenses, Energy)
// This is the "punch" color - used for amounts, FABs, key actions
val AdventureCoral = Color(0xFFE64A19)      // Deeper, richer coral-orange
val AdventureCoralContainer = Color(0xFFFFCCBC) // Soft peach container
val AdventureCoralDark = Color(0xFFFF8A65)  // Vibrant coral for dark mode

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
