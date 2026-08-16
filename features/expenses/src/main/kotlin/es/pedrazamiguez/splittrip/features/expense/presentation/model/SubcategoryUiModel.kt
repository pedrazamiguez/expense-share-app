package es.pedrazamiguez.splittrip.features.expense.presentation.model

import androidx.compose.ui.graphics.vector.ImageVector
import es.pedrazamiguez.splittrip.core.common.presentation.UiText
import es.pedrazamiguez.splittrip.domain.enums.ExpenseSubcategory

data class SubcategoryUiModel(
    val subcategory: ExpenseSubcategory,
    val name: UiText,
    val icon: ImageVector
)
