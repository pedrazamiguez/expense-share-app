package es.pedrazamiguez.expenseshareapp.domain.di

import es.pedrazamiguez.expenseshareapp.domain.repository.CashWithdrawalRepository
import es.pedrazamiguez.expenseshareapp.domain.repository.CurrencyRepository
import es.pedrazamiguez.expenseshareapp.domain.repository.ExpenseRepository
import es.pedrazamiguez.expenseshareapp.domain.repository.GroupRepository
import es.pedrazamiguez.expenseshareapp.domain.repository.SubunitRepository
import es.pedrazamiguez.expenseshareapp.domain.service.AddOnCalculationService
import es.pedrazamiguez.expenseshareapp.domain.service.ExchangeRateCalculationService
import es.pedrazamiguez.expenseshareapp.domain.service.ExpenseCalculatorService
import es.pedrazamiguez.expenseshareapp.domain.service.ExpenseValidationService
import es.pedrazamiguez.expenseshareapp.domain.service.GroupMembershipService
import es.pedrazamiguez.expenseshareapp.domain.service.RemainderDistributionService
import es.pedrazamiguez.expenseshareapp.domain.service.addon.AddOnResolverFactory
import es.pedrazamiguez.expenseshareapp.domain.service.split.ExpenseSplitCalculatorFactory
import es.pedrazamiguez.expenseshareapp.domain.service.split.SplitPreviewService
import es.pedrazamiguez.expenseshareapp.domain.service.split.SubunitAwareSplitService
import es.pedrazamiguez.expenseshareapp.domain.usecase.expense.AddExpenseUseCase
import es.pedrazamiguez.expenseshareapp.domain.usecase.expense.DeleteExpenseUseCase
import es.pedrazamiguez.expenseshareapp.domain.usecase.expense.GetGroupExpenseConfigUseCase
import es.pedrazamiguez.expenseshareapp.domain.usecase.expense.GetGroupExpensesFlowUseCase
import es.pedrazamiguez.expenseshareapp.domain.usecase.expense.PreviewCashExchangeRateUseCase
import org.koin.dsl.module

val expensesDomainModule = module {
    factory<AddExpenseUseCase> {
        AddExpenseUseCase(
            expenseRepository = get<ExpenseRepository>(),
            cashWithdrawalRepository = get<CashWithdrawalRepository>(),
            expenseCalculatorService = get<ExpenseCalculatorService>(),
            exchangeRateCalculationService = get<ExchangeRateCalculationService>(),
            groupMembershipService = get<GroupMembershipService>()
        )
    }
    factory<DeleteExpenseUseCase> {
        DeleteExpenseUseCase(
            expenseRepository = get<ExpenseRepository>(),
            cashWithdrawalRepository = get<CashWithdrawalRepository>(),
            groupMembershipService = get<GroupMembershipService>()
        )
    }
    factory<GetGroupExpensesFlowUseCase> { GetGroupExpensesFlowUseCase(expenseRepository = get<ExpenseRepository>()) }
    factory<GetGroupExpenseConfigUseCase> {
        GetGroupExpenseConfigUseCase(
            groupRepository = get<GroupRepository>(),
            currencyRepository = get<CurrencyRepository>(),
            subunitRepository = get<SubunitRepository>()
        )
    }
    factory { AddOnResolverFactory() }
    factory { ExpenseCalculatorService() }
    factory { AddOnCalculationService(addOnResolverFactory = get<AddOnResolverFactory>()) }
    factory { ExchangeRateCalculationService() }
    factory { RemainderDistributionService() }
    factory {
        PreviewCashExchangeRateUseCase(
            cashWithdrawalRepository = get<CashWithdrawalRepository>(),
            expenseCalculatorService = get<ExpenseCalculatorService>(),
            exchangeRateCalculationService = get<ExchangeRateCalculationService>()
        )
    }
    factory { SplitPreviewService() }
    factory { ExpenseSplitCalculatorFactory(expenseCalculatorService = get<ExpenseCalculatorService>()) }
    factory { ExpenseValidationService(splitCalculatorFactory = get<ExpenseSplitCalculatorFactory>()) }
    factory { SubunitAwareSplitService(splitCalculatorFactory = get<ExpenseSplitCalculatorFactory>()) }
}
