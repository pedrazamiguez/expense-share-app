package es.pedrazamiguez.splittrip.features.expense.presentation.component.filter

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import es.pedrazamiguez.splittrip.core.designsystem.foundation.spacing
import es.pedrazamiguez.splittrip.core.designsystem.presentation.component.chip.PassportChip
import es.pedrazamiguez.splittrip.core.designsystem.presentation.component.layout.SectionCard
import es.pedrazamiguez.splittrip.core.designsystem.presentation.extensions.toIconVector
import es.pedrazamiguez.splittrip.core.designsystem.presentation.extensions.toStringRes
import es.pedrazamiguez.splittrip.domain.enums.ExpenseCategory
import es.pedrazamiguez.splittrip.domain.enums.ExpenseSubcategory
import es.pedrazamiguez.splittrip.domain.model.ExpenseFilterCriteria
import es.pedrazamiguez.splittrip.features.expense.R

@Suppress("LongMethod", "CognitiveComplexMethod")
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CategoryFilterSection(
    criteria: ExpenseFilterCriteria,
    onCriteriaChange: (ExpenseFilterCriteria) -> Unit,
    modifier: Modifier = Modifier
) {
    SectionCard(
        title = stringResource(R.string.expenses_filter_section_category),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.Medium)
        ) {
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.Small),
                verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.Small)
            ) {
                ExpenseCategory.entries.forEach { category ->
                    val isSelected = category in criteria.selectedCategories
                    PassportChip(
                        label = stringResource(category.toStringRes()),
                        selected = isSelected,
                        onClick = {
                            onCriteriaChange(toggleCategory(criteria, category))
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = category.toIconVector(),
                                contentDescription = null
                            )
                        }
                    )
                }
            }

            AnimatedVisibility(
                visible = criteria.selectedCategories.isNotEmpty(),
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.Medium)
                ) {
                    criteria.selectedCategories.forEach { selectedCategory ->
                        val subcategories = ExpenseSubcategory.forCategory(selectedCategory)
                        if (subcategories.isNotEmpty()) {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.Small)
                            ) {
                                Text(
                                    text = stringResource(selectedCategory.toStringRes()),
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                FlowRow(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.Small),
                                    verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.Small)
                                ) {
                                    subcategories.forEach { subcategory ->
                                        val isSubSelected = subcategory in criteria.selectedSubcategories
                                        PassportChip(
                                            label = stringResource(subcategory.toStringRes()),
                                            selected = isSubSelected,
                                            onClick = {
                                                onCriteriaChange(toggleSubcategory(criteria, subcategory))
                                            },
                                            leadingIcon = {
                                                Icon(
                                                    imageVector = subcategory.toIconVector(),
                                                    contentDescription = null
                                                )
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun toggleCategory(
    criteria: ExpenseFilterCriteria,
    category: ExpenseCategory
): ExpenseFilterCriteria {
    val isSelected = category in criteria.selectedCategories
    val updatedCategories = if (isSelected) {
        criteria.selectedCategories - category
    } else {
        criteria.selectedCategories + category
    }
    val updatedSubcategories = if (isSelected) {
        criteria.selectedSubcategories.filterNot { it.parentCategory == category }.toSet()
    } else {
        criteria.selectedSubcategories
    }
    return criteria.copy(
        selectedCategories = updatedCategories,
        selectedSubcategories = updatedSubcategories
    )
}

private fun toggleSubcategory(
    criteria: ExpenseFilterCriteria,
    subcategory: ExpenseSubcategory
): ExpenseFilterCriteria {
    val isSelected = subcategory in criteria.selectedSubcategories
    val updatedSubcategories = if (isSelected) {
        criteria.selectedSubcategories - subcategory
    } else {
        criteria.selectedSubcategories + subcategory
    }
    return criteria.copy(selectedSubcategories = updatedSubcategories)
}
