package es.pedrazamiguez.expenseshareapp.features.expense.presentation.viewmodel.handler

import es.pedrazamiguez.expenseshareapp.core.common.presentation.UiText
import es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.formatter.FormattingHelper
import es.pedrazamiguez.expenseshareapp.domain.exception.InsufficientCashException
import es.pedrazamiguez.expenseshareapp.features.expense.R
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.viewmodel.action.AddExpenseUiAction
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.viewmodel.state.AddExpenseUiState
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

/**
 * Stateless delegate that handles the post-submit result processing for expenses.
 *
 * Extracted from [SubmitEventHandler] to reduce cognitive complexity.
 * Handles the success/failure branching after [AddExpenseUseCase] completes,
 * including saving last-used preferences on success and emitting error actions
 * on failure (with special handling for [InsufficientCashException]).
 */
class SubmitResultDelegate(
    private val saveLastUsedPreferences: SaveLastUsedPreferencesBundle,
    private val formattingHelper: FormattingHelper
) {

    /**
     * Handles a successful expense submission: persists user preferences
     * for currency, payment method, and category, then invokes [onSuccess].
     */
    suspend fun handleSuccess(
        uiState: MutableStateFlow<AddExpenseUiState>,
        groupId: String,
        onSuccess: () -> Unit
    ) {
        uiState.value.selectedCurrency?.code?.let { code ->
            runCatching { saveLastUsedPreferences.setGroupLastUsedCurrencyUseCase(groupId, code) }
        }
        uiState.value.selectedPaymentMethod?.id?.let { id ->
            runCatching { saveLastUsedPreferences.setGroupLastUsedPaymentMethodUseCase(groupId, id) }
        }
        uiState.value.selectedCategory?.id?.let { id ->
            runCatching { saveLastUsedPreferences.setGroupLastUsedCategoryUseCase(groupId, id) }
        }
        uiState.update { it.copy(isLoading = false) }
        onSuccess()
    }

    /**
     * Handles a failed expense submission: clears loading state, then emits
     * an appropriate error action via [actionsFlow].
     *
     * For [InsufficientCashException], formats a detailed message showing
     * required vs available amounts. For all other exceptions, emits a
     * generic failure message.
     */
    suspend fun handleFailure(
        error: Throwable,
        uiState: MutableStateFlow<AddExpenseUiState>,
        actionsFlow: MutableSharedFlow<AddExpenseUiAction>,
        currentState: AddExpenseUiState
    ) {
        uiState.update { it.copy(isLoading = false, error = null) }

        when (error) {
            is InsufficientCashException -> emitInsufficientCashError(error, actionsFlow, currentState)
            else -> actionsFlow.emit(
                AddExpenseUiAction.ShowError(
                    UiText.StringResource(R.string.expense_error_addition_failed)
                )
            )
        }
    }

    internal suspend fun emitInsufficientCashError(
        error: InsufficientCashException,
        actionsFlow: MutableSharedFlow<AddExpenseUiAction>,
        currentState: AddExpenseUiState
    ) {
        val cashCurrency = currentState.selectedCurrency
        if (cashCurrency != null) {
            val required = formattingHelper.formatCentsWithCurrency(error.requiredCents, cashCurrency.code)
            val available = formattingHelper.formatCentsWithCurrency(error.availableCents, cashCurrency.code)
            actionsFlow.emit(
                AddExpenseUiAction.ShowError(
                    UiText.StringResource(R.string.expense_error_insufficient_cash, required, available)
                )
            )
        } else {
            actionsFlow.emit(
                AddExpenseUiAction.ShowError(
                    UiText.StringResource(R.string.expense_error_addition_failed)
                )
            )
        }
    }
}
