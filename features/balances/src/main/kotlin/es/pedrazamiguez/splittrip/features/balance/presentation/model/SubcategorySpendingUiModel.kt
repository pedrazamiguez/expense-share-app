package es.pedrazamiguez.splittrip.features.balance.presentation.model

import androidx.compose.ui.graphics.vector.ImageVector

data class SubcategorySpendingUiModel(
    val subcategoryName: String,
    val subcategoryIcon: ImageVector,
    val formattedAmount: String,
    val percentageOfCategory: Int,
    val rawAmountCents: Long
)
