package es.pedrazamiguez.splittrip.features.expense.presentation.model

import androidx.compose.ui.graphics.vector.ImageVector

data class CategoryUiModel(
    val id: String,
    val displayText: String,
    val icon: ImageVector? = null
)
