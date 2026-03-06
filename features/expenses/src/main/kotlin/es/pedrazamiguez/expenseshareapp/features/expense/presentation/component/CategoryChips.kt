package es.pedrazamiguez.expenseshareapp.features.expense.presentation.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.model.CategoryUiModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CategoryChips(
    categories: List<CategoryUiModel>,
    selectedCategory: CategoryUiModel?,
    onCategorySelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    FlowRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        categories.forEach { category ->
            val isSelected = selectedCategory?.id == category.id
            FilterChip(
                selected = isSelected,
                onClick = { onCategorySelected(category.id) },
                label = { Text(category.displayText) },
                leadingIcon = if (isSelected) {
                    { Icon(Icons.Default.Check, null) }
                } else null
            )
        }
    }
}

