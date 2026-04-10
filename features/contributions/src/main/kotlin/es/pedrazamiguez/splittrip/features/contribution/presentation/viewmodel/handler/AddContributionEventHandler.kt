package es.pedrazamiguez.splittrip.features.contribution.presentation.viewmodel.handler

import es.pedrazamiguez.splittrip.features.contribution.presentation.viewmodel.action.AddContributionUiAction
import es.pedrazamiguez.splittrip.features.contribution.presentation.viewmodel.state.AddContributionUiState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * Base contract for event handlers that manage a sub-domain of AddContribution logic.
 *
 * Handlers are plain classes (NOT ViewModels) that receive the shared state
 * and actions flows to read/write UI state. They are injected into the
 * [AddContributionViewModel][es.pedrazamiguez.splittrip.features.contribution.presentation.viewmodel.AddContributionViewModel]
 * via co-creation in the Koin `viewModel` block.
 */
interface AddContributionEventHandler {

    /**
     * Binds the handler to the ViewModel's shared state and action flows.
     * Must be called once during ViewModel initialisation, before any event handling.
     */
    fun bind(
        stateFlow: MutableStateFlow<AddContributionUiState>,
        actionsFlow: MutableSharedFlow<AddContributionUiAction>,
        scope: CoroutineScope
    )
}
