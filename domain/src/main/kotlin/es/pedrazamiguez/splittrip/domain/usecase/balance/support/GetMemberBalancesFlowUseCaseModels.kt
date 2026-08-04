package es.pedrazamiguez.splittrip.domain.usecase.balance.support

import es.pedrazamiguez.splittrip.domain.model.CashTransfer
import es.pedrazamiguez.splittrip.domain.model.CashWithdrawal
import es.pedrazamiguez.splittrip.domain.model.Contribution
import es.pedrazamiguez.splittrip.domain.model.Expense
import es.pedrazamiguez.splittrip.domain.model.SettlementRecord
import es.pedrazamiguez.splittrip.domain.model.Subunit
import es.pedrazamiguez.splittrip.domain.usecase.balance.strategy.ContributionAttributionStrategy
import es.pedrazamiguez.splittrip.domain.usecase.balance.strategy.StandardContributionAttributionStrategy

data class MemberBalanceCalculationInputs(
    val contributions: List<Contribution> = emptyList(),
    val withdrawals: List<CashWithdrawal> = emptyList(),
    val expenses: List<Expense> = emptyList(),
    val cashTransfers: List<CashTransfer> = emptyList(),
    val subunits: List<Subunit> = emptyList(),
    val groupMemberIds: List<String> = emptyList(),
    val groupCurrency: String = "",
    val settlements: List<SettlementRecord> = emptyList(),
    val attributionStrategy: ContributionAttributionStrategy = StandardContributionAttributionStrategy
)

/** Per-currency withdrawal attribution for a single member in a single currency. */
internal data class WithdrawalCurrencyAttribution(
    val nativeAmount: Long,
    val groupEquivalent: Long
)

/** Per-currency remaining-cash attribution for a single member in a single currency. */
internal data class RemainingCurrencyAttribution(
    val nativeRemaining: Long,
    val groupEquivalent: Long
)

/** Result of [attributeWithdrawals]. */
internal data class WithdrawalResult(
    /** Effective deducted amounts (including ATM fee add-ons) per member in group currency. */
    val groupCurrencyMap: Map<String, Long>,
    val byCurrency: Map<String, Map<String, WithdrawalCurrencyAttribution>>
)

/**
 * Result of [attributeRemainingByScope].
 *
 * [groupCurrencyMap] is the per-member group-currency equivalent of remaining cash
 * (used as the scalar `cashInHand`). [byCurrency] provides the per-currency
 * native remaining amounts for per-currency breakdown display.
 */
internal data class RemainingResult(
    val groupCurrencyMap: Map<String, Long>,
    val byCurrency: Map<String, Map<String, RemainingCurrencyAttribution>>
)

/** Result of [attributeExpensesByPaymentMethod]. */
internal data class ExpenseResult(
    val cashSpentMap: Map<String, Long>,
    val nonCashSpentMap: Map<String, Long>,
    val refundableSpentMap: Map<String, Long>,
    /** userId → sourceCurrency → native amountCents */
    val cashSpentByCurrency: Map<String, Map<String, Long>>,
    val nonCashSpentByCurrency: Map<String, Map<String, Long>>,
    val refundableSpentByCurrency: Map<String, Map<String, Long>>,
    /** userId → sourceCurrency → exact group-currency equivalent */
    val cashEquivByCurrency: Map<String, Map<String, Long>>,
    val nonCashEquivByCurrency: Map<String, Map<String, Long>>,
    val refundableEquivByCurrency: Map<String, Map<String, Long>>
)
