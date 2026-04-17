package es.pedrazamiguez.splittrip.features.expense.presentation.viewmodel.handler

import es.pedrazamiguez.splittrip.core.common.presentation.UiText
import es.pedrazamiguez.splittrip.core.designsystem.presentation.formatter.FormattingHelper
import es.pedrazamiguez.splittrip.domain.exception.InsufficientCashException
import es.pedrazamiguez.splittrip.features.expense.R
import es.pedrazamiguez.splittrip.features.expense.presentation.viewmodel.action.AddExpenseUiAction
import es.pedrazamiguez.splittrip.features.expense.presentation.viewmodel.state.AddExpenseUiState
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
    @Suppress("UnusedPrivateProperty")
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
     * For [InsufficientCashException], emits [AddExpenseUiAction.ShowCashConflictError]
     * so the Feature can show a conflict-specific message and refresh the tranche preview.
     * For all other exceptions, emits a generic failure message.
     */
    suspend fun handleFailure(
        error: Throwable,
        uiState: MutableStateFlow<AddExpenseUiState>,
        actionsFlow: MutableSharedFlow<AddExpenseUiAction>,
        currentState: AddExpenseUiState
    ) {
        uiState.update { it.copy(isLoading = false, error = null) }

        when (error) {
            is InsufficientCashException -> emitCashConflictError(actionsFlow)
            else -> actionsFlow.emit(
                AddExpenseUiAction.ShowError(
                    UiText.StringResource(R.string.expense_error_addition_failed)
                )
            )
        }
    }

    /**
     * Emits [AddExpenseUiAction.ShowCashConflictError] with a user-friendly conflict message.
     *
     * At save time, [InsufficientCashException] indicates that another group member consumed
     * cash between the preview snapshot and this submit. The conflict message instructs the
     * user to review the refreshed preview and retry.
     *
     * Kept internal so it can be unit-tested independently of [handleFailure].
     */
    internal suspend fun emitCashConflictError(
        actionsFlow: MutableSharedFlow<AddExpenseUiAction>
    ) {
        actionsFlow.emit(
            AddExpenseUiAction.ShowCashConflictError(
                UiText.StringResource(R.string.expense_error_cash_conflict)
            )
        )
    }
}
