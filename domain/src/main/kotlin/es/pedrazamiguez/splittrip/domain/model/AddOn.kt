package es.pedrazamiguez.splittrip.domain.model

import es.pedrazamiguez.splittrip.domain.enums.AddOnMode
import es.pedrazamiguez.splittrip.domain.enums.AddOnType
import es.pedrazamiguez.splittrip.domain.enums.AddOnValueType
import es.pedrazamiguez.splittrip.domain.enums.PaymentMethod
import java.math.BigDecimal

/**
 * Represents a structured add-on attached to an [Expense] or [CashWithdrawal].
 *
 * Add-ons track extra charges (fees, tips, surcharges) or reductions (discounts)
 * that are separate from the base item price. Each add-on can have its own currency,
 * exchange rate, and payment method — enabling scenarios like a bank fee in EUR
 * on a MXN boat trip, or a cash tip on a card-paid dinner.
 *
 * @param id Local UUID — generated client-side, never from Firestore `.add()`.
 * @param type The semantic category: TIP, FEE, DISCOUNT, or SURCHARGE.
 * @param mode How the add-on relates to the base amount:
 *   - [AddOnMode.ON_TOP]: added to the base (grows the effective total).
 *   - [AddOnMode.INCLUDED]: extracted from the user-entered total to derive the base cost.
 *     The add-on's [groupAmountCents] captures the extracted portion; the expense's
 *     `sourceAmount`/`groupAmount` store the remaining base cost.
 * @param valueType How the user originally entered the value:
 *   - [AddOnValueType.EXACT]: an absolute amount (e.g., "2.50 EUR").
 *   - [AddOnValueType.PERCENTAGE]: a percentage of the base (e.g., "10%").
 *   In both cases [amountCents] stores the resolved absolute amount.
 * @param amountCents The resolved absolute amount in the add-on's own currency (minor units).
 *   Always populated regardless of [valueType].
 * @param currency The add-on's currency code — can differ from the parent expense.
 * @param exchangeRate The rate from add-on currency → group currency. Stored as [BigDecimal]
 *   for precision; serialized as String at the Firestore boundary.
 * @param groupAmountCents The converted amount in the group's base currency (minor units).
 *   Used directly in balance calculations.
 * @param paymentMethod The payment method for this specific add-on — may differ from the parent expense.
 * @param description Optional free-text label (e.g., "ATM fee", "Service charge").
 */
data class AddOn(
    val id: String = "",
    val type: AddOnType = AddOnType.FEE,
    val mode: AddOnMode = AddOnMode.ON_TOP,
    val valueType: AddOnValueType = AddOnValueType.EXACT,
    val amountCents: Long = 0,
    val currency: String = "EUR",
    val exchangeRate: BigDecimal = BigDecimal.ONE,
    val groupAmountCents: Long = 0,
    val paymentMethod: PaymentMethod = PaymentMethod.OTHER,
    val description: String? = null
)
