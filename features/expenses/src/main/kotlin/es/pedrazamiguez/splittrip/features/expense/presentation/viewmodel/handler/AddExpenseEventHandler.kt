package es.pedrazamiguez.splittrip.features.expense.presentation.viewmodel.handler

import es.pedrazamiguez.splittrip.features.expense.presentation.viewmodel.action.AddExpenseUiAction
import es.pedrazamiguez.splittrip.features.expense.presentation.viewmodel.state.AddExpenseUiState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * Base contract for event handlers that manage a sub-domain of AddExpense logic.
 *
 * Handlers are plain classes (NOT ViewModels) that receive the shared state
 * and actions flows to read/write UI state. They are injected into the
 * [AddExpenseViewModel] as `factory` scoped Koin dependencies.
 */
interface AddExpenseEventHandler {

    /**
     * Binds the handler to the ViewModel's shared state and action flows.
     * Must be called once during ViewModel initialisation, before any event handling.
     */
    fun bind(
        stateFlow: MutableStateFlow<AddExpenseUiState>,
        actionsFlow: MutableSharedFlow<AddExpenseUiAction>,
        scope: CoroutineScope
    )
}
