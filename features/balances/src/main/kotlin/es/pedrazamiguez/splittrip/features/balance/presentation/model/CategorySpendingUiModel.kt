package es.pedrazamiguez.splittrip.features.balance.presentation.model

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

data class CategorySpendingUiModel(
    val categoryName: String,
    val categoryIcon: ImageVector,
    val formattedAmount: String,
    val progress: Float,
    val color: Color
)
