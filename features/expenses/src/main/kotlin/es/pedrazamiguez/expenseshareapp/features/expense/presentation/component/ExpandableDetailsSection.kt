package es.pedrazamiguez.expenseshareapp.features.expense.presentation.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.viewmodel.event.AddExpenseUiEvent
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.viewmodel.state.AddExpenseUiState

/**
 * Expandable details section of the Add Expense form.
 * Contains the progressive disclosure toggle and all detail fields:
 * Payment Method, Category, Split, Vendor, Notes, Payment Status, Due Date, and Receipt.
 *
 * The toggle state is managed internally with [rememberSaveable].
 */
@Composable
fun ExpandableDetailsSection(
    uiState: AddExpenseUiState,
    onEvent: (AddExpenseUiEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current
    var showDetails by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        DetailsToggleButton(
            showDetails = showDetails,
            onToggle = {
                focusManager.clearFocus()
                showDetails = !showDetails
            }
        )

        AnimatedVisibility(
            visible = showDetails,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
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

                if (uiState.availableSplitTypes.isNotEmpty() && uiState.memberIds.size > 1) {
                    SplitSection(uiState = uiState, onEvent = onEvent)
                }

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
                        onDateSelected = { millis -> onEvent(AddExpenseUiEvent.DueDateSelected(millis)) }
                    )
                }

                ReceiptSection(
                    receiptUri = uiState.receiptUri,
                    onImageSelected = { uri -> onEvent(AddExpenseUiEvent.ReceiptImageSelected(uri)) },
                    onRemoveImage = { onEvent(AddExpenseUiEvent.RemoveReceiptImage) }
                )
            }
        }
    }
}
