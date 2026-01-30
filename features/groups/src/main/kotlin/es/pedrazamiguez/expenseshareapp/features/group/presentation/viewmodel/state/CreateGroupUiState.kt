package es.pedrazamiguez.expenseshareapp.features.group.presentation.viewmodel.state

import androidx.annotation.StringRes
import es.pedrazamiguez.expenseshareapp.domain.model.Currency
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

data class CreateGroupUiState(
    val isLoading: Boolean = false,
    val isLoadingCurrencies: Boolean = false,
    val groupName: String = "",
    val groupDescription: String = "",
    val groupMembers: ImmutableList<String> = persistentListOf(),

    // Currency selection
    val availableCurrencies: ImmutableList<Currency> = persistentListOf(),
    val selectedCurrency: Currency? = null,
    val extraCurrencies: ImmutableList<Currency> = persistentListOf(),

    // Errors
    @field:StringRes
    val errorRes: Int? = null,
    val errorMessage: String? = null,
    val isNameValid: Boolean = true
)