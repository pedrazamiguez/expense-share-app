package es.pedrazamiguez.expenseshareapp.features.balance.presentation.preview

import es.pedrazamiguez.expenseshareapp.domain.enums.PayerType
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

// ── Cash Withdrawals ────────────────────────────────────────────────────────

val PREVIEW_CASH_WITHDRAWAL_GROUP = CashWithdrawal(
    id = "cw1",
    groupId = "group-1",
    withdrawnBy = "user-1",
    withdrawalScope = PayerType.GROUP,
    amountWithdrawn = 1000000L,
    remainingAmount = 770000L,
    currency = "THB",
    deductedBaseAmount = 27000L,
    exchangeRate = BigDecimal("37.037"),
    createdAt = LocalDateTime.of(2026, 1, 16, 14, 0)
)

val PREVIEW_CASH_WITHDRAWAL_SUBUNIT = CashWithdrawal(
    id = "cw2",
    groupId = "group-1",
    withdrawnBy = "user-1",
    withdrawalScope = PayerType.SUBUNIT,
    subunitId = "subunit-1",
    amountWithdrawn = 500000L,
    remainingAmount = 500000L,
    currency = "THB",
    deductedBaseAmount = 13500L,
    exchangeRate = BigDecimal("37.037"),
    createdAt = LocalDateTime.of(2026, 1, 17, 10, 0)
)

val PREVIEW_CASH_WITHDRAWAL_PERSONAL = CashWithdrawal(
    id = "cw3",
    groupId = "group-1",
    withdrawnBy = "user-1",
    withdrawalScope = PayerType.USER,
    amountWithdrawn = 200000L,
    remainingAmount = 200000L,
    currency = "THB",
    deductedBaseAmount = 5400L,
    exchangeRate = BigDecimal("37.037"),
    createdAt = LocalDateTime.of(2026, 1, 18, 9, 0)
)

// ── Contributions ───────────────────────────────────────────────────────────

val PREVIEW_CONTRIBUTION_GROUP = Contribution(
    id = "c1",
    groupId = "group-1",
    userId = "Antonio",
    contributionScope = PayerType.GROUP,
    amount = 30000L,
    currency = "EUR",
    createdAt = LocalDateTime.of(2026, 1, 15, 10, 30)
)

val PREVIEW_CONTRIBUTION_SUBUNIT = Contribution(
    id = "c2",
    groupId = "group-1",
    userId = "Maria",
    contributionScope = PayerType.SUBUNIT,
    subunitId = "subunit-1",
    amount = 30000L,
    currency = "EUR",
    createdAt = LocalDateTime.of(2026, 1, 15, 11, 0)
)

val PREVIEW_CONTRIBUTION_PERSONAL = Contribution(
    id = "c3",
    groupId = "group-1",
    userId = "Pedro",
    contributionScope = PayerType.USER,
    amount = 30000L,
    currency = "EUR",
    createdAt = LocalDateTime.of(2026, 1, 14, 9, 15)
)

val PREVIEW_CONTRIBUTION_4 = Contribution(
    id = "c4",
    groupId = "group-1",
    userId = "Laura",
    contributionScope = PayerType.GROUP,
    amount = 30000L,
    currency = "EUR",
    createdAt = LocalDateTime.of(2026, 1, 14, 14, 45)
)

val PREVIEW_CONTRIBUTIONS = listOf(
    PREVIEW_CONTRIBUTION_GROUP,
    PREVIEW_CONTRIBUTION_SUBUNIT,
    PREVIEW_CONTRIBUTION_PERSONAL,
    PREVIEW_CONTRIBUTION_4
)
