package es.pedrazamiguez.expenseshareapp.features.balance.presentation.viewmodel.state

import es.pedrazamiguez.expenseshareapp.features.balance.presentation.model.ActivityItemUiModel
import es.pedrazamiguez.expenseshareapp.features.balance.presentation.model.CashWithdrawalUiModel
import es.pedrazamiguez.expenseshareapp.features.balance.presentation.model.ContributionUiModel
import es.pedrazamiguez.expenseshareapp.features.balance.presentation.model.GroupPocketBalanceUiModel
import es.pedrazamiguez.expenseshareapp.features.balance.presentation.model.MemberBalanceUiModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

data class BalancesUiState(
    val isLoading: Boolean = true,
    val groupId: String? = null,
    val pocketBalance: GroupPocketBalanceUiModel = GroupPocketBalanceUiModel(),
    val contributions: ImmutableList<ContributionUiModel> = persistentListOf(),
    val cashWithdrawals: ImmutableList<CashWithdrawalUiModel> = persistentListOf(),
    val memberBalances: ImmutableList<MemberBalanceUiModel> = persistentListOf(),
    val activityItems: ImmutableList<ActivityItemUiModel> = persistentListOf(),
    val shouldAnimateBalance: Boolean = false,
    val previousBalance: String = "",
    val balanceRollingUp: Boolean = true,
    val errorMessage: String? = null
)
