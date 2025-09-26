package es.pedrazamiguez.expenseshareapp.ui.settings.presentation.view

import androidx.compose.ui.graphics.vector.ImageVector

data class SettingItemView(
    val icon: ImageVector,
    val title: String,
    val description: String? = null,
    val onClick: () -> Unit
)
