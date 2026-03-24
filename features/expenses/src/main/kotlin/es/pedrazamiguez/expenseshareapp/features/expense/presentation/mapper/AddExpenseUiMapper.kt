package es.pedrazamiguez.expenseshareapp.features.expense.presentation.mapper

import es.pedrazamiguez.expenseshareapp.core.common.provider.LocaleProvider
import es.pedrazamiguez.expenseshareapp.core.common.provider.ResourceProvider
import es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.formatter.formatCurrencyAmount
import es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.formatter.formatNumberForDisplay
import es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.formatter.formatRateForDisplay
import es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.model.CurrencyUiModel
import es.pedrazamiguez.expenseshareapp.domain.converter.CurrencyConverter
import es.pedrazamiguez.expenseshareapp.domain.enums.ExpenseCategory
import es.pedrazamiguez.expenseshareapp.domain.enums.PaymentMethod
import es.pedrazamiguez.expenseshareapp.domain.enums.PaymentStatus
import es.pedrazamiguez.expenseshareapp.domain.enums.SplitType
import es.pedrazamiguez.expenseshareapp.domain.model.AddOn
import es.pedrazamiguez.expenseshareapp.domain.model.Expense
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.model.AddOnUiModel
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.viewmodel.state.AddExpenseUiState
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset

/**
 * Core Add Expense mapper: handles date formatting, display formatting,
 * add-on mapping, and the full UI-state → domain [Expense] conversion.
 *
 * Option-list mapping (currencies, payment methods, categories, split types)
 * is in [AddExpenseOptionsMapper]. Split-display and split-domain mapping is in
 * [AddExpenseSplitMapper]. This class delegates split mapping to [splitMapper]
 * inside [mapToDomain].
 */
