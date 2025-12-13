package es.pedrazamiguez.expenseshareapp.core.designsystem

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

// Material Expressive favors rounded corners (approx 20dp-28dp for cards)
val Shapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(16.dp),     // Standard cards
    large = RoundedCornerShape(24.dp),      // Expressive cards / Dialogs
    extraLarge = RoundedCornerShape(32.dp)  // Bottom sheets
)
