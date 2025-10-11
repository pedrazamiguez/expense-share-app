package es.pedrazamiguez.expenseshareapp.ui.expense.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import es.pedrazamiguez.expenseshareapp.ui.expense.R
import es.pedrazamiguez.expenseshareapp.ui.expense.presentation.model.AddExpenseUiEvent
import es.pedrazamiguez.expenseshareapp.ui.expense.presentation.model.AddExpenseUiState

@Composable
fun AddExpenseScreen(
    uiState: AddExpenseUiState,
    onEvent: (AddExpenseUiEvent) -> Unit = {},
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .verticalScroll(rememberScrollState())
                .imePadding()
        ) {

            OutlinedTextField(
                value = uiState.expenseTitle,
                onValueChange = { onEvent(AddExpenseUiEvent.TitleChanged(it)) },
                label = { Text(stringResource(R.string.expense_field_title)) },
                singleLine = true,
                isError = !uiState.isTitleValid,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Next
                ),
                modifier = Modifier.fillMaxWidth()
            )
            if (!uiState.isTitleValid) {
                Text(
                    text = stringResource(R.string.expense_field_title_required),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            OutlinedTextField(
                value = uiState.expenseAmount,
                onValueChange = { onEvent(AddExpenseUiEvent.AmountChanged(it)) },
                label = { Text(stringResource(R.string.expense_field_amount)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Next
                ),
                modifier = Modifier.fillMaxWidth()
            )
            if (!uiState.isAmountValid) {
                Text(
                    text = stringResource(R.string.expense_field_amount_required),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Button(
                onClick = { onEvent(AddExpenseUiEvent.SubmitAddExpense) },
                enabled = !uiState.isLoading && uiState.isTitleValid && uiState.isAmountValid,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(18.dp)
                    )
                } else {
                    Text(stringResource(R.string.expenses_add))
                }
            }

            if (uiState.error != null) {
                Text(
                    uiState.error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

        }
    }
}