package es.pedrazamiguez.splittrip.features.expense.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import es.pedrazamiguez.splittrip.core.designsystem.foundation.spacing
import es.pedrazamiguez.splittrip.core.designsystem.navigation.LocalBottomPadding
import es.pedrazamiguez.splittrip.core.designsystem.presentation.component.form.GradientButton
import es.pedrazamiguez.splittrip.core.designsystem.presentation.component.layout.DeferredLoadingContainer
import es.pedrazamiguez.splittrip.core.designsystem.presentation.component.layout.ShimmerLoadingList
import es.pedrazamiguez.splittrip.domain.model.ExpenseFilterCriteria
import es.pedrazamiguez.splittrip.features.expense.R
import es.pedrazamiguez.splittrip.features.expense.presentation.component.filter.CategoryFilterSection
import es.pedrazamiguez.splittrip.features.expense.presentation.component.filter.DateRangeFilterSection
import es.pedrazamiguez.splittrip.features.expense.presentation.component.filter.MemberFilterSection
import es.pedrazamiguez.splittrip.features.expense.presentation.viewmodel.state.ExpensesFilterUiState

private val STICKY_BUTTON_BOTTOM_SPACING = 80.dp

@Suppress("LongMethod")
@Composable
fun ExpensesFilterScreen(
    uiState: ExpensesFilterUiState = ExpensesFilterUiState(),
    onUpdateDraft: (ExpenseFilterCriteria) -> Unit = {},
    onApplyFilters: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val bottomPadding = LocalBottomPadding.current

    DeferredLoadingContainer(
        isLoading = uiState.isLoading,
        loadingContent = { ShimmerLoadingList() }
    ) {
        Box(modifier = modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = MaterialTheme.spacing.Default,
                    top = MaterialTheme.spacing.Default,
                    end = MaterialTheme.spacing.Default,
                    bottom = STICKY_BUTTON_BOTTOM_SPACING + bottomPadding
                ),
                verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.Large)
            ) {
                item(key = "category_section") {
                    CategoryFilterSection(
                        criteria = uiState.draftCriteria,
                        onCriteriaChange = onUpdateDraft,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item(key = "member_section") {
                    MemberFilterSection(
                        criteria = uiState.draftCriteria,
                        onCriteriaChange = onUpdateDraft,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item(key = "date_range_section") {
                    DateRangeFilterSection(
                        criteria = uiState.draftCriteria,
                        onCriteriaChange = onUpdateDraft,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(
                        start = MaterialTheme.spacing.Default,
                        end = MaterialTheme.spacing.Default,
                        bottom = MaterialTheme.spacing.Default + bottomPadding
                    )
            ) {
                val buttonText = stringResource(
                    R.string.expenses_filter_apply_count,
                    uiState.matchingExpensesCount
                )
                GradientButton(
                    text = buttonText,
                    onClick = onApplyFilters,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
