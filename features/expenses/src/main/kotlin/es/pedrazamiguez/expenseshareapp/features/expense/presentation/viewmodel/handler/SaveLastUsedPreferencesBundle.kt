package es.pedrazamiguez.expenseshareapp.features.expense.presentation.viewmodel.handler

import es.pedrazamiguez.expenseshareapp.domain.usecase.setting.SetGroupLastUsedCategoryUseCase
import es.pedrazamiguez.expenseshareapp.domain.usecase.setting.SetGroupLastUsedCurrencyUseCase
import es.pedrazamiguez.expenseshareapp.domain.usecase.setting.SetGroupLastUsedPaymentMethodUseCase

/**
 * Bundles the three "save last used preference" use cases injected into
 * [SubmitEventHandler] to reduce constructor parameter count below the
 * detekt `LongParameterList` threshold.
 */
data class SaveLastUsedPreferencesBundle(
    val setGroupLastUsedCurrencyUseCase: SetGroupLastUsedCurrencyUseCase,
    val setGroupLastUsedPaymentMethodUseCase: SetGroupLastUsedPaymentMethodUseCase,
    val setGroupLastUsedCategoryUseCase: SetGroupLastUsedCategoryUseCase
)
