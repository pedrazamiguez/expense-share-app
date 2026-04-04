package es.pedrazamiguez.expenseshareapp.features.expense.presentation.viewmodel.handler

import es.pedrazamiguez.expenseshareapp.core.common.presentation.UiText
import es.pedrazamiguez.expenseshareapp.domain.enums.PayerType
import es.pedrazamiguez.expenseshareapp.domain.enums.PaymentMethod
import es.pedrazamiguez.expenseshareapp.domain.enums.PaymentStatus
import es.pedrazamiguez.expenseshareapp.features.expense.R
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.mapper.AddExpenseUiMapper
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.viewmodel.action.AddExpenseUiAction
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.viewmodel.state.AddExpenseUiState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

/**
 * Handles simple form field events that contain inline branching logic:
 * [SourceAmountChanged], [PaymentMethodSelected], [FundingSourceSelected],
 * [PaymentStatusSelected], [DueDateSelected], and other direct state updates.
 *
 * Cross-handler coordination (e.g., recalculating splits after amount changes)
 * is routed via [formPostCallback], following the same pattern as
 * [ConfigEventHandler]'s [PostConfigAction] callback.
 */
class FormEventHandler(
    private val addExpenseUiMapper: AddExpenseUiMapper
) : AddExpenseEventHandler {

    private lateinit var _uiState: MutableStateFlow<AddExpenseUiState>
    private lateinit var _actions: MutableSharedFlow<AddExpenseUiAction>
    private lateinit var scope: CoroutineScope

    /**
     * Callback for post-form-update actions that require cross-handler communication.
     * Set by the ViewModel during initialization via [setFormPostCallback].
     */
    private var formPostCallback: ((FormPostAction) -> Unit)? = null

    override fun bind(
        stateFlow: MutableStateFlow<AddExpenseUiState>,
        actionsFlow: MutableSharedFlow<AddExpenseUiAction>,
        scope: CoroutineScope
    ) {
        _uiState = stateFlow
        _actions = actionsFlow
        this.scope = scope
    }

    /**
     * Registers the callback the ViewModel uses to route post-form-update actions
     * to the appropriate sibling handlers.
     */
    fun setFormPostCallback(callback: (FormPostAction) -> Unit) {
        formPostCallback = callback
    }

    fun handleTitleChanged(title: String) {
        _uiState.update {
            it.copy(
                expenseTitle = title,
                isTitleValid = true,
                error = null
            )
        }
    }

    fun handleSourceAmountChanged(amount: String) {
        _uiState.update {
            it.copy(
                sourceAmount = amount,
                isAmountValid = true,
                error = null
            )
        }
        formPostCallback?.invoke(
            FormPostAction.RecalculateAfterAmount(_uiState.value.isExchangeRateLocked)
        )
    }

    fun handlePaymentMethodSelected(methodId: String) {
        val selectedMethod = _uiState.value.paymentMethods
            .find { it.id == methodId } ?: return
        _uiState.update { it.copy(selectedPaymentMethod = selectedMethod) }

        val isCash = try {
            PaymentMethod.fromString(selectedMethod.id) == PaymentMethod.CASH
        } catch (_: IllegalArgumentException) {
            false
        }
        val isGroupPocket = _uiState.value.selectedFundingSource?.id
            ?.let {
                runCatching { PayerType.fromString(it) }
                    .getOrDefault(PayerType.GROUP) == PayerType.GROUP
            }
            ?: true
        formPostCallback?.invoke(FormPostAction.PaymentMethodChanged(isCash, isGroupPocket))
    }

    fun handleFundingSourceSelected(fundingSourceId: String) {
        val selectedSource = _uiState.value.fundingSources
            .find { it.id == fundingSourceId } ?: return
        val isUserMoney = fundingSourceId == PayerType.USER.name
        _uiState.update {
            it.copy(
                selectedFundingSource = selectedSource,
                fundingSourceHint = if (isUserMoney) {
                    UiText.StringResource(R.string.funding_source_my_money_hint)
                } else {
                    null
                },
                contributionScope = if (isUserMoney) it.contributionScope else PayerType.USER,
                selectedContributionSubunitId = if (isUserMoney) {
                    it.selectedContributionSubunitId
                } else {
                    null
                }
            ).withStepClamped()
        }
        formPostCallback?.invoke(FormPostAction.FundingSourceChanged(isGroupPocket = !isUserMoney))
    }

    fun handleContributionScopeSelected(scope: PayerType, subunitId: String?) {
        _uiState.update {
            it.copy(
                contributionScope = scope,
                selectedContributionSubunitId = subunitId
            )
        }
    }

    fun handleCategorySelected(categoryId: String) {
        val selectedCategory = _uiState.value.availableCategories
            .find { it.id == categoryId } ?: return
        _uiState.update { it.copy(selectedCategory = selectedCategory) }
    }

    fun handleVendorChanged(vendor: String) {
        _uiState.update { it.copy(vendor = vendor) }
    }

    fun handleNotesChanged(notes: String) {
        _uiState.update { it.copy(notes = notes) }
    }

    fun handlePaymentStatusSelected(statusId: String) {
        val selectedStatus = _uiState.value.availablePaymentStatuses
            .find { it.id == statusId } ?: return
        val isScheduled = statusId == PaymentStatus.SCHEDULED.name
        _uiState.update {
            it.copy(
                selectedPaymentStatus = selectedStatus,
                showDueDateSection = isScheduled,
                dueDateMillis = if (isScheduled) it.dueDateMillis else null,
                formattedDueDate = if (isScheduled) it.formattedDueDate else "",
                isDueDateValid = true
            )
        }
    }

    fun handleDueDateSelected(dateMillis: Long) {
        val formattedDate = addExpenseUiMapper.formatDueDateForDisplay(dateMillis)
        _uiState.update {
            it.copy(
                dueDateMillis = dateMillis,
                formattedDueDate = formattedDate,
                isDueDateValid = true
            )
        }
    }

    fun handleReceiptImageSelected(uri: String) {
        _uiState.update { it.copy(receiptUri = uri) }
    }

    fun handleRemoveReceiptImage() {
        _uiState.update { it.copy(receiptUri = null) }
    }
}
