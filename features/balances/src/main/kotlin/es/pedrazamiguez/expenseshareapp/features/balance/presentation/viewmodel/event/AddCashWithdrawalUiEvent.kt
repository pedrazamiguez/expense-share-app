package es.pedrazamiguez.expenseshareapp.features.balance.presentation.viewmodel.event

import es.pedrazamiguez.expenseshareapp.domain.enums.PayerType

sealed interface AddCashWithdrawalUiEvent {
    data class LoadGroupConfig(val groupId: String?) : AddCashWithdrawalUiEvent
    data class RetryLoadConfig(val groupId: String?) : AddCashWithdrawalUiEvent
    data class CurrencySelected(val currencyCode: String) : AddCashWithdrawalUiEvent
    data class WithdrawalAmountChanged(val amount: String) : AddCashWithdrawalUiEvent
    data class DeductedAmountChanged(val amount: String) : AddCashWithdrawalUiEvent
    data class ExchangeRateChanged(val rate: String) : AddCashWithdrawalUiEvent
    data class WithdrawalScopeSelected(val scope: PayerType, val subunitId: String? = null) : AddCashWithdrawalUiEvent
    data class SubmitWithdrawal(val groupId: String?) : AddCashWithdrawalUiEvent
}
