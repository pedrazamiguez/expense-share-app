package es.pedrazamiguez.splittrip.features.expense.presentation.preview

import es.pedrazamiguez.splittrip.domain.enums.ExpenseCategory
import es.pedrazamiguez.splittrip.domain.enums.PayerType
import es.pedrazamiguez.splittrip.domain.enums.PaymentMethod
import es.pedrazamiguez.splittrip.domain.enums.PaymentStatus
import es.pedrazamiguez.splittrip.domain.model.Contribution
import es.pedrazamiguez.splittrip.domain.model.Expense
import es.pedrazamiguez.splittrip.domain.model.Subunit
import es.pedrazamiguez.splittrip.domain.model.User
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

val PREVIEW_EXPENSE_OUT_OF_POCKET_SUBUNIT = Expense(
    id = "exp-6",
    groupId = "group-1",
    title = "Couple spa treatment",
    sourceAmount = 12000L,
    sourceCurrency = "EUR",
    groupAmount = 12000L,
    groupCurrency = "EUR",
    category = ExpenseCategory.ACTIVITIES,
    paymentMethod = PaymentMethod.CREDIT_CARD,
    paymentStatus = PaymentStatus.FINISHED,
    createdBy = "user-antonio",
    payerType = PayerType.USER,
    payerId = "user-antonio",
    createdAt = LocalDateTime.of(2026, 1, 19, 15, 0)
)

val PREVIEW_EXPENSE_OUT_OF_POCKET_GROUP = Expense(
    id = "exp-7",
    groupId = "group-1",
    title = "Group tour tickets",
    sourceAmount = 24000L,
    sourceCurrency = "EUR",
    groupAmount = 24000L,
    groupCurrency = "EUR",
    category = ExpenseCategory.ACTIVITIES,
    paymentMethod = PaymentMethod.CREDIT_CARD,
    paymentStatus = PaymentStatus.FINISHED,
    createdBy = "user-maria",
    payerType = PayerType.USER,
    payerId = "user-maria",
    createdAt = LocalDateTime.of(2026, 1, 20, 10, 0)
)

// ── Member Profiles ─────────────────────────────────────────────────────────

val PREVIEW_MEMBER_PROFILES = mapOf(
    "user-maria" to User(
        userId = "user-maria",
        email = "maria@test.com",
        displayName = "María"
    ),
    "user-antonio" to User(
        userId = "user-antonio",
        email = "antonio@test.com",
        displayName = "Antonio"
    ),
    "Antonio" to User(
        userId = "Antonio",
        email = "antonio@test.com",
        displayName = "Antonio"
    )
)

// ── Preview current user ID ─────────────────────────────────────────────────

const val PREVIEW_CURRENT_USER_ID = "user-antonio"

// ── Preview Subunits ────────────────────────────────────────────────────────

val PREVIEW_SUBUNITS = mapOf(
    "subunit-cantalobos" to Subunit(
        id = "subunit-cantalobos",
        groupId = "group-1",
        name = "Cantalobos",
        memberIds = listOf("user-antonio", "user-andres")
    )
)

// ── Preview Paired Contributions ────────────────────────────────────────────

val PREVIEW_PAIRED_CONTRIBUTIONS = mapOf(
    "exp-5" to Contribution(
        id = "contrib-5",
        groupId = "group-1",
        userId = "user-maria",
        contributionScope = PayerType.USER,
        linkedExpenseId = "exp-5",
        amount = 16500L,
        currency = "EUR"
    ),
    "exp-6" to Contribution(
        id = "contrib-6",
        groupId = "group-1",
        userId = "user-antonio",
        contributionScope = PayerType.SUBUNIT,
        subunitId = "subunit-cantalobos",
        linkedExpenseId = "exp-6",
        amount = 12000L,
        currency = "EUR"
    ),
    "exp-7" to Contribution(
        id = "contrib-7",
        groupId = "group-1",
        userId = "user-maria",
        contributionScope = PayerType.GROUP,
        linkedExpenseId = "exp-7",
        amount = 24000L,
        currency = "EUR"
    )
)

val PREVIEW_EXPENSES = listOf(
    PREVIEW_EXPENSE_BASIC,
    PREVIEW_EXPENSE_FOREIGN_CURRENCY,
    PREVIEW_EXPENSE_SCHEDULED,
    PREVIEW_EXPENSE_WITH_VENDOR,
    PREVIEW_EXPENSE_OUT_OF_POCKET,
    PREVIEW_EXPENSE_OUT_OF_POCKET_SUBUNIT,
    PREVIEW_EXPENSE_OUT_OF_POCKET_GROUP
)
