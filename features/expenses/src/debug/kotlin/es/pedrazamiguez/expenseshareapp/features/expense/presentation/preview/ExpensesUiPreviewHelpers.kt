package es.pedrazamiguez.expenseshareapp.features.expense.presentation.preview

import androidx.compose.runtime.Composable
import es.pedrazamiguez.expenseshareapp.core.designsystem.preview.MappedPreview
import es.pedrazamiguez.expenseshareapp.domain.model.Expense
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.mapper.ExpenseUiMapper
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.model.ExpenseDateGroupUiModel
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.model.ExpenseUiModel
import kotlinx.collections.immutable.ImmutableList

@Composable
fun ExpenseItemPreviewHelper(
    domainExpense: Expense = PREVIEW_EXPENSE_BASIC,
    content: @Composable (ExpenseUiModel) -> Unit
) {
    MappedPreview(
        domain = domainExpense,
        mapper = { localeProvider, resourceProvider ->
            ExpenseUiMapper(localeProvider, resourceProvider)
        },
        transform = { mapper, domain ->
            mapper.map(domain)
        },
        content = content
    )
}

@Composable
fun ExpenseListPreviewHelper(
    domainExpenses: List<Expense> = PREVIEW_EXPENSES,
    content: @Composable (ImmutableList<ExpenseDateGroupUiModel>) -> Unit
) {
    MappedPreview(
        domain = domainExpenses,
        mapper = { localeProvider, resourceProvider ->
            ExpenseUiMapper(localeProvider, resourceProvider)
        },
        transform = { mapper, domain ->
            mapper.mapGroupedByDate(domain)
        },
        content = content
    )
}
