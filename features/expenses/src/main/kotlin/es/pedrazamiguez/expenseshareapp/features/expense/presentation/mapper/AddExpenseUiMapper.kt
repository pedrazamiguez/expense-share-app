package es.pedrazamiguez.expenseshareapp.features.expense.presentation.mapper

import es.pedrazamiguez.expenseshareapp.core.common.provider.LocaleProvider
import es.pedrazamiguez.expenseshareapp.core.common.provider.ResourceProvider
import es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.formatter.formatCurrencyAmount
import es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.formatter.formatDisplay
import es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.formatter.formatNumberForDisplay
import es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.formatter.formatRateForDisplay
import es.pedrazamiguez.expenseshareapp.domain.converter.CurrencyConverter
import es.pedrazamiguez.expenseshareapp.domain.enums.ExpenseCategory
import es.pedrazamiguez.expenseshareapp.domain.enums.PaymentMethod
import es.pedrazamiguez.expenseshareapp.domain.enums.PaymentStatus
import es.pedrazamiguez.expenseshareapp.domain.enums.SplitType
import es.pedrazamiguez.expenseshareapp.domain.model.Expense
import es.pedrazamiguez.expenseshareapp.domain.model.ExpenseSplit
import es.pedrazamiguez.expenseshareapp.domain.model.Currency
import es.pedrazamiguez.expenseshareapp.features.expense.R
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.extensions.toStringRes
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.model.CategoryUiModel
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.model.CurrencyUiModel
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.model.PaymentMethodUiModel
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.model.PaymentStatusUiModel
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.model.SplitTypeUiModel
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.model.SplitUiModel
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.viewmodel.state.AddExpenseUiState
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

