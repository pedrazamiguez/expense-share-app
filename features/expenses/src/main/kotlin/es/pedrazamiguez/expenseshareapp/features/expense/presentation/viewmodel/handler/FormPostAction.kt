package es.pedrazamiguez.expenseshareapp.features.expense.presentation.viewmodel.handler

/**
 * Sealed interface representing cross-handler actions that [FormEventHandler]
 * needs to trigger after processing form field updates.
 *
 * Instead of directly injecting sibling handlers (which creates tight coupling),
 * [FormEventHandler] emits these actions via a callback, and the ViewModel
 * routes them to the appropriate handler.
 */
sealed interface FormPostAction {

    /** Recalculate exchange rate + splits after source amount change. */
    data class RecalculateAfterAmount(val isExchangeRateLocked: Boolean) : FormPostAction

    /** React to payment method change for exchange rate behavior. */
    data class PaymentMethodChanged(val isCash: Boolean, val isGroupPocket: Boolean) : FormPostAction

    /** React to funding source change for exchange rate behavior. */
    data class FundingSourceChanged(val isGroupPocket: Boolean) : FormPostAction
}
