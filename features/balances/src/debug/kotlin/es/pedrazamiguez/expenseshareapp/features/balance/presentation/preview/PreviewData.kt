package es.pedrazamiguez.expenseshareapp.features.balance.presentation.preview

import es.pedrazamiguez.expenseshareapp.domain.model.Contribution
import es.pedrazamiguez.expenseshareapp.domain.model.GroupPocketBalance
import java.time.LocalDateTime

val PREVIEW_GROUP_NAME = "Thai 2.0"

val PREVIEW_POCKET_BALANCE = GroupPocketBalance(
    totalContributions = 120000L,
    totalExpenses = 16545L,
    balance = 103455L,
    currency = "EUR"
)

val PREVIEW_POCKET_BALANCE_EMPTY = GroupPocketBalance(
    totalContributions = 0L,
    totalExpenses = 0L,
    balance = 0L,
    currency = "EUR"
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

