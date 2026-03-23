package es.pedrazamiguez.expenseshareapp.features.balance.presentation.viewmodel.handler

import es.pedrazamiguez.expenseshareapp.core.common.presentation.UiText
import es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.formatter.parseAmountToSmallestUnit
import es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.model.CurrencyUiModel
import es.pedrazamiguez.expenseshareapp.domain.enums.AddOnMode
import es.pedrazamiguez.expenseshareapp.domain.enums.AddOnType
import es.pedrazamiguez.expenseshareapp.domain.enums.AddOnValueType
import es.pedrazamiguez.expenseshareapp.domain.enums.PayerType
import es.pedrazamiguez.expenseshareapp.domain.enums.PaymentMethod
import es.pedrazamiguez.expenseshareapp.domain.model.AddOn
import es.pedrazamiguez.expenseshareapp.domain.model.CashWithdrawal
import es.pedrazamiguez.expenseshareapp.domain.service.CashWithdrawalValidationService
import es.pedrazamiguez.expenseshareapp.domain.service.ExpenseCalculatorService
import es.pedrazamiguez.expenseshareapp.domain.usecase.balance.AddCashWithdrawalUseCase
import es.pedrazamiguez.expenseshareapp.features.balance.R
import es.pedrazamiguez.expenseshareapp.features.balance.presentation.viewmodel.action.AddCashWithdrawalUiAction
import es.pedrazamiguez.expenseshareapp.features.balance.presentation.viewmodel.state.AddCashWithdrawalUiState
import java.math.BigDecimal
import java.util.UUID
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * Handles withdrawal submission and validation:
 * [SubmitWithdrawal].
 */
class WithdrawalSubmitHandler(
    private val addCashWithdrawalUseCase: AddCashWithdrawalUseCase,
    private val cashWithdrawalValidationService: CashWithdrawalValidationService,
    private val expenseCalculatorService: ExpenseCalculatorService
) : AddCashWithdrawalEventHandler {

    private lateinit var _uiState: MutableStateFlow<AddCashWithdrawalUiState>
    private lateinit var _actions: MutableSharedFlow<AddCashWithdrawalUiAction>
    private lateinit var scope: CoroutineScope

    override fun bind(
        stateFlow: MutableStateFlow<AddCashWithdrawalUiState>,
        actionsFlow: MutableSharedFlow<AddCashWithdrawalUiAction>,
        scope: CoroutineScope
    ) {
        _uiState = stateFlow
        _actions = actionsFlow
        this.scope = scope
    }

    fun submitWithdrawal(groupId: String?, onSuccess: () -> Unit) {
        if (groupId == null) return
        val state = _uiState.value
        val selectedCurrency = state.selectedCurrency ?: return
        val groupCurrency = state.groupCurrency ?: return

        val amountWithdrawn = parseAmountToSmallestUnit(
            state.withdrawalAmount,
            selectedCurrency.code
        )

        if (!validateInputs(state, amountWithdrawn, groupCurrency)) return

        val deductedBaseAmount = resolveDeductedAmount(state, amountWithdrawn, groupCurrency)
        val exchangeRate = resolveExchangeRate(state, amountWithdrawn, deductedBaseAmount)
        val addOns = buildFeeAddOn(state, groupCurrency)

        _uiState.update { it.copy(isLoading = true) }
        scope.launch {
            try {
                val withdrawal = CashWithdrawal(
                    groupId = groupId,
                    withdrawalScope = state.withdrawalScope,
                    subunitId = if (state.withdrawalScope == PayerType.SUBUNIT) {
                        state.selectedSubunitId
                    } else {
                        null
                    },
                    amountWithdrawn = amountWithdrawn,
                    remainingAmount = amountWithdrawn,
                    currency = selectedCurrency.code,
                    deductedBaseAmount = deductedBaseAmount,
                    exchangeRate = exchangeRate,
                    addOns = addOns,
                    title = state.title.trim().ifBlank { null },
                    notes = state.notes.trim().ifBlank { null }
                )
                addCashWithdrawalUseCase(groupId, withdrawal).getOrThrow()
                onSuccess()
            } catch (e: Exception) {
                Timber.e(e, "Failed to add cash withdrawal")
                _uiState.update { it.copy(isLoading = false) }
                _actions.emit(
                    AddCashWithdrawalUiAction.ShowError(
                        UiText.StringResource(R.string.balances_withdraw_cash_error)
                    )
                )
            }
        }
    }

    private fun validateInputs(
        state: AddCashWithdrawalUiState,
        amountWithdrawn: Long,
        groupCurrency: CurrencyUiModel
    ): Boolean {
        val amountValidation = cashWithdrawalValidationService.validateAmountWithdrawn(amountWithdrawn)
        if (amountValidation is CashWithdrawalValidationService.ValidationResult.Invalid) {
            _uiState.update { it.copy(isAmountValid = false) }
            return false
        }
        if (state.hasFee && state.feeAmount.isNotBlank()) {
            val feeCurrency = state.feeCurrency ?: groupCurrency
            val feeAmountCents = parseAmountToSmallestUnit(state.feeAmount, feeCurrency.code)
            if (feeAmountCents <= 0) {
                _uiState.update { it.copy(isFeeAmountValid = false) }
                return false
            }
        }
        return true
    }

    private fun resolveDeductedAmount(
        state: AddCashWithdrawalUiState,
        amountWithdrawn: Long,
        groupCurrency: CurrencyUiModel
    ): Long = if (state.showExchangeRateSection) {
        parseAmountToSmallestUnit(state.deductedAmount, groupCurrency.code)
    } else {
        amountWithdrawn
    }

    private fun resolveExchangeRate(
        state: AddCashWithdrawalUiState,
        amountWithdrawn: Long,
        deductedBaseAmount: Long
    ): BigDecimal = if (state.showExchangeRateSection) {
        expenseCalculatorService.calculateExchangeRate(
            amountWithdrawn = amountWithdrawn,
            deductedBaseAmount = deductedBaseAmount
        )
    } else {
        BigDecimal.ONE
    }

    /**
     * Builds the ATM fee add-on if user has enabled it and entered a valid amount.
     */
    private fun buildFeeAddOn(
        state: AddCashWithdrawalUiState,
        groupCurrency: CurrencyUiModel
    ): List<AddOn> {
        if (!state.hasFee || state.feeAmount.isBlank()) return emptyList()

        val feeCurrency = state.feeCurrency ?: groupCurrency
        val feeAmountCents = parseAmountToSmallestUnit(state.feeAmount, feeCurrency.code)
        if (feeAmountCents <= 0) return emptyList()

        val groupAmountCents = if (state.showFeeExchangeRateSection && state.feeConvertedAmount.isNotBlank()) {
            parseAmountToSmallestUnit(state.feeConvertedAmount, groupCurrency.code)
        } else {
            feeAmountCents
        }
        if (groupAmountCents <= 0) return emptyList()

        val feeExchangeRate = if (state.showFeeExchangeRateSection) {
            expenseCalculatorService.calculateExchangeRate(
                amountWithdrawn = feeAmountCents,
                deductedBaseAmount = groupAmountCents
            )
        } else {
            BigDecimal.ONE
        }

        return listOf(
            AddOn(
                id = UUID.randomUUID().toString(),
                type = AddOnType.FEE,
                mode = AddOnMode.ON_TOP,
                valueType = AddOnValueType.EXACT,
                amountCents = feeAmountCents,
                currency = feeCurrency.code,
                exchangeRate = feeExchangeRate,
                groupAmountCents = groupAmountCents,
                paymentMethod = PaymentMethod.OTHER,
                description = null
            )
        )
    }
}
