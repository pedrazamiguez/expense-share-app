package es.pedrazamiguez.expenseshareapp.features.expense.presentation.model

import es.pedrazamiguez.expenseshareapp.domain.enums.AddOnMode
import es.pedrazamiguez.expenseshareapp.domain.enums.AddOnType
import es.pedrazamiguez.expenseshareapp.domain.enums.AddOnValueType

/**
 * UI model representing a single add-on in the expense form.
 *
 * [amountInput] is the raw user input (editable text field).
 * [resolvedAmountCents] is the computed absolute amount in add-on currency cents.
 * [groupAmountCents] is the amount converted to the group currency.
 */
data class AddOnUiModel(
    val id: String,
    val type: AddOnType = AddOnType.FEE,
    val mode: AddOnMode = AddOnMode.ON_TOP,
    val valueType: AddOnValueType = AddOnValueType.EXACT,

    /** Raw user input in the amount field (locale-aware string). */
    val amountInput: String = "",

    /** Resolved absolute amount in the add-on's own currency (cents). */
    val resolvedAmountCents: Long = 0L,

    /** The add-on's currency (defaults to expense source currency). */
    val currency: CurrencyUiModel? = null,

    /** Display exchange rate: "1 GroupCurrency = X AddOnCurrency". */
    val displayExchangeRate: String = "1.0",

    /** Amount converted to group currency (cents). */
    val groupAmountCents: Long = 0L,

    /** Payment method override (defaults to expense payment method). */
    val paymentMethod: PaymentMethodUiModel? = null,

    /** Optional free-text description. */
    val description: String = "",

    /** Whether the amount input is valid. */
    val isAmountValid: Boolean = true
)
