package es.pedrazamiguez.splittrip.features.balance.presentation.model

import androidx.compose.ui.graphics.Color
import es.pedrazamiguez.splittrip.domain.enums.ExpenseCategory

data class CategorySpendingUiModel(
    val category: ExpenseCategory,
    val formattedAmount: String,
    val progress: Float,
    val color: Color
)
