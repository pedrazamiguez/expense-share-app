package es.pedrazamiguez.expenseshareapp.features.group.presentation.viewmodel.state

import androidx.annotation.StringRes
import es.pedrazamiguez.expenseshareapp.domain.model.Currency

data class CreateGroupUiState(
    val isLoading: Boolean = false,
    val isLoadingCurrencies: Boolean = false,
    val groupName: String = "",
    val groupDescription: String = "",
    val groupMembers: List<String> = emptyList(),

    // Currency selection
    val availableCurrencies: List<Currency> = emptyList(),
    val selectedCurrency: Currency? = null,
    val extraCurrencies: List<Currency> = emptyList(),

    // Errors
    @field:StringRes
    val errorRes: Int? = null,
    val errorMessage: String? = null,
    val isNameValid: Boolean = true
)