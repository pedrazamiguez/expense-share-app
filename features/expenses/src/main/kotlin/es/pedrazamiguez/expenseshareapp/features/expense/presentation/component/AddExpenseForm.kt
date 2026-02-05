package es.pedrazamiguez.expenseshareapp.features.expense.presentation.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.component.input.StyledOutlinedTextField
import es.pedrazamiguez.expenseshareapp.features.expense.R
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.viewmodel.event.AddExpenseUiEvent
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.viewmodel.state.AddExpenseUiState

@Composable
fun AddExpenseForm(
    groupId: String?,
    uiState: AddExpenseUiState,
    onEvent: (AddExpenseUiEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- 1. TITLE ---
        StyledOutlinedTextField(
            value = uiState.expenseTitle,
            onValueChange = { onEvent(AddExpenseUiEvent.TitleChanged(it)) },
            label = stringResource(R.string.add_expense_what_for),
            modifier = Modifier.fillMaxWidth(),
            isError = !uiState.isTitleValid,
            capitalization = KeyboardCapitalization.Sentences
        )

        // --- 2. AMOUNT & CURRENCY ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Source Amount
            StyledOutlinedTextField(
                value = uiState.sourceAmount,
                onValueChange = { onEvent(AddExpenseUiEvent.SourceAmountChanged(it)) },
                label = stringResource(R.string.add_expense_amount_paid),
                modifier = Modifier.weight(1f),
                keyboardType = KeyboardType.Decimal,
                isError = !uiState.isAmountValid
            )

            // Currency Dropdown
            Box(modifier = Modifier.weight(0.4f)) {
                var expanded by remember { mutableStateOf(false) }
                StyledOutlinedTextField(
                    value = uiState.selectedCurrency?.code ?: "",
                    onValueChange = {},
                    readOnly = true,
                    label = stringResource(R.string.add_expense_currency_label),
                    trailingIcon = { Icon(Icons.Default.ArrowDropDown, null) },
                    onClick = { expanded = true },
                    modifier = Modifier.fillMaxWidth()
                )

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    uiState.availableCurrencies.forEach { currency ->
                        DropdownMenuItem(
                            text = { Text(currency.formatDisplay()) },
                            onClick = {
                                onEvent(AddExpenseUiEvent.CurrencySelected(currency))
                                expanded = false
                            }
                        )
                    }
                }
            }
        }

        // --- 3. CONVERSION CARD (Conditional) ---
        AnimatedVisibility(visible = uiState.showExchangeRateSection) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.add_expense_exchange_rate_title),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        if (uiState.isLoadingRate) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(14.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Rate Input - shows "1 EUR =" format
                        StyledOutlinedTextField(
                            value = uiState.displayExchangeRate,
                            onValueChange = { onEvent(AddExpenseUiEvent.ExchangeRateChanged(it)) },
                            label = stringResource(
                                R.string.add_expense_rate_label_format,
                                uiState.groupCurrency?.code ?: "",
                                uiState.selectedCurrency?.code ?: ""
                            ),
                            modifier = Modifier.weight(1f),
                            keyboardType = KeyboardType.Decimal
                        )

                        // Group Amount (Charged)
                        StyledOutlinedTextField(
                            value = uiState.calculatedGroupAmount,
                            onValueChange = { onEvent(AddExpenseUiEvent.GroupAmountChanged(it)) },
                            label = stringResource(
                                R.string.add_expense_amount_in,
                                uiState.groupCurrency?.code ?: ""
                            ),
                            modifier = Modifier.weight(1f),
                            keyboardType = KeyboardType.Decimal,
                        )
                    }
                }
            }
        }

        // --- 4. PAYMENT METHOD ---
        Text(
            text = stringResource(R.string.add_expense_payment_method_title),
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(top = 8.dp)
        )

        PaymentMethodChips(
            paymentMethods = uiState.paymentMethods,
            selectedPaymentMethod = uiState.selectedPaymentMethod,
            onPaymentMethodSelected = { onEvent(AddExpenseUiEvent.PaymentMethodSelected(it)) }
        )

        Spacer(modifier = Modifier.height(24.dp))

        // --- 5. ERROR MESSAGE (if any) ---
        val errorText = when {
            uiState.errorRes != null -> stringResource(uiState.errorRes)
            !uiState.errorMessage.isNullOrBlank() -> uiState.errorMessage
            else -> null
        }

        if (errorText != null) {
            Text(
                text = errorText,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )
        }

        // --- 6. SUBMIT BUTTON ---
        val isFormValid =
            uiState.isTitleValid && uiState.isAmountValid && uiState.expenseTitle.isNotBlank() && uiState.sourceAmount.isNotBlank()

        Button(
            onClick = { onEvent(AddExpenseUiEvent.SubmitAddExpense(groupId)) },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            enabled = isFormValid && !uiState.isLoading
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text(stringResource(R.string.add_expense_submit_button))
            }
        }

        // Bottom padding to ensure button is visible above bottom navigation
        Spacer(modifier = Modifier.height(80.dp))
    }
}
