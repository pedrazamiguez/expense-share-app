package es.pedrazamiguez.expenseshareapp.domain.service.addon

import es.pedrazamiguez.expenseshareapp.domain.enums.AddOnValueType

/**
 * Factory that vends the correct [AddOnAmountResolver] strategy based on [AddOnValueType].
 *
 * Mirrors the [ExpenseSplitCalculatorFactory] pattern used for expense split strategies.
 */
class AddOnResolverFactory {

    fun create(valueType: AddOnValueType): AddOnAmountResolver = when (valueType) {
        AddOnValueType.EXACT -> ExactAddOnResolver()
        AddOnValueType.PERCENTAGE -> PercentageAddOnResolver()
    }
}
