package es.pedrazamiguez.expenseshareapp.core.designsystem.foundation

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

// Material Expressive favors rounded corners (approx 20dp-28dp for cards)
val Shapes = Shapes(
    extraSmall = RoundedCornerShape(12.dp),
    small = RoundedCornerShape(16.dp),
    medium = RoundedCornerShape(24.dp),     // Standard cards
    large = RoundedCornerShape(32.dp),      // Expressive cards / Dialogs
    extraLarge = RoundedCornerShape(48.dp)  // Bottom sheets
)
