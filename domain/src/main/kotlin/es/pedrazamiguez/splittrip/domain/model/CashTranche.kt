package es.pedrazamiguez.splittrip.domain.model

/**
 * Tracks exactly which ATM withdrawal funded a portion of a cash expense.
 *
 * When an expense is paid in CASH, the FIFO algorithm generates one or more
 * CashTranches linking the expense to the specific withdrawals it consumed.
 * This allows accurate restoration if the expense is deleted.
 *
 * @param withdrawalId The ID of the CashWithdrawal that was consumed.
 * @param amountConsumed The amount consumed from this withdrawal (in minor units / cents).
 */
data class CashTranche(val withdrawalId: String = "", val amountConsumed: Long = 0)