class AddExpenseUiMapper(
    private val localeProvider: LocaleProvider,
    private val resourceProvider: ResourceProvider,
) {

    companion object {
        private const val RATE_PRECISION = 6
    }

    // ── Domain → UI Model Mapping ──────────────────────────────────────────

    fun mapCurrency(currency: Currency): CurrencyUiModel {
        return CurrencyUiModel(
            code = currency.code,
            displayText = currency.formatDisplay(),
            decimalDigits = currency.decimalDigits
        )
    }

    fun mapCurrencies(currencies: List<Currency>): ImmutableList<CurrencyUiModel> {
        return currencies.map { mapCurrency(it) }.toImmutableList()
    }

    fun mapPaymentMethods(methods: List<PaymentMethod>): ImmutableList<PaymentMethodUiModel> {
        return methods.map { method ->
            PaymentMethodUiModel(
                id = method.name, displayText = resourceProvider.getString(method.toStringRes())
            )
        }.toImmutableList()
    }

    /**
     * Maps a list of ExpenseCategory enums to UI models, filtering out
     * non-user-selectable categories (CONTRIBUTION, REFUND).
     */
    fun mapCategories(categories: List<ExpenseCategory>): ImmutableList<CategoryUiModel> {
        return categories
            .filter { it != ExpenseCategory.CONTRIBUTION && it != ExpenseCategory.REFUND }
            .map { category ->
                CategoryUiModel(
                    id = category.name,
                    displayText = resourceProvider.getString(category.toStringRes())
                )
            }.toImmutableList()
    }

    /**
     * Maps a list of PaymentStatus enums to UI models, filtering to only
     * user-selectable statuses (FINISHED, SCHEDULED).
     */
    fun mapPaymentStatuses(statuses: List<PaymentStatus>): ImmutableList<PaymentStatusUiModel> {
        return statuses
            .filter { it == PaymentStatus.FINISHED || it == PaymentStatus.SCHEDULED }
            .map { status ->
                PaymentStatusUiModel(
                    id = status.name,
                    displayText = resourceProvider.getString(status.toStringRes())
                )
            }.toImmutableList()
    }

    /**
     * Formats a due date millis value to a locale-aware display string.
     */
    fun formatDueDateForDisplay(dateMillis: Long): String {
        val locale = localeProvider.getCurrentLocale()
        val dateTime = LocalDateTime.ofInstant(
            Instant.ofEpochMilli(dateMillis),
            ZoneOffset.UTC
        )
        val formatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(locale)
        return dateTime.format(formatter)
    }

    // ── Label Building ─────────────────────────────────────────────────────

    fun buildExchangeRateLabel(
        groupCurrency: CurrencyUiModel, selectedCurrency: CurrencyUiModel
    ): String {
        return resourceProvider.getString(
            R.string.add_expense_rate_label_format,
            groupCurrency.displayText,
            selectedCurrency.displayText
        )
    }

    fun buildGroupAmountLabel(groupCurrency: CurrencyUiModel): String {
        return resourceProvider.getString(
            R.string.add_expense_amount_in, groupCurrency.displayText
        )
    }

    // ── Formatting ─────────────────────────────────────────────────────────

    /**
     * Formats an internal number string (dot decimal) to locale-aware display format.
     *
     * @param internalValue The number in internal format (e.g., "200.08")
     * @param maxDecimalPlaces Maximum decimal places to display
     * @param minDecimalPlaces Minimum decimal places to display (pads with zeros if needed).
     *                         Use this to respect currency decimal digits (e.g., 2 for EUR).
     * @return Locale-formatted string (e.g., "200,08" for Spanish)
     */
    fun formatForDisplay(
        internalValue: String, maxDecimalPlaces: Int, minDecimalPlaces: Int = 0
    ): String {
        return internalValue.formatNumberForDisplay(
            locale = localeProvider.getCurrentLocale(),
            maxDecimalPlaces = maxDecimalPlaces,
            minDecimalPlaces = minDecimalPlaces
        )
    }

    /**
     * Formats an exchange rate for display using locale-aware formatting.
     *
     * @param internalValue The rate in internal format (e.g., "37.22")
     * @return Locale-formatted string (e.g., "37,22" for Spanish)
     */
    fun formatRateForDisplay(internalValue: String): String {
        return internalValue.formatRateForDisplay(locale = localeProvider.getCurrentLocale())
    }

    /**
     * Converts a raw cents value to a locale-aware, symbol-correct display string.
     *
     * Delegates to [formatCurrencyAmount] so that symbol resolution (including
     * disambiguated $ variants like "US$", "MX$", etc.) is handled consistently
     * across the whole app.
     *
     * @param cents    The amount in the smallest currency unit (e.g. 108574 for 1,085.74 EUR).
     * @param currency The UI model of the currency, used for the ISO code.
     * @return A display string such as "€1,085.74" (en-US) or "1.085,74 €" (es-ES).
     */
    fun formatCentsForDisplay(cents: Long, currency: CurrencyUiModel): String =
        formatCurrencyAmount(
            amount = cents, currencyCode = currency.code, locale = localeProvider.getCurrentLocale()
        )

    // ── Split Type Mapping ─────────────────────────────────────────────

    fun mapSplitTypes(splitTypes: List<SplitType>): ImmutableList<SplitTypeUiModel> {
        return splitTypes.map { splitType ->
            SplitTypeUiModel(
                id = splitType.name,
                displayText = resourceProvider.getString(splitType.toStringRes())
            )
        }.toImmutableList()
    }

    /**
     * Builds initial split UI models for all group members with equal amounts.
     */
    fun buildInitialSplits(
        memberIds: List<String>,
        shares: List<ExpenseSplit>
    ): ImmutableList<SplitUiModel> {
        return memberIds.mapIndexed { index, userId ->
            val share = shares.getOrNull(index)
            val amountCents = share?.amountCents ?: 0L
            SplitUiModel(
                userId = userId,
                displayName = userId, // Will be resolved to display name in the future
                amountCents = amountCents,
                formattedAmount = formatCentsValue(amountCents),
                amountInput = formatCentsValue(amountCents),
                percentageInput = share?.percentage?.toPlainString() ?: ""
            )
        }.toImmutableList()
    }

    /**
     * Formats cents to a plain decimal string for input fields.
     */
    fun formatCentsValue(cents: Long): String {
        val amount = BigDecimal(cents).movePointLeft(2)
        return amount.toPlainString().formatNumberForDisplay(
            locale = localeProvider.getCurrentLocale(),
            maxDecimalPlaces = 2,
            minDecimalPlaces = 2
        )
    }

    /**
     * Maps split UI models to domain ExpenseSplit list.
     */
    fun mapSplitsToDomain(
        splits: List<SplitUiModel>,
        splitType: SplitType
    ): List<ExpenseSplit> {
        return splits.filter { !it.isExcluded }.map { uiModel ->
            ExpenseSplit(
                userId = uiModel.userId,
                amountCents = uiModel.amountCents,
                percentage = if (splitType == SplitType.PERCENT) {
                    uiModel.percentageInput.toBigDecimalOrNull()
                } else null,
                isExcluded = uiModel.isExcluded
            )
        }
    }

    private fun SplitType.toStringRes(): Int = when (this) {
        SplitType.EQUAL -> R.string.split_type_equal
        SplitType.EXACT -> R.string.split_type_exact
        SplitType.PERCENT -> R.string.split_type_percent
    }

    // ── UI State → Domain Mapping ──────────────────────────────────────────

    fun mapToDomain(state: AddExpenseUiState, groupId: String): Result<Expense> {
        return try {
            val sourceCurrencyCode = state.selectedCurrency?.code
            val groupCurrencyCode = state.groupCurrency?.code
            val sourceDecimalDigits = state.selectedCurrency?.decimalDigits ?: 2
            val groupDecimalDigits = state.groupCurrency?.decimalDigits ?: 2

            val sourceAmount = parseToSmallestUnit(state.sourceAmount, sourceDecimalDigits)

            // Convert display rate (1 GroupCurrency = X SourceCurrency) to internal rate (1 SourceCurrency = X GroupCurrency)
            // Normalize the rate string to handle locale-specific decimal separators (comma vs dot)
            val normalizedDisplayRate =
                CurrencyConverter.normalizeAmountString(state.displayExchangeRate.trim())
            val displayRate = normalizedDisplayRate.toBigDecimalOrNull() ?: BigDecimal.ONE
            val internalRate = if (displayRate.compareTo(BigDecimal.ZERO) != 0) {
                BigDecimal.ONE.divide(displayRate, RATE_PRECISION, RoundingMode.HALF_UP)
            } else {
                BigDecimal.ZERO
            }

            // Calculate groupAmount based on whether it was explicitly set or needs to be calculated
            val groupAmount = if (state.calculatedGroupAmount.isNotBlank()) {
                // User explicitly set the group amount (Revolut case) or it was calculated
                parseToSmallestUnit(state.calculatedGroupAmount, groupDecimalDigits)
            } else {
                // Not set, calculate from source amount and internal rate using BigDecimal
                BigDecimal(sourceAmount).multiply(internalRate).setScale(0, RoundingMode.HALF_UP)
                    .toLong()
            }

            // Resolve PaymentMethod from the UI model's id
            val paymentMethod = state.selectedPaymentMethod?.let {
                PaymentMethod.fromString(it.id)
            } ?: PaymentMethod.CASH

            // Resolve Category from the UI model's id
            val category = state.selectedCategory?.let {
                runCatching { ExpenseCategory.fromString(it.id) }.getOrDefault(ExpenseCategory.OTHER)
            } ?: ExpenseCategory.OTHER

            // Resolve PaymentStatus from the UI model's id
            val paymentStatus = state.selectedPaymentStatus?.let {
                runCatching { PaymentStatus.fromString(it.id) }.getOrDefault(PaymentStatus.FINISHED)
            } ?: PaymentStatus.FINISHED

            // Resolve due date
            val dueDate = if (paymentStatus == PaymentStatus.SCHEDULED && state.dueDateMillis != null) {
                LocalDateTime.ofInstant(
                    Instant.ofEpochMilli(state.dueDateMillis),
                    ZoneOffset.UTC
                )
            } else null

            // Resolve split type
            val splitType = state.selectedSplitType?.let {
                SplitType.fromString(it.id)
            } ?: SplitType.EQUAL

            val expense = Expense(
                groupId = groupId,
                title = state.expenseTitle,
                sourceAmount = sourceAmount,
                sourceCurrency = sourceCurrencyCode ?: "EUR",
                groupAmount = groupAmount,
                groupCurrency = groupCurrencyCode ?: "EUR",
                exchangeRate = internalRate,
                category = category,
                vendor = state.vendor.ifBlank { null },
                paymentMethod = paymentMethod,
                paymentStatus = paymentStatus,
                dueDate = dueDate,
                receiptLocalUri = state.receiptUri,
                splitType = splitType,
                splits = mapSplitsToDomain(state.splits, splitType)
            )
            Result.success(expense)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Parses an amount string to the smallest currency unit (e.g., cents for EUR, yen for JPY).
     * Uses the currency's decimal digits to determine the multiplier.
     *
     * Examples:
     * - "10.50" with EUR (2 decimals) → 1050 (cents)
     * - "10" with JPY (0 decimals) → 10 (yen)
     * - "10.500" with TND (3 decimals) → 10500 (millimes)
     */
    private fun parseToSmallestUnit(amountString: String, decimalPlaces: Int): Long {
        val normalizedString = CurrencyConverter.normalizeAmountString(amountString.trim())

        val amount = normalizedString.toBigDecimalOrNull() ?: BigDecimal.ZERO
        val multiplier = BigDecimal.TEN.pow(decimalPlaces)

        return amount.multiply(multiplier).setScale(0, RoundingMode.HALF_UP).toLong()
    }
}
