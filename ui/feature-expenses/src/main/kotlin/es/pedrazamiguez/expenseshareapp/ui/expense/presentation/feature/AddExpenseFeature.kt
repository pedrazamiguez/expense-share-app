package es.pedrazamiguez.expenseshareapp.ui.expense.presentation.feature

import androidx.compose.runtime.Composable
import es.pedrazamiguez.expenseshareapp.ui.expense.presentation.viewmodel.AddExpenseViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun AddExpenseFeature(
    addExpenseViewModel: AddExpenseViewModel = koinViewModel<AddExpenseViewModel>(),
    onAddExpenseSuccess: () -> Unit = {}
) {

}