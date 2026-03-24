package es.pedrazamiguez.expenseshareapp.features.expense.di

import es.pedrazamiguez.expenseshareapp.core.common.provider.LocaleProvider
import es.pedrazamiguez.expenseshareapp.core.common.provider.ResourceProvider
import es.pedrazamiguez.expenseshareapp.core.designsystem.navigation.NavigationProvider
import es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.screen.ScreenUiProvider
import es.pedrazamiguez.expenseshareapp.domain.service.ExpenseCalculatorService
import es.pedrazamiguez.expenseshareapp.domain.service.ExpenseValidationService
import es.pedrazamiguez.expenseshareapp.domain.service.split.ExpenseSplitCalculatorFactory
import es.pedrazamiguez.expenseshareapp.domain.service.split.SplitPreviewService
import es.pedrazamiguez.expenseshareapp.domain.service.split.SubunitAwareSplitService
import es.pedrazamiguez.expenseshareapp.domain.usecase.currency.GetExchangeRateUseCase
import es.pedrazamiguez.expenseshareapp.domain.usecase.expense.AddExpenseUseCase
import es.pedrazamiguez.expenseshareapp.domain.usecase.expense.DeleteExpenseUseCase
import es.pedrazamiguez.expenseshareapp.domain.usecase.expense.GetGroupExpenseConfigUseCase
import es.pedrazamiguez.expenseshareapp.domain.usecase.expense.GetGroupExpensesFlowUseCase
import es.pedrazamiguez.expenseshareapp.domain.usecase.expense.PreviewCashExchangeRateUseCase
import es.pedrazamiguez.expenseshareapp.domain.usecase.group.GetGroupByIdUseCase
import es.pedrazamiguez.expenseshareapp.domain.usecase.setting.GetGroupLastUsedCategoryUseCase
import es.pedrazamiguez.expenseshareapp.domain.usecase.setting.GetGroupLastUsedCurrencyUseCase
import es.pedrazamiguez.expenseshareapp.domain.usecase.setting.GetGroupLastUsedPaymentMethodUseCase
import es.pedrazamiguez.expenseshareapp.domain.usecase.setting.SetGroupLastUsedCategoryUseCase
import es.pedrazamiguez.expenseshareapp.domain.usecase.setting.SetGroupLastUsedCurrencyUseCase
import es.pedrazamiguez.expenseshareapp.domain.usecase.setting.SetGroupLastUsedPaymentMethodUseCase
import es.pedrazamiguez.expenseshareapp.domain.usecase.user.GetMemberProfilesUseCase
import es.pedrazamiguez.expenseshareapp.features.expense.navigation.impl.ExpensesNavigationProviderImpl
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.mapper.AddExpenseUiMapper
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.mapper.ExpenseUiMapper
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.screen.impl.AddExpenseScreenUiProviderImpl
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.screen.impl.ExpensesScreenUiProviderImpl
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.viewmodel.AddExpenseViewModel
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.viewmodel.ExpensesViewModel
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.viewmodel.handler.AddOnEventHandler
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.viewmodel.handler.ConfigEventHandler
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.viewmodel.handler.CurrencyEventHandler
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.viewmodel.handler.SplitEventHandler
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.viewmodel.handler.SubmitEventHandler
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.viewmodel.handler.SubunitSplitEventHandler
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.bind
import org.koin.dsl.module

