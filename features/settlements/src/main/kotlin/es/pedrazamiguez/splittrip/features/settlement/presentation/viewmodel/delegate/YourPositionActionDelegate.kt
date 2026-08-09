package es.pedrazamiguez.splittrip.features.settlement.presentation.viewmodel.delegate

import es.pedrazamiguez.splittrip.core.common.presentation.UiText
import es.pedrazamiguez.splittrip.domain.usecase.balance.ConfirmSettlementUseCase
import es.pedrazamiguez.splittrip.domain.usecase.balance.DisputeSettlementUseCase
import es.pedrazamiguez.splittrip.domain.usecase.settlement.NudgeDebtorUseCase
import es.pedrazamiguez.splittrip.features.settlement.R
import es.pedrazamiguez.splittrip.features.settlement.presentation.viewmodel.action.YourPositionUiAction
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import timber.log.Timber

data class LocalUiState(
    val activeDisputeSettlementId: String? = null,
    val disputeReasonInput: String = ""
)

class YourPositionActionDelegate(
    private val confirmSettlementUseCase: ConfirmSettlementUseCase,
    private val disputeSettlementUseCase: DisputeSettlementUseCase,
    private val nudgeDebtorUseCase: NudgeDebtorUseCase
) {

    private val _localState = MutableStateFlow(LocalUiState())
    val localState: StateFlow<LocalUiState> = _localState

    suspend fun handleConfirm(
        settlementIdString: String,
        groupId: String?,
        isOffline: Boolean,
        actions: Channel<YourPositionUiAction>
    ) {
        if (checkOfflineAndEmitError(isOffline, actions)) return
        val validGroupId = groupId ?: return
        val settlementIds = settlementIdString.split(",")

        var hasError = false
        for (id in settlementIds) {
            confirmSettlementUseCase(validGroupId, id).fold(
                onSuccess = { /* continue */ },
                onFailure = { e ->
                    Timber.w(e, "Failed to confirm settlement $id")
                    hasError = true
                }
            )
        }

        if (hasError) {
            actions.send(
                YourPositionUiAction.ShowError(
                    UiText.StringResource(R.string.your_position_confirm_error)
                )
            )
        } else {
            actions.send(
                YourPositionUiAction.ShowSuccess(
                    UiText.StringResource(R.string.your_position_confirm_success)
                )
            )
        }
    }

    suspend fun handleNudgeDebtor(
        settlementId: String,
        groupId: String?,
        isOffline: Boolean,
        actions: Channel<YourPositionUiAction>
    ) {
        if (checkOfflineAndEmitError(isOffline, actions)) return
        val validGroupId = groupId ?: return

        nudgeDebtorUseCase(validGroupId, settlementId).fold(
            onSuccess = {
                actions.send(
                    YourPositionUiAction.ShowSuccess(
                        UiText.StringResource(R.string.your_position_nudge_success)
                    )
                )
            },
            onFailure = { e ->
                Timber.w(e, "Failed to send nudge for settlement $settlementId")
                actions.send(
                    YourPositionUiAction.ShowError(
                        UiText.StringResource(R.string.your_position_nudge_error)
                    )
                )
            }
        )
    }

    suspend fun handleOpenDispute(settlementId: String, isOffline: Boolean, actions: Channel<YourPositionUiAction>) {
        if (checkOfflineAndEmitError(isOffline, actions)) return
        _localState.update {
            it.copy(
                activeDisputeSettlementId = settlementId,
                disputeReasonInput = ""
            )
        }
    }

    suspend fun handleSubmitDispute(groupId: String?, isOffline: Boolean, actions: Channel<YourPositionUiAction>) {
        if (checkOfflineAndEmitError(isOffline, actions)) return
        val validGroupId = groupId ?: return
        val settlementId = _localState.value.activeDisputeSettlementId ?: return
        val reason = _localState.value.disputeReasonInput.trim()
        if (reason.isBlank()) return

        disputeSettlementUseCase(validGroupId, settlementId, reason).fold(
            onSuccess = {
                _localState.update {
                    it.copy(
                        activeDisputeSettlementId = null,
                        disputeReasonInput = ""
                    )
                }
                actions.send(
                    YourPositionUiAction.ShowSuccess(
                        UiText.StringResource(R.string.your_position_dispute_success)
                    )
                )
            },
            onFailure = { e ->
                Timber.w(e, "Failed to dispute settlement $settlementId")
                actions.send(
                    YourPositionUiAction.ShowError(
                        UiText.StringResource(R.string.your_position_dispute_error)
                    )
                )
            }
        )
    }

    private suspend fun checkOfflineAndEmitError(isOffline: Boolean, actions: Channel<YourPositionUiAction>): Boolean {
        if (isOffline) {
            actions.send(
                YourPositionUiAction.ShowError(
                    UiText.StringResource(R.string.your_position_offline_warning)
                )
            )
            return true
        }
        return false
    }

    fun updateDisputeReason(reason: String) {
        _localState.update {
            it.copy(disputeReasonInput = reason)
        }
    }

    fun handleCancelDispute() {
        _localState.update {
            it.copy(
                activeDisputeSettlementId = null,
                disputeReasonInput = ""
            )
        }
    }
}
