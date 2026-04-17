package es.pedrazamiguez.splittrip.domain.usecase.expense

import es.pedrazamiguez.splittrip.domain.enums.PayerType
import es.pedrazamiguez.splittrip.domain.model.WithdrawalPoolOption
import es.pedrazamiguez.splittrip.domain.repository.CashWithdrawalRepository

/**
 * Determines which withdrawal pools have available funds for a given cash expense configuration.
 *
 * When an expense's funding scope is GROUP, only the GROUP pool is ever relevant — no pool
 * selection UI is needed. When the scope is USER or SUBUNIT, both the personal/subunit pool
 * AND the GROUP pool may hold funds, so this use case probes each independently using
 * [CashWithdrawalRepository.getAvailableWithdrawalsByExactScope] (no fallback).
 *
 * The UI shows a pool-selection widget only when this use case returns more than one option.
 * When only one pool has funds, the caller should auto-select it silently so the submit path
 * is always uniform (preferred pool is always set before calling [AddExpenseUseCase]).
 *
 * @param cashWithdrawalRepository Repository for querying scoped withdrawal availability.
 */
class GetAvailableWithdrawalPoolsUseCase(
    private val cashWithdrawalRepository: CashWithdrawalRepository
) {
    /**
     * Returns the list of pools that have at least one withdrawal with `remainingAmount > 0`
     * for the given [groupId] / [currency] combination.
     *
     * @param groupId       The group the expense belongs to.
     * @param currency      The source currency of the expense (ISO 4217, e.g. "THB").
     * @param payerType     The expense's payer scope (GROUP / USER / SUBUNIT).
     * @param payerId       The userId for USER scope, or the subunitId for SUBUNIT scope.
     *                      Ignored for GROUP scope.
     * @return A list of [WithdrawalPoolOption] values with available cash, in priority order
     *         (personal/subunit pool first, GROUP pool second). Empty when no pool has funds.
     */
    suspend operator fun invoke(
        groupId: String,
        currency: String,
        payerType: PayerType,
        payerId: String? = null
    ): List<WithdrawalPoolOption> {
        // GROUP expenses only have one possible pool — no selection needed.
        if (payerType == PayerType.GROUP) {
            val groupPool = cashWithdrawalRepository.getAvailableWithdrawalsByExactScope(
                groupId = groupId,
                currency = currency,
                scope = PayerType.GROUP
            )
            return if (groupPool.isNotEmpty()) listOf(WithdrawalPoolOption(PayerType.GROUP)) else emptyList()
        }

        // USER / SUBUNIT: probe the personal/subunit pool and the GROUP pool independently.
        val personalPool = if (!payerId.isNullOrBlank()) {
            cashWithdrawalRepository.getAvailableWithdrawalsByExactScope(
                groupId = groupId,
                currency = currency,
                scope = payerType,
                scopeOwnerId = payerId
            )
        } else {
            emptyList()
        }

        val groupPool = cashWithdrawalRepository.getAvailableWithdrawalsByExactScope(
            groupId = groupId,
            currency = currency,
            scope = PayerType.GROUP
        )

        return buildList {
            if (personalPool.isNotEmpty()) add(WithdrawalPoolOption(payerType, payerId))
            if (groupPool.isNotEmpty()) add(WithdrawalPoolOption(PayerType.GROUP))
        }
    }
}