val expensesUiModule = module {

    single {
        AddExpenseUiMapper(
            localeProvider = get<LocaleProvider>(),
            resourceProvider = get<ResourceProvider>()
        )
    }

    single {
        ExpenseUiMapper(
            localeProvider = get<LocaleProvider>(),
            resourceProvider = get<ResourceProvider>()
        )
    }

    viewModel {
        ExpensesViewModel(
            getGroupExpensesFlowUseCase = get<GetGroupExpensesFlowUseCase>(),
            deleteExpenseUseCase = get<DeleteExpenseUseCase>(),
            expenseUiMapper = get<ExpenseUiMapper>(),
            getGroupByIdUseCase = get<GetGroupByIdUseCase>(),
            getMemberProfilesUseCase = get<GetMemberProfilesUseCase>()
        )
    }

    // ── AddExpense ViewModel with co-created handlers ───────────────────
    // Handlers are created inside the viewModel block so the SAME instances
    // are shared between the ViewModel and cross-handler references
    // (e.g., ConfigEventHandler calls CurrencyEventHandler.fetchRate()).

    viewModel {
        val mapper = get<AddExpenseUiMapper>()

        val splitHandler = SplitEventHandler(
            splitCalculatorFactory = get<ExpenseSplitCalculatorFactory>(),
            splitPreviewService = get<SplitPreviewService>(),
            addExpenseUiMapper = mapper
        )

        val subunitSplitHandler = SubunitSplitEventHandler(
            splitCalculatorFactory = get<ExpenseSplitCalculatorFactory>(),
            splitPreviewService = get<SplitPreviewService>(),
            subunitAwareSplitService = get<SubunitAwareSplitService>(),
            addExpenseUiMapper = mapper
        )

        val currencyHandler = CurrencyEventHandler(
            getExchangeRateUseCase = get<GetExchangeRateUseCase>(),
            previewCashExchangeRateUseCase = get<PreviewCashExchangeRateUseCase>(),
            expenseCalculatorService = get<ExpenseCalculatorService>(),
            addExpenseUiMapper = mapper
        )

        val configHandler = ConfigEventHandler(
            getGroupExpenseConfigUseCase = get<GetGroupExpenseConfigUseCase>(),
            getGroupLastUsedCurrencyUseCase = get<GetGroupLastUsedCurrencyUseCase>(),
            getGroupLastUsedPaymentMethodUseCase = get<GetGroupLastUsedPaymentMethodUseCase>(),
            getGroupLastUsedCategoryUseCase = get<GetGroupLastUsedCategoryUseCase>(),
            getMemberProfilesUseCase = get<GetMemberProfilesUseCase>(),
            addExpenseUiMapper = mapper,
            currencyEventHandler = currencyHandler,
            subunitSplitEventHandler = subunitSplitHandler
        )

        val submitHandler = SubmitEventHandler(
            addExpenseUseCase = get<AddExpenseUseCase>(),
            expenseValidationService = get<ExpenseValidationService>(),
            expenseCalculatorService = get<ExpenseCalculatorService>(),
            setGroupLastUsedCurrencyUseCase = get<SetGroupLastUsedCurrencyUseCase>(),
            setGroupLastUsedPaymentMethodUseCase = get<SetGroupLastUsedPaymentMethodUseCase>(),
            setGroupLastUsedCategoryUseCase = get<SetGroupLastUsedCategoryUseCase>(),
            addExpenseUiMapper = mapper
        )

        val addOnHandler = AddOnEventHandler(
            expenseCalculatorService = get<ExpenseCalculatorService>(),
            addExpenseUiMapper = mapper,
            getExchangeRateUseCase = get<GetExchangeRateUseCase>(),
            previewCashExchangeRateUseCase = get<PreviewCashExchangeRateUseCase>()
        )

        AddExpenseViewModel(
            configEventHandler = configHandler,
            currencyEventHandler = currencyHandler,
            splitEventHandler = splitHandler,
            subunitSplitEventHandler = subunitSplitHandler,
            addOnEventHandler = addOnHandler,
            submitEventHandler = submitHandler,
            addExpenseUiMapper = mapper
        )
    }

    factory { ExpensesNavigationProviderImpl() } bind NavigationProvider::class

    single { ExpensesScreenUiProviderImpl() } bind ScreenUiProvider::class
    single { AddExpenseScreenUiProviderImpl() } bind ScreenUiProvider::class
}
