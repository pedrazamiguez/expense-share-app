package es.pedrazamiguez.expenseshareapp.features.expense.presentation.component.step.expense

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.component.CategorySection
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.component.DueDateSection
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.component.PaymentMethodSection
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.component.PaymentStatusSection
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.component.ReceiptSection
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.component.VendorNotesSection
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.viewmodel.event.AddExpenseUiEvent
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.viewmodel.state.AddExpenseUiState

/**
 * Step 3: Detail fields — payment method, category, vendor, notes,
 * payment status, due date, and receipt.
 * All fields are optional.
 */
@Composable
fun DetailsStep(
    uiState: AddExpenseUiState,
    onEvent: (AddExpenseUiEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(top = 24.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        PaymentMethodSection(
            paymentMethods = uiState.paymentMethods,
            selectedPaymentMethod = uiState.selectedPaymentMethod,
            onPaymentMethodSelected = { onEvent(AddExpenseUiEvent.PaymentMethodSelected(it)) }
        )

        CategorySection(
            availableCategories = uiState.availableCategories,
            selectedCategory = uiState.selectedCategory,
            onCategorySelected = { onEvent(AddExpenseUiEvent.CategorySelected(it)) }
        )

        VendorNotesSection(
            vendor = uiState.vendor,
            notes = uiState.notes,
            onVendorChanged = { onEvent(AddExpenseUiEvent.VendorChanged(it)) },
            onNotesChanged = { onEvent(AddExpenseUiEvent.NotesChanged(it)) }
        )

        PaymentStatusSection(
            availablePaymentStatuses = uiState.availablePaymentStatuses,
            selectedPaymentStatus = uiState.selectedPaymentStatus,
            onPaymentStatusSelected = { onEvent(AddExpenseUiEvent.PaymentStatusSelected(it)) }
        )

        AnimatedVisibility(visible = uiState.showDueDateSection) {
            DueDateSection(
                formattedDueDate = uiState.formattedDueDate,
                isDueDateValid = uiState.isDueDateValid,
                dueDateMillis = uiState.dueDateMillis,
                onDateSelected = { onEvent(AddExpenseUiEvent.DueDateSelected(it)) }
            )
        }

        ReceiptSection(
            receiptUri = uiState.receiptUri,
            onImageSelected = { onEvent(AddExpenseUiEvent.ReceiptImageSelected(it)) },
            onRemoveImage = { onEvent(AddExpenseUiEvent.RemoveReceiptImage) }
        )
    }
}
