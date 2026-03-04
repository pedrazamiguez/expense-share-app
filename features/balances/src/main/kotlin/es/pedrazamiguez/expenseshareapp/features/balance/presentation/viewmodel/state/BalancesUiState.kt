package es.pedrazamiguez.expenseshareapp.features.balance.presentation.viewmodel.state

import es.pedrazamiguez.expenseshareapp.features.balance.presentation.model.CashWithdrawalUiModel
import es.pedrazamiguez.expenseshareapp.features.balance.presentation.model.ContributionUiModel
import es.pedrazamiguez.expenseshareapp.features.balance.presentation.model.GroupPocketBalanceUiModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

data class BalancesUiState(
    val isLoading: Boolean = true,
    val groupId: String? = null,
    val pocketBalance: GroupPocketBalanceUiModel = GroupPocketBalanceUiModel(),
    val contributions: ImmutableList<ContributionUiModel> = persistentListOf(),
    val cashWithdrawals: ImmutableList<CashWithdrawalUiModel> = persistentListOf(),
    val isAddMoneyDialogVisible: Boolean = false,
    val contributionAmountInput: String = "",
    val contributionAmountError: Boolean = false,
    val errorMessage: String? = null
)
