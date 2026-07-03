package es.pedrazamiguez.splittrip.domain.usecase.balance

import es.pedrazamiguez.splittrip.domain.model.MemberBalance
import es.pedrazamiguez.splittrip.domain.model.Settlement
import es.pedrazamiguez.splittrip.domain.usecase.UseCase

interface GetSettlementSuggestionsUseCase : UseCase {
    /** Produces NET settlements — for the Balances dashboard. */
    operator fun invoke(memberBalances: List<MemberBalance>): List<Settlement>

    /** Produces per-pocket-type settlements — for leave-group / trip-close flows. */
    fun invokeByPocket(memberBalances: List<MemberBalance>, groupCurrency: String): List<Settlement>
}