class AddExpenseUiMapper(
    private val localeProvider: LocaleProvider,
    @Suppress("UnusedPrivateMember")
    private val resourceProvider: ResourceProvider,
    private val splitMapper: AddExpenseSplitMapper
) {

    companion object {
        private const val RATE_PRECISION = 6
    }

    // ── Date Formatting ────────────────────────────────────────────────────

    /**
     * Formats a due date millis value to a locale-aware display string.
     */
    fun formatDueDateForDisplay(dateMillis: Long): String {
        val locale = localeProvider.getCurrentLocale()
        val dateTime = LocalDateTime.ofInstant(
            Instant.ofEpochMilli(dateMillis),
            ZoneOffset.UTC
        )
        val formatter = java.time.format.DateTimeFormatter
            .ofLocalizedDate(java.time.format.FormatStyle.MEDIUM)
            .withLocale(locale)
        return dateTime.format(formatter)
    }

    // ── Formatting ─────────────────────────────────────────────────────────

    /**
     * Formats an internal number string (dot decimal) to locale-aware display format.
     */
    fun formatForDisplay(internalValue: String, maxDecimalPlaces: Int, minDecimalPlaces: Int = 0): String =
        internalValue.formatNumberForDisplay(
            locale = localeProvider.getCurrentLocale(),
            maxDecimalPlaces = maxDecimalPlaces,
            minDecimalPlaces = minDecimalPlaces
        )

    /**
     * Formats an exchange rate for display using locale-aware formatting.
     */
    fun formatRateForDisplay(internalValue: String): String =
        internalValue.formatRateForDisplay(locale = localeProvider.getCurrentLocale())

    /**
     * Converts a raw cents value to a locale-aware, symbol-correct display string.
     */
    fun formatCentsForDisplay(cents: Long, currency: CurrencyUiModel): String = formatCurrencyAmount(
        amount = cents,
        currencyCode = currency.code,
        locale = localeProvider.getCurrentLocale()
    )

    // ── Add-On Mapping ──────────────────────────────────────────────────

    /**
     * Maps add-on UI models to domain [AddOn] objects.
     * Only includes add-ons with a valid resolved amount.
     */
    fun mapAddOnsToDomain(
        addOns: List<AddOnUiModel>,
        fallbackCurrencyCode: String
    ): List<AddOn> = addOns
        .filter { it.resolvedAmountCents > 0 }
        .map { uiModel ->
            val exchangeRate = resolveAddOnExchangeRate(uiModel.displayExchangeRate)
            AddOn(
                id = uiModel.id,
                type = uiModel.type,
                mode = uiModel.mode,
                valueType = uiModel.valueType,
                amountCents = uiModel.resolvedAmountCents,
                currency = uiModel.currency?.code ?: fallbackCurrencyCode,
                exchangeRate = exchangeRate,
                groupAmountCents = uiModel.groupAmountCents,
                paymentMethod = uiModel.paymentMethod?.let {
                    runCatching {
                        PaymentMethod.fromString(it.id)
                    }.getOrDefault(PaymentMethod.OTHER)
                } ?: PaymentMethod.OTHER,
                description = uiModel.description.ifBlank { null }
            )
        }

    private fun resolveAddOnExchangeRate(displayExchangeRate: String): BigDecimal {
        val normalizedRate = CurrencyConverter.normalizeAmountString(displayExchangeRate.trim())
        val displayRate = normalizedRate.toBigDecimalOrNull() ?: BigDecimal.ONE
        return if (displayRate.compareTo(BigDecimal.ZERO) != 0) {
            BigDecimal.ONE.divide(displayRate, RATE_PRECISION, RoundingMode.HALF_UP)
        } else {
            BigDecimal.ONE
        }
    }

    // ── UI State → Domain Mapping ──────────────────────────────────────────

    fun mapToDomain(state: AddExpenseUiState, groupId: String): Result<Expense> = try {
        val sourceCurrencyCode = state.selectedCurrency?.code
        val groupCurrencyCode = state.groupCurrency?.code
        val sourceDecimalDigits = state.selectedCurrency?.decimalDigits ?: 2
        val groupDecimalDigits = state.groupCurrency?.decimalDigits ?: 2

        val sourceAmount = parseToSmallestUnit(state.sourceAmount, sourceDecimalDigits)

        val normalizedDisplayRate =
            CurrencyConverter.normalizeAmountString(state.displayExchangeRate.trim())
        val displayRate = normalizedDisplayRate.toBigDecimalOrNull() ?: BigDecimal.ONE
        val internalRate = if (displayRate.compareTo(BigDecimal.ZERO) != 0) {
            BigDecimal.ONE.divide(displayRate, RATE_PRECISION, RoundingMode.HALF_UP)
        } else {
            BigDecimal.ZERO
        }

        val groupAmount = if (state.calculatedGroupAmount.isNotBlank()) {
            parseToSmallestUnit(state.calculatedGroupAmount, groupDecimalDigits)
        } else {
            BigDecimal(sourceAmount).multiply(internalRate).setScale(0, RoundingMode.HALF_UP).toLong()
        }

        val paymentMethod = state.selectedPaymentMethod?.let {
            PaymentMethod.fromString(it.id)
        } ?: PaymentMethod.CASH

        val category = state.selectedCategory?.let {
            runCatching { ExpenseCategory.fromString(it.id) }.getOrDefault(ExpenseCategory.OTHER)
        } ?: ExpenseCategory.OTHER

        val paymentStatus = state.selectedPaymentStatus?.let {
            runCatching { PaymentStatus.fromString(it.id) }.getOrDefault(PaymentStatus.FINISHED)
        } ?: PaymentStatus.FINISHED

        val dueDate = if (paymentStatus == PaymentStatus.SCHEDULED && state.dueDateMillis != null) {
            LocalDateTime.ofInstant(Instant.ofEpochMilli(state.dueDateMillis), ZoneOffset.UTC)
        } else {
            null
        }

        val splitType = state.selectedSplitType?.let { SplitType.fromString(it.id) } ?: SplitType.EQUAL

        val splits = if (state.isSubunitMode && state.entitySplits.isNotEmpty()) {
            splitMapper.mapEntitySplitsToDomain(state.entitySplits, splitType)
        } else {
            splitMapper.mapSplitsToDomain(state.splits, splitType)
        }

        val addOns = mapAddOnsToDomain(
            state.addOns,
            sourceCurrencyCode ?: groupCurrencyCode ?: "EUR"
        )

        val expense = Expense(
            groupId = groupId,
            title = state.expenseTitle,
            sourceAmount = sourceAmount,
            sourceCurrency = sourceCurrencyCode ?: "EUR",
            groupAmount = groupAmount,
            groupCurrency = groupCurrencyCode ?: "EUR",
            exchangeRate = internalRate,
            addOns = addOns,
            category = category,
            vendor = state.vendor.ifBlank { null },
            notes = state.notes.ifBlank { null },
            paymentMethod = paymentMethod,
            paymentStatus = paymentStatus,
            dueDate = dueDate,
            receiptLocalUri = state.receiptUri,
            splitType = splitType,
            splits = splits
        )
        Result.success(expense)
    } catch (e: Exception) {
        Result.failure(e)
    }

    // ── Private helpers ──────────────────────────────────────────────────

    private fun parseToSmallestUnit(amountString: String, decimalPlaces: Int): Long {
        val normalizedString = CurrencyConverter.normalizeAmountString(amountString.trim())
        val amount = normalizedString.toBigDecimalOrNull() ?: BigDecimal.ZERO
        val multiplier = BigDecimal.TEN.pow(decimalPlaces)
        return amount.multiply(multiplier).setScale(0, RoundingMode.HALF_UP).toLong()
    }
}
