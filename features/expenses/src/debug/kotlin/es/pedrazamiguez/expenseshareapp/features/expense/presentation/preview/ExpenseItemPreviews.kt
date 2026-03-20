package es.pedrazamiguez.expenseshareapp.features.expense.presentation.preview

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import es.pedrazamiguez.expenseshareapp.core.designsystem.preview.PreviewLocales
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.component.ExpenseItem

@PreviewLocales
@Composable
private fun ExpenseItemBasicPreview() {
    ExpenseItemPreviewHelper(domainExpense = PREVIEW_EXPENSE_BASIC) {
        ExpenseItem(
            expenseUiModel = it,
            modifier = Modifier.padding(16.dp)
        )
    }
}

@PreviewLocales
@Composable
private fun ExpenseItemForeignCurrencyPreview() {
    ExpenseItemPreviewHelper(domainExpense = PREVIEW_EXPENSE_FOREIGN_CURRENCY) {
        ExpenseItem(
            expenseUiModel = it,
            modifier = Modifier.padding(16.dp)
        )
    }
}

@PreviewLocales
@Composable
private fun ExpenseItemScheduledPreview() {
    ExpenseItemPreviewHelper(domainExpense = PREVIEW_EXPENSE_SCHEDULED) {
        ExpenseItem(
            expenseUiModel = it,
            modifier = Modifier.padding(16.dp)
        )
    }
}

@PreviewLocales
@Composable
private fun ExpenseItemWithVendorPreview() {
    ExpenseItemPreviewHelper(domainExpense = PREVIEW_EXPENSE_WITH_VENDOR) {
        ExpenseItem(
            expenseUiModel = it,
            modifier = Modifier.padding(16.dp)
        )
    }
}
