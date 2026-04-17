package es.pedrazamiguez.splittrip.domain.model

import es.pedrazamiguez.splittrip.domain.enums.PayerType

/**
 * Represents a single available cash withdrawal pool that can fund a cash expense.
 *
 * Used by [es.pedrazamiguez.splittrip.domain.usecase.expense.GetAvailableWithdrawalPoolsUseCase]
 * to communicate which pools currently hold available funds for the given currency and scope.
 *
 * @param scope   The scope of the pool (GROUP, USER, or SUBUNIT).
 * @param ownerId The userId for USER-scoped pools, or the subunitId for SUBUNIT-scoped pools.
 *                Null for GROUP-scoped pools (GROUP cash belongs to the whole group).
 */
data class WithdrawalPoolOption(
    val scope: PayerType,
    val ownerId: String? = null
)
