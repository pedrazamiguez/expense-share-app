package es.pedrazamiguez.expenseshareapp.features.expense.presentation.preview

import es.pedrazamiguez.expenseshareapp.domain.enums.ExpenseCategory
import es.pedrazamiguez.expenseshareapp.domain.enums.PayerType
import es.pedrazamiguez.expenseshareapp.domain.enums.PaymentMethod
import es.pedrazamiguez.expenseshareapp.domain.enums.PaymentStatus
import es.pedrazamiguez.expenseshareapp.domain.model.Expense
import es.pedrazamiguez.expenseshareapp.domain.model.User
import java.math.BigDecimal
import java.time.LocalDateTime

// ── Expense Domain Objects ──────────────────────────────────────────────────

val PREVIEW_EXPENSE_BASIC = Expense(
    id = "exp-1",
    groupId = "group-1",
    title = "Dinner at Thai restaurant",
    sourceAmount = 4500L,
    sourceCurrency = "EUR",
    groupAmount = 4500L,
    groupCurrency = "EUR",
    category = ExpenseCategory.FOOD,
    paymentMethod = PaymentMethod.CREDIT_CARD,
    paymentStatus = PaymentStatus.FINISHED,
    createdBy = "Antonio",
    createdAt = LocalDateTime.of(2026, 1, 15, 20, 30)
)

val PREVIEW_EXPENSE_FOREIGN_CURRENCY = Expense(
    id = "exp-2",
    groupId = "group-1",
    title = "Taxi to airport",
    sourceAmount = 45000L,
    sourceCurrency = "THB",
    groupAmount = 1250L,
    groupCurrency = "EUR",
    exchangeRate = BigDecimal("37.037"),
    category = ExpenseCategory.TRANSPORT,
    vendor = "Grab",
    paymentMethod = PaymentMethod.CASH,
    paymentStatus = PaymentStatus.FINISHED,
    createdBy = "Maria",
    createdAt = LocalDateTime.of(2026, 1, 16, 10, 0)
)

val PREVIEW_EXPENSE_SCHEDULED = Expense(
    id = "exp-3",
    groupId = "group-1",
    title = "Hotel booking",
    sourceAmount = 32000L,
    sourceCurrency = "EUR",
    groupAmount = 32000L,
    groupCurrency = "EUR",
    category = ExpenseCategory.LODGING,
    paymentMethod = PaymentMethod.CREDIT_CARD,
    paymentStatus = PaymentStatus.SCHEDULED,
    dueDate = LocalDateTime.of(2026, 3, 20, 0, 0),
    createdBy = "Pedro",
    createdAt = LocalDateTime.of(2026, 1, 17, 14, 0)
)

val PREVIEW_EXPENSE_WITH_VENDOR = Expense(
    id = "exp-4",
    groupId = "group-1",
    title = "Museum tickets",
    sourceAmount = 2800L,
    sourceCurrency = "EUR",
    groupAmount = 2800L,
    groupCurrency = "EUR",
    category = ExpenseCategory.ACTIVITIES,
    vendor = "Grand Palace",
    paymentMethod = PaymentMethod.CASH,
    paymentStatus = PaymentStatus.FINISHED,
    createdBy = "Laura",
    createdAt = LocalDateTime.of(2026, 1, 14, 11, 0)
)

val PREVIEW_EXPENSE_OUT_OF_POCKET = Expense(
    id = "exp-5",
    groupId = "group-1",
    title = "Group dinner at La Paella",
    sourceAmount = 16500L,
    sourceCurrency = "EUR",
    groupAmount = 16500L,
    groupCurrency = "EUR",
    category = ExpenseCategory.FOOD,
    paymentMethod = PaymentMethod.CREDIT_CARD,
    paymentStatus = PaymentStatus.FINISHED,
    createdBy = "user-maria",
    payerType = PayerType.USER,
    payerId = "user-maria",
    createdAt = LocalDateTime.of(2026, 1, 18, 21, 0)
)

// ── Member Profiles ─────────────────────────────────────────────────────────

val PREVIEW_MEMBER_PROFILES = mapOf(
    "user-maria" to User(
        userId = "user-maria",
        email = "maria@test.com",
        displayName = "María"
    ),
    "Antonio" to User(
        userId = "Antonio",
        email = "antonio@test.com",
        displayName = "Antonio"
    )
)

val PREVIEW_EXPENSES = listOf(
    PREVIEW_EXPENSE_BASIC,
    PREVIEW_EXPENSE_FOREIGN_CURRENCY,
    PREVIEW_EXPENSE_SCHEDULED,
    PREVIEW_EXPENSE_WITH_VENDOR,
    PREVIEW_EXPENSE_OUT_OF_POCKET
)
