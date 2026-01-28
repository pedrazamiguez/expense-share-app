package es.pedrazamiguez.expenseshareapp.domain.usecase.expense

import es.pedrazamiguez.expenseshareapp.domain.model.GroupExpenseConfig
import es.pedrazamiguez.expenseshareapp.domain.repository.CurrencyRepository
import es.pedrazamiguez.expenseshareapp.domain.repository.GroupRepository

/**
 * Use case for fetching the configuration needed to add an expense to a group.
 *
 * This encapsulates the business logic for:
 * - Fetching the group details
 * - Fetching all available currencies
 * - Filtering currencies to only those allowed for the group
 * - Identifying the group's primary currency
 */
class GetGroupExpenseConfigUseCase(
    private val groupRepository: GroupRepository,
    private val currencyRepository: CurrencyRepository
) {
    /**
     * Fetches the expense configuration for a specific group.
     *
     * @param groupId The ID of the group to fetch configuration for
     * @param forceRefresh Whether to bypass cache and fetch fresh currency data
     * @return Result containing GroupExpenseConfig, or failure if:
     *         - groupId is null/blank
     *         - group is not found
     *         - group's currency is not in the available currencies list
     */
    suspend operator fun invoke(groupId: String?, forceRefresh: Boolean = false): Result<GroupExpenseConfig> {
        return runCatching {
            require(!groupId.isNullOrBlank()) { "Group ID cannot be null or blank" }

            val group = groupRepository.getGroupById(groupId)
                ?: throw IllegalStateException("Group not found: $groupId")

            val allCurrencies = currencyRepository.getCurrencies(forceRefresh)

            val groupCurrency = allCurrencies.find { it.code == group.currency }
                ?: throw IllegalStateException("Group currency '${group.currency}' not found in available currencies")

            // Include group's main currency plus any extra currencies configured for the group
            val allowedCodes = (listOf(group.currency) + group.extraCurrencies).distinct()
            val availableCurrencies = allCurrencies.filter { it.code in allowedCodes }

            GroupExpenseConfig(
                group = group,
                groupCurrency = groupCurrency,
                availableCurrencies = availableCurrencies
            )
        }
    }
}
