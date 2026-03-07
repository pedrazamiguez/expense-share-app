package es.pedrazamiguez.expenseshareapp.features.balance.presentation.preview

import es.pedrazamiguez.expenseshareapp.domain.model.CashWithdrawal
import es.pedrazamiguez.expenseshareapp.domain.model.Contribution
import es.pedrazamiguez.expenseshareapp.domain.model.GroupPocketBalance
import java.math.BigDecimal
import java.time.LocalDateTime

val PREVIEW_GROUP_NAME = "Thai 2.0"

val PREVIEW_POCKET_BALANCE = GroupPocketBalance(
    totalContributions = 120000L,
    totalExpenses = 16545L,
    virtualBalance = 103455L,
    currency = "EUR",
    cashBalances = mapOf("THB" to 1000000L),
    cashEquivalents = mapOf("THB" to 27000L)
)

val PREVIEW_POCKET_BALANCE_EMPTY = GroupPocketBalance(
    totalContributions = 0L,
    totalExpenses = 0L,
    virtualBalance = 0L,
    currency = "EUR"
)

val PREVIEW_CASH_WITHDRAWAL_1 = CashWithdrawal(
    id = "cw1",
    groupId = "group-1",
    withdrawnBy = "user-1",
    amountWithdrawn = 1000000L,
    remainingAmount = 770000L,
    currency = "THB",
    deductedBaseAmount = 27000L,
    exchangeRate = BigDecimal("37.037"),
    createdAt = LocalDateTime.of(2026, 1, 16, 14, 0)
)

val PREVIEW_CONTRIBUTION_1 = Contribution(
    id = "c1",
    groupId = "group-1",
    userId = "Antonio",
    amount = 30000L,
    currency = "EUR",
    createdAt = LocalDateTime.of(2026, 1, 15, 10, 30)
)

val PREVIEW_CONTRIBUTION_2 = Contribution(
    id = "c2",
    groupId = "group-1",
    userId = "Maria",
    amount = 30000L,
    currency = "EUR",
    createdAt = LocalDateTime.of(2026, 1, 15, 11, 0)
)

val PREVIEW_CONTRIBUTION_3 = Contribution(
    id = "c3",
    groupId = "group-1",
    userId = "Pedro",
    amount = 30000L,
    currency = "EUR",
    createdAt = LocalDateTime.of(2026, 1, 14, 9, 15)
)

val PREVIEW_CONTRIBUTION_4 = Contribution(
    id = "c4",
    groupId = "group-1",
    userId = "Laura",
    amount = 30000L,
    currency = "EUR",
    createdAt = LocalDateTime.of(2026, 1, 14, 14, 45)
)

val PREVIEW_CONTRIBUTIONS = listOf(
    PREVIEW_CONTRIBUTION_1,
    PREVIEW_CONTRIBUTION_2,
    PREVIEW_CONTRIBUTION_3,
    PREVIEW_CONTRIBUTION_4
)

